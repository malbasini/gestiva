package com.gestiva.purchasing.invoice.service;

import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialCoordinatorRealRollbackIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long SUPPLIER = 1L;
    private static final Long ITEM = 1L;
    private static final Long BOX = 9L;
    private static final Long PIECE = 1L;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager manager;

    @Autowired
    private SupplierInvoicePartialCoordinatorService coordinator;

    @MockitoBean
    private SupplierInvoicePartialGenerationGuard featureGuard;

    @MockitoBean
    private CompanyContext companyContext;

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private static void amount(
            String expected, BigDecimal actual) {
        assertNotNull(actual);
        assertEquals(
                0,
                bd(expected).compareTo(actual)
        );
    }

    private Long count(String sql, Object... args) {
        return jdbc.queryForObject(sql, Long.class, args);
    }

    private Long id(String sql, Object... args) {
        return jdbc.queryForObject(sql, Long.class, args);
    }

    private void verifyDatabaseAndReferences()
            throws Exception {

        try (Connection c =
                     jdbc.getDataSource().getConnection()) {

            assertEquals("gestiva_test", c.getCatalog());

            String user = c.getMetaData().getUserName();

            assertTrue(
                    user.equals("gestiva_test_runner")
                    || user.startsWith("gestiva_test_runner@")
            );
        }

        assertEquals(1L, count("""
                SELECT COUNT(*)
                FROM unit_of_measure
                WHERE tenant_id = ?
                  AND id = ?
                  AND code = 'BOX'
                """, TENANT, BOX));

        assertEquals(1L, count("""
                SELECT COUNT(*)
                FROM unit_of_measure
                WHERE tenant_id = ?
                  AND id = ?
                  AND code = 'PZ'
                """, TENANT, PIECE));

        assertEquals(1L, count("""
                SELECT COUNT(*)
                FROM item
                WHERE tenant_id = ?
                  AND id = ?
                  AND item_type = 'PRODUCT'
                  AND base_uom_id = ?
                """, TENANT, ITEM, PIECE));

        assertEquals(1L, count("""
                SELECT COUNT(*)
                FROM purchase_order
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND supplier_id = ?
                  AND status = 'CONFIRMED'
                """, TENANT, COMPANY, SUPPLIER) > 0 ? 1L : 0L);
    }

    private Long insertReceipt(
            String number,
            Long orderId,
            Long orderLineId,
            String quantity) {

        jdbc.update("""
                INSERT INTO goods_receipt (
                    tenant_id, company_id, receipt_number,
                    receipt_date, purchase_order_id,
                    supplier_id, created_at, updated_at
                ) VALUES (
                    ?, ?, ?, ?, ?, ?,
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """,
                TENANT,
                COMPANY,
                number,
                Date.valueOf(LocalDate.now()),
                orderId,
                SUPPLIER
        );

        Long receiptId = id("""
                SELECT id FROM goods_receipt
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND receipt_number = ?
                """, TENANT, COMPANY, number);

        assertNotNull(receiptId);

        jdbc.update("""
                INSERT INTO goods_receipt_line (
                    tenant_id, goods_receipt_id, line_no,
                    purchase_order_line_id, item_id,
                    description, quantity_received,
                    created_at, updated_at
                ) VALUES (
                    ?, ?, 1, ?, ?,
                    'Ricevimento parziale collaudo',
                    ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """,
                TENANT,
                receiptId,
                orderLineId,
                ITEM,
                bd(quantity)
        );

        return receiptId;
    }

    @Test
    void realCoordinatorReconcilesThreeReceiptsAndRollsBack()
            throws Exception {

        verifyDatabaseAndReferences();

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        String suffix = UUID.randomUUID()
                .toString()
                .substring(0, 12);

        String orderNumber = "IT2042-PO-" + suffix;
        String receiptPrefix = "IT2042-GR-" + suffix;

        Long[] ids = new Long[5];

        TransactionTemplate tx =
                new TransactionTemplate(manager);

        tx.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );
        tx.setReadOnly(false);
        tx.setIsolationLevel(
                TransactionDefinition.ISOLATION_READ_COMMITTED
        );

        tx.execute(status -> {

            jdbc.update("""
                    INSERT INTO purchase_order (
                        tenant_id, company_id, order_number,
                        order_date, supplier_id, status,
                        currency_code, subtotal_amount,
                        tax_amount, total_amount,
                        created_at, updated_at
                    ) VALUES (
                        ?, ?, ?, ?, ?, 'CONFIRMED',
                        'EUR', 144.00, 31.68, 175.68,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                    )
                    """,
                    TENANT,
                    COMPANY,
                    orderNumber,
                    Date.valueOf(LocalDate.now()),
                    SUPPLIER
            );

            ids[0] = id("""
                    SELECT id FROM purchase_order
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND order_number = ?
                    """,
                    TENANT, COMPANY, orderNumber
            );

            assertNotNull(ids[0]);

            jdbc.update("""
                    INSERT INTO purchase_order_line (
                        tenant_id, purchase_order_id, line_no,
                        item_id, uom_id, description,
                        quantity, conversion_factor,
                        unit_price, discount_pct, tax_pct,
                        line_subtotal, tax_amount, line_total,
                        created_at, updated_at
                    ) VALUES (
                        ?, ?, 1, ?, ?,
                        'Articolo collaudo fattura parziale',
                        2.000, 10.000000,
                        72.00, 0.00, 22.00,
                        144.00, 31.68, 175.68,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                    )
                    """,
                    TENANT,
                    ids[0],
                    ITEM,
                    BOX
            );

            ids[1] = id("""
                    SELECT id FROM purchase_order_line
                    WHERE tenant_id = ?
                      AND purchase_order_id = ?
                      AND line_no = 1
                    """,
                    TENANT, ids[0]
            );

            assertNotNull(ids[1]);

            ids[2] = insertReceipt(
                    receiptPrefix + "-1",
                    ids[0], ids[1], "8.000"
            );

            ids[3] = insertReceipt(
                    receiptPrefix + "-2",
                    ids[0], ids[1], "7.000"
            );

            ids[4] = insertReceipt(
                    receiptPrefix + "-3",
                    ids[0], ids[1], "5.000"
            );

            assertTrue(ids[2] < ids[3]);
            assertTrue(ids[3] < ids[4]);

            var first = coordinator.plan(
                    TENANT, ids[2]
            );

            var second = coordinator.plan(
                    TENANT, ids[3]
            );

            var third = coordinator.plan(
                    TENANT, ids[4]
            );

            assertNotNull(first);
            assertNotNull(second);
            assertNotNull(third);

            assertEquals(1, first.lines().size());
            assertEquals(1, second.lines().size());
            assertEquals(1, third.lines().size());

            amount(
                    "0.800",
                    first.lines().get(0).commercialQuantity()
            );
            amount(
                    "0.700",
                    second.lines().get(0).commercialQuantity()
            );
            amount(
                    "0.500",
                    third.lines().get(0).commercialQuantity()
            );

            amount("57.60", first.subtotal());
            amount("50.40", second.subtotal());
            amount("36.00", third.subtotal());

            amount("12.67", first.tax());
            amount("11.09", second.tax());
            amount("7.92", third.tax());

            amount("70.27", first.total());
            amount("61.49", second.total());
            amount("43.92", third.total());

            amount(
                    "144.00",
                    first.subtotal()
                            .add(second.subtotal())
                            .add(third.subtotal())
            );

            amount(
                    "31.68",
                    first.tax()
                            .add(second.tax())
                            .add(third.tax())
            );

            amount(
                    "175.68",
                    first.total()
                            .add(second.total())
                            .add(third.total())
            );

            assertEquals(0L, count("""
                    SELECT COUNT(*)
                    FROM supplier_invoice
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND goods_receipt_id IN (?, ?, ?)
                    """,
                    TENANT, COMPANY,
                    ids[2], ids[3], ids[4]
            ));

            System.out.println(
                    "COORDINATOR REAL: ricevimenti 8 + 7 + 5 PZ"
            );
            System.out.println(
                    "COORDINATOR REAL: piani 70.27 + 61.49 + 43.92"
            );
            System.out.println(
                    "COORDINATOR REAL: totale 175.68 EUR"
            );
            System.out.println(
                    "COORDINATOR REAL: nessuna fattura generata"
            );

            status.setRollbackOnly();
            return null;
        });

        assertEquals(0L, count("""
                SELECT COUNT(*)
                FROM purchase_order
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND order_number = ?
                """, TENANT, COMPANY, orderNumber));

        assertEquals(0L, count("""
                SELECT COUNT(*)
                FROM goods_receipt
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND receipt_number LIKE ?
                """,
                TENANT,
                COMPANY,
                receiptPrefix + "-%"
        ));

        assertEquals(0L, count("""
                SELECT COUNT(*)
                FROM purchase_order_line
                WHERE tenant_id = ?
                  AND purchase_order_id = ?
                """, TENANT, ids[0]));

        assertEquals(0L, count("""
                SELECT COUNT(*)
                FROM goods_receipt_line
                WHERE tenant_id = ?
                  AND goods_receipt_id IN (?, ?, ?)
                """,
                TENANT, ids[2], ids[3], ids[4]
        ));

        verify(featureGuard, times(3)).requireEnabled();

        System.out.println(
                "COORDINATOR REAL: rollback completo OK"
        );
    }
}
