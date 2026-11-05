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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialCoordinatorTransactionIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long RECEIPT = 301L;
    private static final Long ORDER = 100L;
    private static final Long SUPPLIER = 40L;

    @Autowired
    private SupplierInvoicePartialCoordinatorService coordinator;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private SupplierInvoicePartialGenerationGuard featureGuard;

    @MockitoBean
    private CompanyContext companyContext;

    @MockitoBean
    private GoodsReceiptRepository receipts;

    @MockitoBean
    private GoodsReceiptLineRepository receiptLines;

    @MockitoBean
    private PurchaseOrderRepository orders;

    @MockitoBean
    private SupplierInvoiceRepository invoices;

    @MockitoBean
    private SupplierInvoicePartialDraftAssembler assembler;

    private void verifyTestDatabase() throws Exception {
        try (Connection connection =
                     jdbc.getDataSource().getConnection()) {
            assertEquals("gestiva_test", connection.getCatalog());

            String username = connection.getMetaData().getUserName();
            assertTrue(
                    username.equals("gestiva_test_runner")
                    || username.startsWith("gestiva_test_runner@")
            );
        }
    }

    private Long connectionId() {
        return jdbc.queryForObject(
                "SELECT CONNECTION_ID()",
                Long.class
        );
    }

    private void assertReadCommittedWritable() {
        assertTrue(
                TransactionSynchronizationManager
                        .isActualTransactionActive()
        );

        assertFalse(
                TransactionSynchronizationManager
                        .isCurrentTransactionReadOnly()
        );

        Integer isolation = jdbc.execute(
                (org.springframework.jdbc.core.ConnectionCallback<Integer>)
                        Connection::getTransactionIsolation
        );

        assertEquals(
                Connection.TRANSACTION_READ_COMMITTED,
                isolation
        );
    }

    private void configureSuccessfulPreparation(
            AtomicReference<Long> connectionDuringOrderLock) {

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setTenantId(TENANT);
        receipt.setCompanyId(COMPANY);
        receipt.setPurchaseOrderId(ORDER);
        receipt.setSupplierId(SUPPLIER);

        when(receipts.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, RECEIPT
        )).thenReturn(Optional.of(receipt));

        when(orders.findLockedByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, ORDER
        )).thenAnswer(invocation -> {
            assertReadCommittedWritable();
            connectionDuringOrderLock.set(connectionId());

            PurchaseOrder order = new PurchaseOrder();
            order.setStatus("CONFIRMED");
            order.setSupplierId(SUPPLIER);
            return Optional.of(order);
        });

        when(invoices.existsByTenantIdAndCompanyIdAndGoodsReceiptId(
                TENANT, COMPANY, RECEIPT
        )).thenReturn(false);

        when(receiptLines
                .findByTenantIdAndCompanyIdAndGoodsReceiptIdOrderByLineNoAsc(
                        TENANT, COMPANY, RECEIPT
                )).thenReturn(List.of(new GoodsReceiptLine()));
    }

    private TransactionTemplate outerTransaction(
            int isolation,
            boolean readOnly) {

        TransactionTemplate tx =
                new TransactionTemplate(transactionManager);

        tx.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );
        tx.setIsolationLevel(isolation);
        tx.setReadOnly(readOnly);
        return tx;
    }

    @Test
    void coordinatorStartsReadCommittedAndSharesConnection()
            throws Exception {

        verifyTestDatabase();

        assertFalse(
                TransactionSynchronizationManager
                        .isActualTransactionActive()
        );

        AtomicReference<Long> orderConnection =
                new AtomicReference<>();
        AtomicReference<Long> assemblyConnection =
                new AtomicReference<>();

        configureSuccessfulPreparation(orderConnection);

        SupplierInvoicePartialDraftPlan expected =
                mock(SupplierInvoicePartialDraftPlan.class);

        when(assembler.assemble(any())).thenAnswer(invocation -> {
            assertReadCommittedWritable();
            assemblyConnection.set(connectionId());
            return expected;
        });

        SupplierInvoicePartialDraftPlan actual =
                coordinator.plan(TENANT, RECEIPT);

        assertSame(expected, actual);

        assertNotNull(orderConnection.get());
        assertNotNull(assemblyConnection.get());

        assertEquals(
                orderConnection.get(),
                assemblyConnection.get()
        );

        assertFalse(
                TransactionSynchronizationManager
                        .isActualTransactionActive()
        );

        var order = inOrder(
                featureGuard,
                companyContext,
                receipts,
                orders,
                invoices,
                receiptLines,
                assembler
        );

        order.verify(featureGuard).requireEnabled();
        order.verify(companyContext).requireCurrentCompanyId();
        order.verify(receipts).findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, RECEIPT
        );
        order.verify(orders)
                .findLockedByTenantIdAndCompanyIdAndId(
                        TENANT, COMPANY, ORDER
                );
        order.verify(invoices)
                .existsByTenantIdAndCompanyIdAndGoodsReceiptId(
                        TENANT, COMPANY, RECEIPT
                );
        order.verify(receiptLines)
                .findByTenantIdAndCompanyIdAndGoodsReceiptIdOrderByLineNoAsc(
                        TENANT, COMPANY, RECEIPT
                );
        order.verify(assembler).assemble(any());

        System.out.println(
                "COORDINATOR: READ_COMMITTED scrivibile OK"
        );
        System.out.println(
                "COORDINATOR: stessa connessione JDBC OK"
        );
        System.out.println(
                "COORDINATOR: transazione chiusa al ritorno OK"
        );
    }

    private void assertExternalTransactionRejected(
            int isolation,
            boolean readOnly) {

        outerTransaction(isolation, readOnly).execute(status -> {

            assertThrows(
                    BusinessException.class,
                    () -> coordinator.plan(TENANT, RECEIPT)
            );

            status.setRollbackOnly();
            return null;
        });

        verify(featureGuard).requireEnabled();

        verifyNoInteractions(
                companyContext,
                receipts,
                receiptLines,
                orders,
                invoices,
                assembler
        );
    }

    @Test
    void rejectsExternalRepeatableReadBeforeRepositoryAccess()
            throws Exception {

        verifyTestDatabase();

        assertExternalTransactionRejected(
                TransactionDefinition.ISOLATION_REPEATABLE_READ,
                false
        );

        System.out.println(
                "COORDINATOR: REPEATABLE_READ esterna rifiutata"
        );
        System.out.println(
                "COORDINATOR: repository non interrogati"
        );
    }

    @Test
    void rejectsExternalReadOnlyBeforeRepositoryAccess()
            throws Exception {

        verifyTestDatabase();

        assertExternalTransactionRejected(
                TransactionDefinition.ISOLATION_READ_COMMITTED,
                true
        );

        System.out.println(
                "COORDINATOR: READ_ONLY esterna rifiutata"
        );
        System.out.println(
                "COORDINATOR: repository non interrogati"
        );
    }
}
