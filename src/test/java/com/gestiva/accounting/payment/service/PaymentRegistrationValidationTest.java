package com.gestiva.accounting.payment.service;

import com.gestiva.accounting.due.entity.PaymentDue;
import com.gestiva.accounting.due.repository.PaymentDueRepository;
import com.gestiva.accounting.payment.repository.PaymentTransactionRepository;
import com.gestiva.accounting.payment.web.PaymentRegistrationForm;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentRegistrationValidationTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;
    private static final Long DUE_ID = 77L;

    @Mock CompanyContext companyContext;
    @Mock PaymentDueRepository paymentDueRepository;
    @Mock PaymentTransactionRepository paymentTransactionRepository;
    @Mock PaymentAccountingService paymentAccountingService;

    @InjectMocks PaymentRegistrationService service;

    private PaymentDue prepareDue() {
        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        PaymentDue due = mock(PaymentDue.class);

        when(paymentDueRepository
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        TENANT_ID, COMPANY_ID, DUE_ID))
                .thenReturn(Optional.of(due));

        return due;
    }

    private PaymentRegistrationForm form(String amount) {
        PaymentRegistrationForm form =
                mock(PaymentRegistrationForm.class);

        when(form.getPaymentDueId()).thenReturn(DUE_ID);

        if (amount != null) {
            when(form.getAmount()).thenReturn(amount);
        }

        return form;
    }

    private void assertNoWrites() {
        verify(paymentDueRepository, never()).save(any());
        verifyNoInteractions(
                paymentTransactionRepository,
                paymentAccountingService
        );
    }

    @Test
    void shouldRejectAlreadyPaidDue() {
        PaymentDue due = prepareDue();
        when(due.getStatus()).thenReturn("PAID");

        assertThrows(BusinessException.class,
                () -> service.register(TENANT_ID, form(null)));

        assertNoWrites();
    }

    @Test
    void shouldRejectCancelledDue() {
        PaymentDue due = prepareDue();
        when(due.getStatus()).thenReturn("CANCELLED");

        assertThrows(BusinessException.class,
                () -> service.register(TENANT_ID, form(null)));

        assertNoWrites();
    }

    @Test
    void shouldRejectAmountGreaterThanOpenBalance() {
        PaymentDue due = prepareDue();

        when(due.getGrossAmount())
                .thenReturn(new BigDecimal("100.00"));

        when(due.getOpenAmount())
                .thenReturn(new BigDecimal("50.00"));

        assertThrows(BusinessException.class,
                () -> service.register(TENANT_ID, form("60.00")));

        assertNoWrites();
    }

    @Test
    void shouldRejectZeroAmount() {
        prepareDue();

        assertThrows(BusinessException.class,
                () -> service.register(TENANT_ID, form("0.00")));

        assertNoWrites();
    }

    @Test
    void shouldRejectNegativeAmount() {
        prepareDue();

        assertThrows(BusinessException.class,
                () -> service.register(TENANT_ID, form("-10.00")));

        assertNoWrites();
    }
}
