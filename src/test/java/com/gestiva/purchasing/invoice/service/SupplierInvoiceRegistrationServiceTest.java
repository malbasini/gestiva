package com.gestiva.purchasing.invoice.service;

import com.gestiva.accounting.due.service.PaymentDueService;
import com.gestiva.accounting.v2.journal.service.JournalAutoPostingService;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierInvoiceRegistrationServiceTest {

    private static final Long TENANT_ID = 2L;
    private static final Long COMPANY_ID = 5L;
    private static final Long INVOICE_ID = 100L;
    private static final Long SUPPLIER_ID = 20L;

    private static final LocalDate INVOICE_DATE =
            LocalDate.of(2026, 10, 27);

    @Mock
    private SupplierInvoiceRepository supplierInvoiceRepository;

    @Mock
    private CompanyContext companyContext;

    @Mock
    private JournalAutoPostingService journalAutoPostingService;

    @Mock
    private PaymentDueService paymentDueService;

    @InjectMocks
    private SupplierInvoiceService service;

    private SupplierInvoice createInvoice(String status) {

        SupplierInvoice invoice = new SupplierInvoice();

        ReflectionTestUtils.setField(
                invoice, "id", INVOICE_ID
        );

        invoice.setTenantId(TENANT_ID);
        invoice.setCompanyId(COMPANY_ID);
        invoice.setSupplierId(SUPPLIER_ID);
        invoice.setInvoiceNumber("SF-TEST-001");
        invoice.setInvoiceDate(INVOICE_DATE);
        invoice.setCurrencyCode("EUR");
        invoice.setStatus(status);

        invoice.setSubtotalAmount(
                new BigDecimal("100.00")
        );

        invoice.setTaxAmount(
                new BigDecimal("22.00")
        );

        invoice.setTotalAmount(
                new BigDecimal("122.00")
        );

        return invoice;
    }

    private void configureCompanyAndInvoice(
            SupplierInvoice invoice) {

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(supplierInvoiceRepository
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        TENANT_ID,
                        COMPANY_ID,
                        INVOICE_ID
                ))
                .thenReturn(Optional.of(invoice));
    }

    @Test
    void registerSupplierInvoice_shouldRegisterDraft() {

        SupplierInvoice invoice = createInvoice("DRAFT");

        configureCompanyAndInvoice(invoice);

        Long result = service.registerSupplierInvoice(
                TENANT_ID,
                INVOICE_ID
        );

        assertEquals(INVOICE_ID, result);
        assertEquals("REGISTERED", invoice.getStatus());

        InOrder order = inOrder(
                supplierInvoiceRepository,
                journalAutoPostingService,
                paymentDueService
        );

        order.verify(supplierInvoiceRepository)
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        TENANT_ID,
                        COMPANY_ID,
                        INVOICE_ID
                );

        order.verify(supplierInvoiceRepository)
                .save(same(invoice));

        order.verify(journalAutoPostingService)
                .postSupplierInvoice(
                        eq(TENANT_ID),
                        eq(INVOICE_DATE),
                        eq("SF-TEST-001"),
                        eq(new BigDecimal("100.00")),
                        eq(new BigDecimal("22.00")),
                        eq(new BigDecimal("122.00")),
                        eq("EUR"),
                        eq(INVOICE_ID),
                        isNull()
                );

        order.verify(paymentDueService)
                .createPayableFromSupplierInvoice(
                        eq(TENANT_ID),
                        eq(SUPPLIER_ID),
                        eq("SF-TEST-001"),
                        eq(INVOICE_DATE),
                        eq("EUR"),
                        eq(new BigDecimal("122.00")),
                        eq(INVOICE_ID)
                );

        order.verifyNoMoreInteractions();
    }

    @ParameterizedTest
    @ValueSource(strings = {"REGISTERED", "CANCELLED"})
    void registerSupplierInvoice_shouldRejectInvalidStatus(
            String status) {

        SupplierInvoice invoice = createInvoice(status);

        configureCompanyAndInvoice(invoice);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.registerSupplierInvoice(
                        TENANT_ID,
                        INVOICE_ID
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "non è in stato DRAFT"
                )
        );

        assertEquals(status, invoice.getStatus());

        verify(supplierInvoiceRepository, never())
                .save(any());

        verifyNoInteractions(
                journalAutoPostingService,
                paymentDueService
        );
    }

    @Test
    void registerSupplierInvoice_shouldRejectOtherCompany() {

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(supplierInvoiceRepository
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        TENANT_ID,
                        COMPANY_ID,
                        INVOICE_ID
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> service.registerSupplierInvoice(
                        TENANT_ID,
                        INVOICE_ID
                )
        );

        verify(supplierInvoiceRepository, never())
                .save(any());

        verifyNoInteractions(
                journalAutoPostingService,
                paymentDueService
        );
    }

    @Test
    void registerSupplierInvoice_shouldPropagateJournalFailure() {

        SupplierInvoice invoice = createInvoice("DRAFT");
        configureCompanyAndInvoice(invoice);

        doThrow(new BusinessException("Errore contabile simulato"))
                .when(journalAutoPostingService)
                .postSupplierInvoice(
                        eq(TENANT_ID),
                        eq(INVOICE_DATE),
                        eq("SF-TEST-001"),
                        eq(new BigDecimal("100.00")),
                        eq(new BigDecimal("22.00")),
                        eq(new BigDecimal("122.00")),
                        eq("EUR"),
                        eq(INVOICE_ID),
                        isNull()
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.registerSupplierInvoice(
                        TENANT_ID,
                        INVOICE_ID
                )
        );

        assertEquals(
                "Errore contabile simulato",
                exception.getMessage()
        );

        verify(supplierInvoiceRepository)
                .save(same(invoice));

        verify(journalAutoPostingService)
                .postSupplierInvoice(
                        eq(TENANT_ID),
                        eq(INVOICE_DATE),
                        eq("SF-TEST-001"),
                        eq(new BigDecimal("100.00")),
                        eq(new BigDecimal("22.00")),
                        eq(new BigDecimal("122.00")),
                        eq("EUR"),
                        eq(INVOICE_ID),
                        isNull()
                );

        // Se fallisce la contabilizzazione,
        // il servizio non deve creare la scadenza.
        verifyNoInteractions(paymentDueService);
    }

    @Test
    void registerSupplierInvoice_shouldPropagatePaymentDueFailure() {

        SupplierInvoice invoice = createInvoice("DRAFT");
        configureCompanyAndInvoice(invoice);

        doThrow(new BusinessException("Errore scadenza simulato"))
                .when(paymentDueService)
                .createPayableFromSupplierInvoice(
                        eq(TENANT_ID),
                        eq(SUPPLIER_ID),
                        eq("SF-TEST-001"),
                        eq(INVOICE_DATE),
                        eq("EUR"),
                        eq(new BigDecimal("122.00")),
                        eq(INVOICE_ID)
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.registerSupplierInvoice(
                        TENANT_ID,
                        INVOICE_ID
                )
        );

        assertEquals(
                "Errore scadenza simulato",
                exception.getMessage()
        );

        InOrder order = inOrder(
                supplierInvoiceRepository,
                journalAutoPostingService,
                paymentDueService
        );

        order.verify(supplierInvoiceRepository)
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        TENANT_ID,
                        COMPANY_ID,
                        INVOICE_ID
                );

        order.verify(supplierInvoiceRepository)
                .save(same(invoice));

        order.verify(journalAutoPostingService)
                .postSupplierInvoice(
                        eq(TENANT_ID),
                        eq(INVOICE_DATE),
                        eq("SF-TEST-001"),
                        eq(new BigDecimal("100.00")),
                        eq(new BigDecimal("22.00")),
                        eq(new BigDecimal("122.00")),
                        eq("EUR"),
                        eq(INVOICE_ID),
                        isNull()
                );

        order.verify(paymentDueService)
                .createPayableFromSupplierInvoice(
                        eq(TENANT_ID),
                        eq(SUPPLIER_ID),
                        eq("SF-TEST-001"),
                        eq(INVOICE_DATE),
                        eq("EUR"),
                        eq(new BigDecimal("122.00")),
                        eq(INVOICE_ID)
                );

        order.verifyNoMoreInteractions();
    }


    private SupplierInvoiceService createTransactionalProxy(
            org.springframework.transaction.PlatformTransactionManager manager,
            org.springframework.transaction.TransactionStatus status) {

        when(manager.getTransaction(
                any(org.springframework.transaction.TransactionDefinition.class)
        )).thenReturn(status);

        org.springframework.aop.framework.ProxyFactory factory =
                new org.springframework.aop.framework.ProxyFactory(service);

        factory.setProxyTargetClass(true);

        factory.addAdvice(
                new org.springframework.transaction.interceptor.TransactionInterceptor(
                        manager,
                        new org.springframework.transaction.annotation.AnnotationTransactionAttributeSource()
                )
        );

        return (SupplierInvoiceService) factory.getProxy();
    }

    @Test
    void registerSupplierInvoice_transactionShouldCommitOnSuccess() {

        SupplierInvoice invoice = createInvoice("DRAFT");
        configureCompanyAndInvoice(invoice);

        var manager = mock(
                org.springframework.transaction.PlatformTransactionManager.class
        );

        var status = mock(
                org.springframework.transaction.TransactionStatus.class
        );

        SupplierInvoiceService proxy =
                createTransactionalProxy(manager, status);

        Long result = proxy.registerSupplierInvoice(
                TENANT_ID,
                INVOICE_ID
        );

        assertEquals(INVOICE_ID, result);
        assertEquals("REGISTERED", invoice.getStatus());

        verify(manager).commit(status);
        verify(manager, never()).rollback(any());
    }

    @Test
    void registerSupplierInvoice_transactionShouldRollbackOnJournalFailure() {

        SupplierInvoice invoice = createInvoice("DRAFT");
        configureCompanyAndInvoice(invoice);

        doThrow(new BusinessException("Errore contabile simulato"))
                .when(journalAutoPostingService)
                .postSupplierInvoice(
                        eq(TENANT_ID),
                        eq(INVOICE_DATE),
                        eq("SF-TEST-001"),
                        eq(new BigDecimal("100.00")),
                        eq(new BigDecimal("22.00")),
                        eq(new BigDecimal("122.00")),
                        eq("EUR"),
                        eq(INVOICE_ID),
                        isNull()
                );

        var manager = mock(
                org.springframework.transaction.PlatformTransactionManager.class
        );

        var status = mock(
                org.springframework.transaction.TransactionStatus.class
        );

        SupplierInvoiceService proxy =
                createTransactionalProxy(manager, status);

        assertThrows(
                BusinessException.class,
                () -> proxy.registerSupplierInvoice(
                        TENANT_ID,
                        INVOICE_ID
                )
        );

        verify(manager).rollback(status);
        verify(manager, never()).commit(any());

        verifyNoInteractions(paymentDueService);
    }

    @Test
    void registerSupplierInvoice_transactionShouldRollbackOnDueFailure() {

        SupplierInvoice invoice = createInvoice("DRAFT");
        configureCompanyAndInvoice(invoice);

        doThrow(new BusinessException("Errore scadenza simulato"))
                .when(paymentDueService)
                .createPayableFromSupplierInvoice(
                        eq(TENANT_ID),
                        eq(SUPPLIER_ID),
                        eq("SF-TEST-001"),
                        eq(INVOICE_DATE),
                        eq("EUR"),
                        eq(new BigDecimal("122.00")),
                        eq(INVOICE_ID)
                );

        var manager = mock(
                org.springframework.transaction.PlatformTransactionManager.class
        );

        var status = mock(
                org.springframework.transaction.TransactionStatus.class
        );

        SupplierInvoiceService proxy =
                createTransactionalProxy(manager, status);

        assertThrows(
                BusinessException.class,
                () -> proxy.registerSupplierInvoice(
                        TENANT_ID,
                        INVOICE_ID
                )
        );

        verify(journalAutoPostingService).postSupplierInvoice(
                eq(TENANT_ID),
                eq(INVOICE_DATE),
                eq("SF-TEST-001"),
                eq(new BigDecimal("100.00")),
                eq(new BigDecimal("22.00")),
                eq(new BigDecimal("122.00")),
                eq("EUR"),
                eq(INVOICE_ID),
                isNull()
        );

        verify(manager).rollback(status);
        verify(manager, never()).commit(any());
    }

}
