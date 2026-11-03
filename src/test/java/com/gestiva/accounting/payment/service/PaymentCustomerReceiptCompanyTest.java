package com.gestiva.accounting.payment.service;

import com.gestiva.accounting.due.entity.PaymentDue;
import com.gestiva.accounting.due.repository.PaymentDueRepository;
import com.gestiva.accounting.payment.entity.PaymentTransaction;
import com.gestiva.accounting.payment.repository.PaymentTransactionRepository;
import com.gestiva.accounting.payment.web.PaymentRegistrationForm;
import com.gestiva.security.company.context.CompanyContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCustomerReceiptCompanyTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;
    private static final Long DUE_ID = 77L;

    @Mock CompanyContext companyContext;
    @Mock PaymentDueRepository paymentDueRepository;
    @Mock PaymentTransactionRepository paymentTransactionRepository;
    @Mock PaymentAccountingService paymentAccountingService;

    @InjectMocks PaymentRegistrationService service;

    @Test
    void shouldRegisterPartialCustomerReceiptInActiveCompany() {

        LocalDate paymentDate = LocalDate.of(2026, 11, 3);

        PaymentRegistrationForm form =
                mock(PaymentRegistrationForm.class);

        when(form.getPaymentDueId()).thenReturn(DUE_ID);
        when(form.getAmount()).thenReturn("40.00");
        when(form.getPaymentDate()).thenReturn(paymentDate);
        when(form.getFinancialAccountId()).thenReturn(500L);

        PaymentDue due = mock(PaymentDue.class);

        when(due.getDirection()).thenReturn("RECEIVABLE");
        when(due.getCompanyId()).thenReturn(COMPANY_ID);
        when(due.getId()).thenReturn(DUE_ID);
        when(due.getGrossAmount())
                .thenReturn(new BigDecimal("100.00"));
        when(due.getPaidAmount())
                .thenReturn(BigDecimal.ZERO);
        when(due.getOpenAmount())
                .thenReturn(new BigDecimal("100.00"));

        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(paymentDueRepository
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        TENANT_ID, COMPANY_ID, DUE_ID))
                .thenReturn(Optional.of(due));

        when(paymentTransactionRepository.save(
                any(PaymentTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(paymentAccountingService.createAccountingEntryForPayment(
                eq(TENANT_ID),
                same(due),
                eq(paymentDate),
                eq(new BigDecimal("40.00")),
                eq(500L),
                any(),
                any()))
                .thenReturn(900L);

        service.register(TENANT_ID, form);

        ArgumentCaptor<PaymentTransaction> captor =
                ArgumentCaptor.forClass(PaymentTransaction.class);

        verify(paymentTransactionRepository, times(2))
                .save(captor.capture());

        PaymentTransaction tx = captor.getValue();

        assertEquals(TENANT_ID, tx.getTenantId());
        assertEquals(COMPANY_ID, tx.getCompanyId());
        assertEquals(DUE_ID, tx.getPaymentDueId());
        assertEquals("IN", tx.getDirection());
        assertEquals(0,
                new BigDecimal("40.00").compareTo(tx.getAmount()));
        assertEquals(900L, tx.getJournalEntryId());

        verify(due).setPaidAmount(new BigDecimal("40.00"));
        verify(due).setOpenAmount(new BigDecimal("60.00"));
        verify(due).setStatus("PARTIALLY_PAID");

        verify(paymentDueRepository).save(due);

        verify(paymentAccountingService)
                .createAccountingEntryForPayment(
                        eq(TENANT_ID),
                        same(due),
                        eq(paymentDate),
                        eq(new BigDecimal("40.00")),
                        eq(500L),
                        any(),
                        any());
    }

    @Test
    void shouldSetPaidStatusAfterFinalCustomerReceipt() {

        LocalDate paymentDate = LocalDate.of(2026, 11, 3);

        PaymentRegistrationForm form =
                mock(PaymentRegistrationForm.class);

        when(form.getPaymentDueId()).thenReturn(DUE_ID);
        when(form.getAmount()).thenReturn("60.00");
        when(form.getPaymentDate()).thenReturn(paymentDate);
        when(form.getFinancialAccountId()).thenReturn(500L);

        PaymentDue due = mock(PaymentDue.class);

        when(due.getDirection()).thenReturn("RECEIVABLE");
        when(due.getCompanyId()).thenReturn(COMPANY_ID);
        when(due.getId()).thenReturn(DUE_ID);
        when(due.getGrossAmount())
                .thenReturn(new BigDecimal("100.00"));
        when(due.getPaidAmount())
                .thenReturn(new BigDecimal("40.00"));
        when(due.getOpenAmount())
                .thenReturn(new BigDecimal("60.00"));

        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(paymentDueRepository
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        TENANT_ID, COMPANY_ID, DUE_ID))
                .thenReturn(Optional.of(due));

        when(paymentTransactionRepository.save(
                any(PaymentTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(paymentAccountingService.createAccountingEntryForPayment(
                eq(TENANT_ID),
                same(due),
                eq(paymentDate),
                eq(new BigDecimal("60.00")),
                eq(500L),
                any(),
                any()))
                .thenReturn(901L);

        service.register(TENANT_ID, form);

        ArgumentCaptor<PaymentTransaction> captor =
                ArgumentCaptor.forClass(PaymentTransaction.class);

        verify(paymentTransactionRepository, times(2))
                .save(captor.capture());

        PaymentTransaction tx = captor.getValue();

        assertEquals(TENANT_ID, tx.getTenantId());
        assertEquals(COMPANY_ID, tx.getCompanyId());
        assertEquals(DUE_ID, tx.getPaymentDueId());
        assertEquals("IN", tx.getDirection());

        assertEquals(0,
                new BigDecimal("60.00")
                        .compareTo(tx.getAmount()));

        assertEquals(901L, tx.getJournalEntryId());

        verify(due).setPaidAmount(
                new BigDecimal("100.00"));

        verify(due).setOpenAmount(
                new BigDecimal("0.00"));

        verify(due).setStatus("PAID");

        verify(paymentDueRepository).save(due);

        verify(paymentAccountingService)
                .createAccountingEntryForPayment(
                        eq(TENANT_ID),
                        same(due),
                        eq(paymentDate),
                        eq(new BigDecimal("60.00")),
                        eq(500L),
                        any(),
                        any());
    }

}
