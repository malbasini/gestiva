package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialPreparationIsolationIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long RECEIPT = 301L;

    @Autowired
    private SupplierInvoicePartialPreparationService preparation;

    @Autowired
    private PlatformTransactionManager manager;

    @Autowired
    private JdbcTemplate jdbc;

    // Feature gate simulato come abilitato.
    // L'Isolation Guard rimane REALE.
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

    private void verifyDatabase() throws Exception {
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

    private void assertBlockedBeforeRepositories(
            int isolation,
            boolean readOnly) {

        TransactionTemplate tx =
                new TransactionTemplate(manager);

        tx.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );
        tx.setIsolationLevel(isolation);
        tx.setReadOnly(readOnly);

        tx.execute(status -> {
            assertThrows(
                    BusinessException.class,
                    () -> preparation.prepare(TENANT, RECEIPT)
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
                invoices
        );

        verify(receipts, never()).save(any());
        verify(orders, never()).save(any());
        verify(invoices, never()).save(any());
    }

    @Test
    void repeatableReadIsRejectedBeforeRepositoryAccess()
            throws Exception {

        verifyDatabase();

        assertBlockedBeforeRepositories(
                TransactionDefinition.ISOLATION_REPEATABLE_READ,
                false
        );

        System.out.println(
                "PREPARATION GUARD: REPEATABLE_READ respinto"
        );
        System.out.println(
                "PREPARATION GUARD: repository non interrogati"
        );
    }

    @Test
    void readOnlyIsRejectedBeforeRepositoryAccess()
            throws Exception {

        verifyDatabase();

        assertBlockedBeforeRepositories(
                TransactionDefinition.ISOLATION_READ_COMMITTED,
                true
        );

        System.out.println(
                "PREPARATION GUARD: READ_ONLY respinto"
        );
        System.out.println(
                "PREPARATION GUARD: repository non interrogati"
        );
    }
}
