package com.gestiva.accounting.payment.service;

import com.gestiva.accounting.due.entity.PaymentDue;
import com.gestiva.accounting.due.repository.PaymentDueRepository;
import com.gestiva.accounting.payment.entity.PaymentTransaction;
import com.gestiva.accounting.payment.repository.PaymentTransactionRepository;
import com.gestiva.accounting.payment.web.PaymentRegistrationForm;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentRegistrationCompensationV10Test {

    private PaymentDueRepository dues;
    private PaymentTransactionRepository transactions;
    private PaymentAccountingService accounting;
    private CompanyContext companies;
    private PaymentRegistrationService service;

    @BeforeEach
    void setup() {
        dues = mock(PaymentDueRepository.class);
        transactions = mock(PaymentTransactionRepository.class);
        accounting = mock(PaymentAccountingService.class);
        companies = mock(CompanyContext.class);

        service = new PaymentRegistrationService(
                dues, transactions, accounting, companies);

        when(companies.resolveCurrentCompanyId()).thenReturn(3L);
    }

    private PaymentDue due(String gross, String paid,
                           String compensated, String open) {
        PaymentDue value = new PaymentDue();
        value.setTenantId(2L);
        value.setCompanyId(3L);
        value.setDirection("PAYABLE");
        value.setPartyType("SUPPLIER");
        value.setPartyId(11L);
        value.setReferenceType("SUPPLIER_INVOICE");
        value.setReferenceId(36L);
        value.setCurrencyCode("EUR");
        value.setStatus("PARTIALLY_PAID");
        value.setGrossAmount(new BigDecimal(gross));
        value.setPaidAmount(new BigDecimal(paid));
        value.setCompensatedAmount(new BigDecimal(compensated));
        value.setOpenAmount(new BigDecimal(open));
        return value;
    }

    private PaymentRegistrationForm form(String amount) {
        PaymentRegistrationForm value = new PaymentRegistrationForm();
        value.setPaymentDueId(50L);
        value.setPaymentDate(LocalDate.of(2026, 11, 10));
        value.setAmount(amount);
        value.setPaymentMethod("BONIFICO");
        value.setReference("TEST-V10");
        value.setNotes("Test regressione compensazioni");
        value.setFinancialAccountId(100L);
        return value;
    }

    private void setupSuccessfulRegistration(PaymentDue due) {
        when(dues.findByTenantIdAndCompanyIdAndIdForUpdate(
                2L, 3L, 50L)).thenReturn(Optional.of(due));

        when(transactions.save(any(PaymentTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(accounting.createAccountingEntryForPayment(
                eq(2L), same(due), any(), any(),
                eq(100L), any(), any()))
                .thenReturn(77L);
    }

    @Test
    void paymentAfterCompensationPreservesCorrectOutstandingBalance() {
        PaymentDue due = due("100.00", "40.00", "20.00", "40.00");
        setupSuccessfulRegistration(due);

        service.register(2L, form("15.00"));

        assertEquals(0, due.getPaidAmount()
                .compareTo(new BigDecimal("55.00")));
        assertEquals(0, due.getCompensatedAmount()
                .compareTo(new BigDecimal("20.00")));
        assertEquals(0, due.getOpenAmount()
                .compareTo(new BigDecimal("25.00")));
        assertEquals("PARTIALLY_PAID", due.getStatus());

        verify(dues).save(same(due));
        verify(accounting).createAccountingEntryForPayment(
                eq(2L), same(due), any(),
                eq(new BigDecimal("15.00")),
                eq(100L), any(), any());
    }

    @Test
    void paymentWithoutCompensationRemainsCompatible() {
        PaymentDue due = due("100.00", "40.00", "0.00", "60.00");
        setupSuccessfulRegistration(due);

        service.register(2L, form("60.00"));

        assertEquals(0, due.getPaidAmount()
                .compareTo(new BigDecimal("100.00")));
        assertEquals(0, due.getCompensatedAmount()
                .compareTo(new BigDecimal("0.00")));
        assertEquals(0, due.getOpenAmount()
                .compareTo(new BigDecimal("0.00")));
        assertEquals("PAID", due.getStatus());
    }

    @Test
    void rejectsInconsistentDueWithoutWriting() {
        PaymentDue due = due("100.00", "40.00", "20.00", "45.00");

        when(dues.findByTenantIdAndCompanyIdAndIdForUpdate(
                2L, 3L, 50L)).thenReturn(Optional.of(due));

        assertThrows(BusinessException.class,
                () -> service.register(2L, form("5.00")));

        verifyNoInteractions(transactions, accounting);
        verify(dues, never()).save(any());
    }

    @Test
    void rejectsPaymentExceedingNetOutstanding() {
        PaymentDue due = due("100.00", "40.00", "20.00", "40.00");

        when(dues.findByTenantIdAndCompanyIdAndIdForUpdate(
                2L, 3L, 50L)).thenReturn(Optional.of(due));

        assertThrows(BusinessException.class,
                () -> service.register(2L, form("40.01")));

        verifyNoInteractions(transactions, accounting);
        verify(dues, never()).save(any());
    }
}
