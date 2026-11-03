package com.gestiva.accounting.payment.service;

import com.gestiva.accounting.due.entity.PaymentDue;
import com.gestiva.accounting.v2.account.entity.Account;
import com.gestiva.accounting.v2.account.repository.AccountRepository;
import com.gestiva.accounting.v2.account.web.AccountOptionView;
import com.gestiva.accounting.v2.account.web.AccountWebService;
import com.gestiva.accounting.v2.journal.service.JournalEntryService;
import com.gestiva.accounting.v2.journal.web.JournalEntryForm;
import com.gestiva.accounting.v2.journal.web.JournalEntryLineForm;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentAccountingCompanyTest {

    static final Long TENANT_ID = 999L;
    static final Long COMPANY_ID = 123L;
    static final Long FINANCIAL_ACCOUNT_ID = 500L;
    static final Long TRADE_ACCOUNT_ID = 600L;
    static final LocalDate DATE = LocalDate.of(2026, 11, 3);

    @Mock JournalEntryService journalEntryService;
    @Mock AccountRepository accountRepository;
    @Mock CompanyContext companyContext;
    @Mock AccountWebService accountWebService;

    @InjectMocks PaymentAccountingService service;

    private void prepareAccounts(String tradeCode) {
        AccountOptionView financialOption =
                mock(AccountOptionView.class);

        when(financialOption.getId())
                .thenReturn(FINANCIAL_ACCOUNT_ID);

        when(accountWebService.findFinancialAccountOptions(TENANT_ID))
                .thenReturn(List.of(financialOption));

        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        Account tradeAccount = mock(Account.class);
        when(tradeAccount.getId())
                .thenReturn(TRADE_ACCOUNT_ID);

        when(accountRepository.findByTenantIdAndCompanyIdAndCode(
                TENANT_ID, COMPANY_ID, tradeCode))
                .thenReturn(Optional.of(tradeAccount));
    }

    private PaymentDue prepareDue(String direction) {
        PaymentDue due = mock(PaymentDue.class);

        when(due.getCompanyId()).thenReturn(COMPANY_ID);
        when(due.getDirection()).thenReturn(direction);
        when(due.getReferenceType())
                .thenReturn("TEST_INVOICE");
        when(due.getReferenceId()).thenReturn(77L);

        return due;
    }

    private JournalEntryForm captureEntry(
            PaymentDue due, String expectedCausal) {

        when(journalEntryService.createEntry(
                eq(TENANT_ID),
                any(JournalEntryForm.class),
                eq("TEST_INVOICE"),
                eq(77L)))
                .thenReturn(900L);

        Long id = service.createAccountingEntryForPayment(
                TENANT_ID,
                due,
                DATE,
                new BigDecimal("100.00"),
                FINANCIAL_ACCOUNT_ID,
                "TEST",
                "Test contabile");

        assertEquals(900L, id);

        ArgumentCaptor<JournalEntryForm> captor =
                ArgumentCaptor.forClass(JournalEntryForm.class);

        verify(journalEntryService).createEntry(
                eq(TENANT_ID),
                captor.capture(),
                eq("TEST_INVOICE"),
                eq(77L));

        JournalEntryForm entry = captor.getValue();

        assertEquals(expectedCausal, entry.getCausalCode());
        assertEquals(DATE, entry.getEntryDate());
        assertEquals("EUR", entry.getCurrencyCode());
        assertEquals(2, entry.getLines().size());

        BigDecimal debit = entry.getLines().stream()
                .map(JournalEntryLineForm::getDebitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal credit = entry.getLines().stream()
                .map(JournalEntryLineForm::getCreditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, debit.compareTo(credit));
        assertEquals(0,
                debit.compareTo(new BigDecimal("100.00")));

        return entry;
    }

    private void assertLine(
            JournalEntryLineForm line,
            Long accountId,
            String debit,
            String credit) {

        assertEquals(accountId, line.getAccountId());
        assertEquals(0,
                new BigDecimal(debit)
                        .compareTo(line.getDebitAmount()));
        assertEquals(0,
                new BigDecimal(credit)
                        .compareTo(line.getCreditAmount()));
    }

    @Test
    void customerReceiptShouldBeBalancedAndCompanyScoped() {
        prepareAccounts("1210");
        PaymentDue due = prepareDue("RECEIVABLE");

        JournalEntryForm entry =
                captureEntry(due, "CUSTOMER_RECEIPT");

        assertLine(entry.getLines().get(0),
                FINANCIAL_ACCOUNT_ID, "100.00", "0.00");

        assertLine(entry.getLines().get(1),
                TRADE_ACCOUNT_ID, "0.00", "100.00");

        verify(accountRepository)
                .findByTenantIdAndCompanyIdAndCode(
                        TENANT_ID, COMPANY_ID, "1210");
    }

    @Test
    void supplierPaymentShouldBeBalancedAndCompanyScoped() {
        prepareAccounts("2110");
        PaymentDue due = prepareDue("PAYABLE");

        JournalEntryForm entry =
                captureEntry(due, "SUPPLIER_PAYMENT");

        assertLine(entry.getLines().get(0),
                TRADE_ACCOUNT_ID, "100.00", "0.00");

        assertLine(entry.getLines().get(1),
                FINANCIAL_ACCOUNT_ID, "0.00", "100.00");

        verify(accountRepository)
                .findByTenantIdAndCompanyIdAndCode(
                        TENANT_ID, COMPANY_ID, "2110");
    }

    @Test
    void shouldRejectForeignCompanyDueBeforeCreatingJournal() {

        AccountOptionView financialOption =
                mock(AccountOptionView.class);

        when(financialOption.getId())
                .thenReturn(FINANCIAL_ACCOUNT_ID);

        when(accountWebService.findFinancialAccountOptions(TENANT_ID))
                .thenReturn(List.of(financialOption));

        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        PaymentDue foreignDue = mock(PaymentDue.class);

        when(foreignDue.getCompanyId()).thenReturn(456L);

        assertThrows(
                com.gestiva.common.exception.BusinessException.class,
                () -> service.createAccountingEntryForPayment(
                        TENANT_ID,
                        foreignDue,
                        DATE,
                        new BigDecimal("100.00"),
                        FINANCIAL_ACCOUNT_ID,
                        "TEST",
                        "Tentativo altra società"));

        verifyNoInteractions(accountRepository, journalEntryService);
    }

    @Test
    void shouldRejectUnauthorizedFinancialAccountBeforeCreatingJournal() {

        // Il conto 500 non compare fra quelli finanziari autorizzati.
        when(accountWebService.findFinancialAccountOptions(TENANT_ID))
                .thenReturn(List.of());

        PaymentDue due = mock(PaymentDue.class);

        assertThrows(
                com.gestiva.common.exception.BusinessException.class,
                () -> service.createAccountingEntryForPayment(
                        TENANT_ID,
                        due,
                        DATE,
                        new BigDecimal("100.00"),
                        FINANCIAL_ACCOUNT_ID,
                        "TEST",
                        "Conto finanziario non autorizzato"));

        verifyNoInteractions(
                companyContext,
                accountRepository,
                journalEntryService);
    }

}
