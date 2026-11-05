package com.gestiva.purchasing.invoice.service;

import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SupplierInvoicePartialRealHistoryRollbackIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager manager;

    @Autowired
    private GoodsReceiptLineRepository receiptLines;

    @Autowired
    private SupplierInvoicePartialLinePlanner planner;

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private static void equalAmount(
            String expected, BigDecimal actual) {
        assertNotNull(actual);
        assertEquals(0, bd(expected).compareTo(actual));
    }

    private void verifyDatabase() throws Exception {
        try (Connection c = jdbc.getDataSource().getConnection()) {
            assertEquals("gestiva_test", c.getCatalog());

            String user = c.getMetaData().getUserName();

            assertTrue(
                    user.equals("gestiva_test_runner")
                    || user.startsWith("gestiva_test_runner@")
            );
        }
    }

    private Long insertReceipt(
            Long tenant,
            Long company,
            Long supplier,
            Long orderId,
            Long orderLineId,
            String receiptNumber,
            String quantity) {

        jdbc.update("""
                INSERT INTO goods_receipt (
                    tenant_id, company_id, receipt_number,
                    receipt_date, purchase_order_id, supplier_id,
                    created_at, updated_at
                ) VALUES (
                    ?, ?, ?, ?, ?, ?,
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """,
                tenant,
                company,
                receiptNumber,
                Date.valueOf(LocalDate.now()),
                orderId,
                supplier
        );

        Long receiptId = jdbc.queryForObject("""
                SELECT id
                FROM goods_receipt
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND receipt_number = ?
                """,
                Long.class,
                tenant,
                company,
                receiptNumber
        );

        assertNotNull(receiptId);

        jdbc.update("""
                INSERT INTO goods_receipt_line (
                    tenant_id, goods_receipt_id, line_no,
                    purchase_order_line_id, description,
                    quantity_received, created_at, updated_at
                ) VALUES (
                    ?, ?, 1, ?, 'Fixture fatturazione parziale',
                    ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """,
                tenant,
                receiptId,
                orderLineId,
                bd(quantity)
        );

        return receiptId;
    }

    private SupplierInvoicePartialBillabilityValidator.BillableLine
            plan(
                    Long tenant,
                    Long company,
                    Long orderId,
                    Long orderLineId,
                    Long receiptId,
                    String received,
                    String previous) {

        BigDecimal prior =
                receiptLines.sumReceivedBeforeReceiptByOrderLine(
                        tenant,
                        company,
                        orderId,
                        orderLineId,
                        receiptId
                );

        if ("0.000".equals(previous)) {
            assertNull(prior);
        } else {
            equalAmount(previous, prior);
        }

        return planner.plan(
                tenant,
                company,
                orderId,
                orderLineId,
                receiptId,
                bd(received),
                bd("20.000"),
                bd("10.000000"),
                bd("144.00"),
                bd("31.68"),
                bd("175.68")
        );
    }

    @Test
    void threeRealReceiptsReconcileAndRollback()
            throws Exception {

        verifyDatabase();

        Map<String, Object> reference = jdbc.queryForMap("""
                SELECT tenant_id, company_id, supplier_id
                FROM purchase_order
                WHERE supplier_id IS NOT NULL
                ORDER BY id ASC
                LIMIT 1
                """);

        Long tenant =
                ((Number) reference.get("tenant_id")).longValue();
        Long company =
                ((Number) reference.get("company_id")).longValue();
        Long supplier =
                ((Number) reference.get("supplier_id")).longValue();

        String suffix = UUID.randomUUID()
                .toString().substring(0, 12);

        String orderNumber = "IT2028-PO-" + suffix;
        String receiptPrefix = "IT2028-GR-" + suffix;

        Long[] ids = new Long[5];

        TransactionTemplate tx = new TransactionTemplate(manager);
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
                    tenant,
                    company,
                    orderNumber,
                    Date.valueOf(LocalDate.now()),
                    supplier
            );

            ids[0] = jdbc.queryForObject("""
                    SELECT id FROM purchase_order
                    WHERE tenant_id = ?
                      AND company_id = ?
                      AND order_number = ?
                    """,
                    Long.class,
                    tenant,
                    company,
                    orderNumber
            );

            assertNotNull(ids[0]);

            jdbc.update("""
                    INSERT INTO purchase_order_line (
                        tenant_id, purchase_order_id, line_no,
                        description, quantity, conversion_factor,
                        unit_price, discount_pct, tax_pct,
                        line_subtotal, tax_amount, line_total,
                        created_at, updated_at
                    ) VALUES (
                        ?, ?, 1, 'Fixture fatturazione parziale',
                        2.000, 10.000000, 72.00, 0.00, 22.00,
                        144.00, 31.68, 175.68,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                    )
                    """,
                    tenant,
                    ids[0]
            );

            ids[1] = jdbc.queryForObject("""
                    SELECT id FROM purchase_order_line
                    WHERE tenant_id = ?
                      AND purchase_order_id = ?
                      AND line_no = 1
                    """,
                    Long.class,
                    tenant,
                    ids[0]
            );

            assertNotNull(ids[1]);

            ids[2] = insertReceipt(
                    tenant, company, supplier, ids[0], ids[1],
                    receiptPrefix + "-1", "8.000"
            );

            ids[3] = insertReceipt(
                    tenant, company, supplier, ids[0], ids[1],
                    receiptPrefix + "-2", "7.000"
            );

            ids[4] = insertReceipt(
                    tenant, company, supplier, ids[0], ids[1],
                    receiptPrefix + "-3", "5.000"
            );

            assertTrue(ids[2] < ids[3]);
            assertTrue(ids[3] < ids[4]);

            var first = plan(
                    tenant, company, ids[0], ids[1],
                    ids[2], "8.000", "0.000"
            );

            var second = plan(
                    tenant, company, ids[0], ids[1],
                    ids[3], "7.000", "8.000"
            );

            var third = plan(
                    tenant, company, ids[0], ids[1],
                    ids[4], "5.000", "15.000"
            );

            equalAmount(
                    "0.800", first.commercialQuantity()
            );
            equalAmount(
                    "0.700", second.commercialQuantity()
            );
            equalAmount(
                    "0.500", third.commercialQuantity()
            );

            equalAmount("57.60", first.subtotal());
            equalAmount("50.40", second.subtotal());
            equalAmount("36.00", third.subtotal());

            equalAmount("12.67", first.tax());
            equalAmount("11.09", second.tax());
            equalAmount("7.92", third.tax());

            equalAmount("70.27", first.total());
            equalAmount("61.49", second.total());
            equalAmount("43.92", third.total());

            equalAmount(
                    "144.00",
                    first.subtotal()
                            .add(second.subtotal())
                            .add(third.subtotal())
            );

            equalAmount(
                    "31.68",
                    first.tax().add(second.tax()).add(third.tax())
            );

            equalAmount(
                    "175.68",
                    first.total()
                            .add(second.total())
                            .add(third.total())
            );

            assertNull(
                    receiptLines.sumReceivedBeforeReceiptByOrderLine(
                            tenant,
                            Long.MAX_VALUE,
                            ids[0],
                            ids[1],
                            ids[4]
                    )
            );

            assertNull(
                    receiptLines.sumReceivedBeforeReceiptByOrderLine(
                            Long.MAX_VALUE,
                            company,
                            ids[0],
                            ids[1],
                            ids[4]
                    )
            );

            assertNull(
                    receiptLines.sumReceivedBeforeReceiptByOrderLine(
                            tenant,
                            company,
                            ids[0],
                            Long.MAX_VALUE,
                            ids[4]
                    )
            );

            System.out.println(
                    "REAL HISTORY: 8 + 7 + 5 PZ"
            );
            System.out.println(
                    "REAL HISTORY: storico JPA 0 / 8 / 15"
            );
            System.out.println(
                    "REAL HISTORY: totale 175.68 EUR"
            );

            status.setRollbackOnly();
            return null;
        });

        Long remainingOrders = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM purchase_order
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND order_number = ?
                """,
                Long.class,
                tenant,
                company,
                orderNumber
        );

        Long remainingReceipts = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM goods_receipt
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND receipt_number LIKE ?
                """,
                Long.class,
                tenant,
                company,
                receiptPrefix + "-%"
        );

        Long remainingLines = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM goods_receipt_line
                WHERE tenant_id = ?
                  AND goods_receipt_id IN (?, ?, ?)
                """,
                Long.class,
                tenant,
                ids[2],
                ids[3],
                ids[4]
        );

        assertEquals(0L, remainingOrders);
        assertEquals(0L, remainingReceipts);
        assertEquals(0L, remainingLines);

        System.out.println(
                "REAL HISTORY: rollback ordine e ricevimenti OK"
        );
        System.out.println(
                "REAL HISTORY: nessuna fattura generata"
        );
    }
}
