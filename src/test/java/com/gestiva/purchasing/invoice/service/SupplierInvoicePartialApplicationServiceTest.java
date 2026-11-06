package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierInvoicePartialApplicationServiceTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long RECEIPT = 302L;

    @Mock
    SupplierInvoicePartialGenerationGuard guard;

    @Mock
    CompanyContext companyContext;

    @Mock
    SupplierInvoicePartialDraftCreationService creation;

    @InjectMocks
    SupplierInvoicePartialApplicationService service;

    @Test
    void featureGateRunsBeforeAnyOtherOperation() {

        doThrow(new BusinessException(
                "Fatturazione parziale non disponibile."
        )).when(guard).requireEnabled();

        BusinessException error = assertThrows(
                BusinessException.class,
                () -> service.create(TENANT, RECEIPT)
        );

        assertTrue(
                error.getMessage().contains("non disponibile")
        );

        verify(guard).requireEnabled();

        verifyNoInteractions(
                companyContext,
                creation
        );
    }

    @Test
    void missingTenantIsRejectedBeforeCompanyAccess() {

        assertThrows(
                BusinessException.class,
                () -> service.create(null, RECEIPT)
        );

        verify(guard).requireEnabled();

        verifyNoInteractions(
                companyContext,
                creation
        );
    }

    @Test
    void invalidReceiptIsRejectedBeforeCompanyAccess() {

        assertThrows(
                BusinessException.class,
                () -> service.create(TENANT, 0L)
        );

        verify(guard).requireEnabled();

        verifyNoInteractions(
                companyContext,
                creation
        );
    }

    @Test
    void missingCompanyBlocksCreation() {

        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new BusinessException(
                        "Selezionare una società attiva."
                ));

        BusinessException error = assertThrows(
                BusinessException.class,
                () -> service.create(TENANT, RECEIPT)
        );

        assertTrue(
                error.getMessage().contains("società attiva")
        );

        InOrder order = inOrder(
                guard,
                companyContext
        );

        order.verify(guard).requireEnabled();
        order.verify(companyContext)
                .requireCurrentCompanyId();

        verifyNoInteractions(creation);
    }

    @Test
    void invalidCompanyBlocksCreation() {

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(0L);

        assertThrows(
                BusinessException.class,
                () -> service.create(TENANT, RECEIPT)
        );

        verify(guard).requireEnabled();
        verify(companyContext).requireCurrentCompanyId();

        verifyNoInteractions(creation);
    }

    @Test
    void successfulRequestDelegatesToCoordinator() {

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        when(creation.create(TENANT, RECEIPT))
                .thenReturn(501L);

        Long invoiceId = service.create(TENANT, RECEIPT);

        assertEquals(501L, invoiceId);

        InOrder order = inOrder(
                guard,
                companyContext,
                creation
        );

        order.verify(guard).requireEnabled();

        order.verify(companyContext)
                .requireCurrentCompanyId();

        order.verify(creation)
                .create(TENANT, RECEIPT);

        order.verifyNoMoreInteractions();
    }

    @Test
    void coordinatorFailurePropagatesWithoutRetry() {

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        when(creation.create(TENANT, RECEIPT))
                .thenThrow(new BusinessException(
                        "Esiste già una fattura per questa ricezione."
                ));

        BusinessException error = assertThrows(
                BusinessException.class,
                () -> service.create(TENANT, RECEIPT)
        );

        assertTrue(
                error.getMessage().contains(
                        "Esiste già una fattura"
                )
        );

        verify(creation, times(1))
                .create(TENANT, RECEIPT);
    }

    @Test
    void applicationMethodUsesRequiredReadCommittedTransaction()
            throws Exception {

        Method method =
                SupplierInvoicePartialApplicationService.class
                        .getMethod(
                                "create",
                                Long.class,
                                Long.class
                        );

        Transactional transactional =
                method.getAnnotation(Transactional.class);

        assertNotNull(transactional);

        assertEquals(
                Isolation.READ_COMMITTED,
                transactional.isolation()
        );

        assertEquals(
                Propagation.REQUIRED,
                transactional.propagation()
        );

        assertArrayEquals(
                new Class<?>[]{Exception.class},
                transactional.rollbackFor()
        );
    }
}
