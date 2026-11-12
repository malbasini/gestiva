
package com.gestiva.creditnote.service;

import com.gestiva.accounting.v2.account.entity.Account;
import com.gestiva.accounting.v2.account.repository.AccountRepository;
import com.gestiva.accounting.v2.journal.entity.JournalEntry;
import com.gestiva.accounting.v2.journal.entity.JournalEntryLine;
import com.gestiva.accounting.v2.journal.repository.JournalEntryRepository;
import com.gestiva.accounting.v2.journal.repository.JournalEntryLineRepository;
import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.entity.InvoiceLine;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.billing.invoice.repository.InvoiceLineRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteLine;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.creditnote.repository.CreditNoteLineRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.settings.sequence.service.DocumentSequenceService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerCreditNoteAccountingServiceTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 3L;
    private static final Long INVOICE = 10L;
    private static final Long NOTE = 50L;

    @Mock CompanyContext companyContext;
    @Mock CreditNoteRepository notes;
    @Mock CreditNoteLineRepository noteLines;
    @Mock InvoiceRepository invoices;
    @Mock InvoiceLineRepository invoiceLines;
    @Mock AccountRepository accounts;
    @Mock JournalEntryRepository entries;
    @Mock JournalEntryLineRepository entryLines;
    @Mock DocumentSequenceService sequences;

    @InjectMocks
    CustomerCreditNoteAccountingService service;

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    @BeforeEach
    void setup() {
        lenient().when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);
    }

    private CreditNote note(String status) {
        CreditNote value = new CreditNote();
        ReflectionTestUtils.setField(value, "id", NOTE);
        value.setTenantId(TENANT);
        value.setCompanyId(COMPANY);
        value.setCreditNoteType("CUSTOMER");
        value.setStatus(status);
        value.setOriginalInvoiceId(INVOICE);
        value.setPartyId(20L);
        value.setCreditNoteNumber("CCN-00001");
        value.setCreditNoteDate(LocalDate.of(2026, 11, 12));
        value.setCurrencyCode("EUR");
        value.setReason("Rettifica fattura");
        value.setSubtotalAmount(bd("100.00"));
        value.setTaxAmount(bd("22.00"));
        value.setTotalAmount(bd("122.00"));
        return value;
    }

    private Invoice invoice() {
        Invoice value = new Invoice();
        value.setId(INVOICE);
        value.setTenantId(TENANT);
        value.setCompanyId(COMPANY);
        value.setCustomerId(20L);
        value.setCurrencyCode("EUR");
        value.setStatus("ISSUED");
        return value;
    }

    private CreditNoteLine creditLine() {
        CreditNoteLine value = new CreditNoteLine();
        value.setTenantId(TENANT);
        value.setCompanyId(COMPANY);
        value.setCreditNoteId(NOTE);
        value.setOriginalInvoiceLineId(100L);
        value.setQuantity(bd("2.000"));
        value.setLineSubtotal(bd("100.00"));
        value.setTaxAmount(bd("22.00"));
        value.setLineTotal(bd("122.00"));
        return value;
    }

    private void mockValidDocument() {
        when(notes.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, NOTE))
                .thenReturn(Optional.of(note("REGISTERED")));

        when(invoices.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, INVOICE))
                .thenReturn(Optional.of(invoice()));

        when(noteLines
                .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                        TENANT, COMPANY, NOTE))
                .thenReturn(List.of(creditLine()));

        InvoiceLine original = new InvoiceLine();
        original.setId(100L);
        original.setTenantId(TENANT);
        original.setInvoiceId(INVOICE);

        when(invoiceLines
                .findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                        TENANT, INVOICE))
                .thenReturn(List.of(original));
    }

    private void mockAccount(String code, Long id) {
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(id);
        when(account.isActive()).thenReturn(true);
        when(account.isLeafAccount()).thenReturn(true);

        when(accounts.findByTenantIdAndCompanyIdAndCode(
                TENANT, COMPANY, code))
                .thenReturn(Optional.of(account));
    }

    @Test
    void postsBalancedCustomerCreditNote() {
        mockValidDocument();

        mockAccount("1210", 1210L);
        mockAccount("4100", 4100L);
        mockAccount("2210", 2210L);

        when(sequences.nextJournalEntryNumber(
                TENANT, COMPANY))
                .thenReturn("JE-00010");

        when(entries.save(any(JournalEntry.class)))
                .thenAnswer(invocation -> {
                    JournalEntry entry = invocation.getArgument(0);
                    ReflectionTestUtils.setField(entry, "id", 90L);
                    return entry;
                });

        Long journalId = service.postRegisteredNote(
                TENANT, NOTE);

        assertEquals(90L, journalId);

        verify(entries).save(argThat(entry ->
                "CUSTOMER_CREDIT_NOTE".equals(
                        entry.getReferenceType())
                        && NOTE.equals(entry.getReferenceId())
                        && entry.isPosted()
                        && entry.getTotalDebit().compareTo(
                        bd("122.00")) == 0
                        && entry.getTotalCredit().compareTo(
                        bd("122.00")) == 0
        ));

        verify(entryLines).saveAll(argThat(rows -> {
            List<JournalEntryLine> saved = new ArrayList<>();
            rows.forEach(saved::add);

            if (saved.size() != 3) {
                return false;
            }

            JournalEntryLine revenue = saved.get(0);
            JournalEntryLine vat = saved.get(1);
            JournalEntryLine receivables = saved.get(2);

            return Long.valueOf(4100L).equals(
                    revenue.getAccountId())
                    && revenue.getDebitAmount()
                    .compareTo(bd("100.00")) == 0
                    && revenue.getCreditAmount()
                    .compareTo(bd("0.00")) == 0

                    && Long.valueOf(2210L).equals(
                    vat.getAccountId())
                    && vat.getDebitAmount()
                    .compareTo(bd("22.00")) == 0
                    && vat.getCreditAmount()
                    .compareTo(bd("0.00")) == 0

                    && Long.valueOf(1210L).equals(
                    receivables.getAccountId())
                    && receivables.getDebitAmount()
                    .compareTo(bd("0.00")) == 0
                    && receivables.getCreditAmount()
                    .compareTo(bd("122.00")) == 0;
        }));
    }

    @Test
    void rejectsDraftNote() {
        when(notes.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, NOTE))
                .thenReturn(Optional.of(note("DRAFT")));

        assertThrows(BusinessException.class,
                () -> service.postRegisteredNote(TENANT, NOTE));

        verify(entries, never()).save(any());
        verifyNoInteractions(entryLines);
    }

    @Test
    void rejectsDuplicateAccounting() {
        mockValidDocument();

        when(entries
                .existsByTenantIdAndCompanyIdAndReferenceTypeAndReferenceId(
                        TENANT,
                        COMPANY,
                        "CUSTOMER_CREDIT_NOTE",
                        NOTE))
                .thenReturn(true);

        assertThrows(BusinessException.class,
                () -> service.postRegisteredNote(TENANT, NOTE));

        verify(entries, never()).save(any());
        verifyNoInteractions(entryLines);
    }

    @Test
    void rejectsInvoiceOutsideActiveCompany() {
        when(notes.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, NOTE))
                .thenReturn(Optional.of(note("REGISTERED")));

        when(invoices.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, INVOICE))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> service.postRegisteredNote(TENANT, NOTE));

        verify(entries, never()).save(any());
        verifyNoInteractions(entryLines);
    }
}
