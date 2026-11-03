package com.gestiva.accounting.payment.service;

import com.gestiva.accounting.due.repository.PaymentDueRepository;
import com.gestiva.accounting.due.repository.PaymentDueTransactionRepository;
import com.gestiva.accounting.due.service.PaymentDueRegistrationService;
import com.gestiva.accounting.due.web.PaymentDueRegistrationForm;
import com.gestiva.accounting.payment.repository.PaymentTransactionRepository;
import com.gestiva.accounting.payment.web.PaymentRegistrationForm;
import com.gestiva.accounting.v2.journal.service.JournalAutoPostingService;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.security.company.context.CompanyContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentRegistrationMultiCompanyIsolationTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;
    private static final Long DUE_ID = 77L;

    @Mock CompanyContext companyContext;
    @Mock PaymentDueRepository paymentDueRepository;
    @Mock PaymentTransactionRepository paymentTransactionRepository;
    @Mock PaymentDueTransactionRepository paymentDueTransactionRepository;
    @Mock PaymentAccountingService paymentAccountingService;
    @Mock JournalAutoPostingService journalAutoPostingService;

    @InjectMocks PaymentRegistrationService paymentRegistrationService;
    @InjectMocks PaymentDueRegistrationService paymentDueRegistrationService;

    @Test
    void paymentRegistrationRejectsMissingCompanyBeforeAnyWrite() {
        when(companyContext.resolveCurrentCompanyId()).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> paymentRegistrationService.register(
                        TENANT_ID, mock(PaymentRegistrationForm.class)));

        verifyNoInteractions(
                paymentDueRepository,
                paymentTransactionRepository,
                paymentDueTransactionRepository,
                paymentAccountingService,
                journalAutoPostingService);
    }

    @Test
    void paymentRegistrationRejectsForeignCompanyDueBeforeAnyWrite() {
        when(companyContext.resolveCurrentCompanyId()).thenReturn(COMPANY_ID);

        PaymentRegistrationForm form = mock(PaymentRegistrationForm.class);
        when(form.getPaymentDueId()).thenReturn(DUE_ID);

        when(paymentDueRepository
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        TENANT_ID, COMPANY_ID, DUE_ID))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> paymentRegistrationService.register(TENANT_ID, form));

        verify(paymentDueRepository)
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        TENANT_ID, COMPANY_ID, DUE_ID);

        verify(paymentDueRepository, never()).save(any());
        verifyNoInteractions(
                paymentTransactionRepository,
                paymentDueTransactionRepository,
                paymentAccountingService,
                journalAutoPostingService);
    }

    @Test
    void dueRegistrationRejectsMissingCompanyBeforeAnyWrite() {
        when(companyContext.resolveCurrentCompanyId()).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> paymentDueRegistrationService.registerMovement(
                        TENANT_ID, DUE_ID,
                        mock(PaymentDueRegistrationForm.class)));

        verifyNoInteractions(
                paymentDueRepository,
                paymentTransactionRepository,
                paymentDueTransactionRepository,
                paymentAccountingService,
                journalAutoPostingService);
    }

    @Test
    void dueRegistrationRejectsForeignCompanyDueBeforeAnyWrite() {
        when(companyContext.resolveCurrentCompanyId()).thenReturn(COMPANY_ID);

        when(paymentDueRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID, COMPANY_ID, DUE_ID))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> paymentDueRegistrationService.registerMovement(
                        TENANT_ID, DUE_ID,
                        mock(PaymentDueRegistrationForm.class)));

        verify(paymentDueRepository)
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, DUE_ID);

        verify(paymentDueRepository, never()).save(any());
        verifyNoInteractions(
                paymentTransactionRepository,
                paymentDueTransactionRepository,
                paymentAccountingService,
                journalAutoPostingService);
    }
}
