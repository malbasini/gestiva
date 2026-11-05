package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialPreparationTransactionIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long RECEIPT = 301L;
    private static final Long ORDER = 100L;
    private static final Long SUPPLIER = 40L;

    @Autowired
    private SupplierInvoicePartialPreparationService service;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private SupplierInvoicePartialGenerationGuard guard;

    @MockitoBean
    private CompanyContext companyContext;

    @MockitoBean
    private GoodsReceiptRepository receiptRepository;

    @MockitoBean
    private GoodsReceiptLineRepository receiptLineRepository;

    @MockitoBean
    private PurchaseOrderRepository orderRepository;

    @MockitoBean
    private SupplierInvoiceRepository invoiceRepository;

    private void verifyTestDatabase() throws Exception {
        try (Connection connection =
                     jdbc.getDataSource().getConnection()) {

            assertEquals(
                    "gestiva_test",
                    connection.getCatalog()
            );

            String username =
                    connection.getMetaData().getUserName();

            assertTrue(
                    username.equals("gestiva_test_runner")
                    || username.startsWith("gestiva_test_runner@")
            );
        }
    }

    private TransactionTemplate transaction() {
        TransactionTemplate tx =
                new TransactionTemplate(transactionManager);

        tx.setReadOnly(false);
        tx.setIsolationLevel(
                TransactionDefinition.ISOLATION_READ_COMMITTED
        );

        return tx;
    }

    private void configureSuccessfulPreparation() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setTenantId(TENANT);
        receipt.setCompanyId(COMPANY);
        receipt.setPurchaseOrderId(ORDER);
        receipt.setSupplierId(SUPPLIER);

        when(receiptRepository.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, RECEIPT
        )).thenReturn(Optional.of(receipt));

        PurchaseOrder order = new PurchaseOrder();
        order.setStatus("CONFIRMED");
        order.setSupplierId(SUPPLIER);

        when(orderRepository.findLockedByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, ORDER
        )).thenReturn(Optional.of(order));

        when(receiptLineRepository
                .findByTenantIdAndCompanyIdAndGoodsReceiptIdOrderByLineNoAsc(
                        TENANT, COMPANY, RECEIPT
                )).thenReturn(List.of(new GoodsReceiptLine()));
    }

    @Test
    void proxyRejectsInvocationWithoutTransaction()
            throws Exception {

        verifyTestDatabase();

        assertFalse(
                TransactionSynchronizationManager
                        .isActualTransactionActive()
        );

        assertThrows(
                IllegalTransactionStateException.class,
                () -> service.prepare(TENANT, RECEIPT)
        );

        verifyNoInteractions(
                guard,
                companyContext,
                receiptRepository,
                receiptLineRepository,
                orderRepository,
                invoiceRepository
        );
    }

    @Test
    void preparationSharesExistingReadCommittedTransaction()
            throws Exception {

        verifyTestDatabase();
        configureSuccessfulPreparation();

        AtomicReference<Long> lockConnection =
                new AtomicReference<>();

        AtomicReference<Long> duplicateConnection =
                new AtomicReference<>();

        AtomicReference<String> lockTransaction =
                new AtomicReference<>();

        when(orderRepository.findLockedByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, ORDER
        )).thenAnswer(invocation -> {

            assertTrue(
                    TransactionSynchronizationManager
                            .isActualTransactionActive()
            );

            lockTransaction.set(
                    TransactionSynchronizationManager
                            .getCurrentTransactionName()
            );

            lockConnection.set(
                    jdbc.queryForObject(
                            "SELECT CONNECTION_ID()",
                            Long.class
                    )
            );

            PurchaseOrder order = new PurchaseOrder();
            order.setStatus("CONFIRMED");
            order.setSupplierId(SUPPLIER);

            return Optional.of(order);
        });

        when(invoiceRepository
                .existsByTenantIdAndCompanyIdAndGoodsReceiptId(
                        TENANT, COMPANY, RECEIPT
                )).thenAnswer(invocation -> {

            assertTrue(
                    TransactionSynchronizationManager
                            .isActualTransactionActive()
            );

            duplicateConnection.set(
                    jdbc.queryForObject(
                            "SELECT CONNECTION_ID()",
                            Long.class
                    )
            );

            return false;
        });

        transaction().execute(status -> {

            assertTrue(
                    TransactionSynchronizationManager
                            .isActualTransactionActive()
            );

            var result = service.prepare(TENANT, RECEIPT);

            assertEquals(COMPANY, result.companyId());
            assertEquals(1, result.receiptLines().size());

            status.setRollbackOnly();
            return null;
        });

        assertNotNull(lockConnection.get());
        assertNotNull(duplicateConnection.get());

        assertEquals(
                lockConnection.get(),
                duplicateConnection.get()
        );

        // TransactionTemplate può creare transazioni anonime.
        // La validità della transazione è verificata tramite
        // isActualTransactionActive() e connessione JDBC condivisa.
        assertTrue(
                lockConnection.get() > 0,
                "Connessione JDBC non valida."
        );

        var order = inOrder(
                guard,
                companyContext,
                receiptRepository,
                orderRepository,
                invoiceRepository,
                receiptLineRepository
        );

        order.verify(guard).requireEnabled();
        order.verify(companyContext).requireCurrentCompanyId();

        order.verify(receiptRepository)
                .findByTenantIdAndCompanyIdAndId(
                        TENANT, COMPANY, RECEIPT
                );

        order.verify(orderRepository)
                .findLockedByTenantIdAndCompanyIdAndId(
                        TENANT, COMPANY, ORDER
                );

        order.verify(invoiceRepository)
                .existsByTenantIdAndCompanyIdAndGoodsReceiptId(
                        TENANT, COMPANY, RECEIPT
                );

        order.verify(receiptLineRepository)
                .findByTenantIdAndCompanyIdAndGoodsReceiptIdOrderByLineNoAsc(
                        TENANT, COMPANY, RECEIPT
                );
    }

    @Test
    void duplicateAfterLockStopsWithoutWrites()
            throws Exception {

        verifyTestDatabase();
        configureSuccessfulPreparation();

        when(invoiceRepository
                .existsByTenantIdAndCompanyIdAndGoodsReceiptId(
                        TENANT, COMPANY, RECEIPT
                )).thenReturn(true);

        transaction().execute(status -> {
            assertThrows(
                    BusinessException.class,
                    () -> service.prepare(TENANT, RECEIPT)
            );

            status.setRollbackOnly();
            return null;
        });

        verify(orderRepository)
                .findLockedByTenantIdAndCompanyIdAndId(
                        TENANT, COMPANY, ORDER
                );

        verify(invoiceRepository)
                .existsByTenantIdAndCompanyIdAndGoodsReceiptId(
                        TENANT, COMPANY, RECEIPT
                );

        verifyNoInteractions(receiptLineRepository);

        verify(invoiceRepository, never()).save(any());
        verify(receiptRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }
}
