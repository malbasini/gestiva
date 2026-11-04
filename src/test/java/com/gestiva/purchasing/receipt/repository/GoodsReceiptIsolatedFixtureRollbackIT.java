package com.gestiva.purchasing.receipt.repository;

import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptIsolatedFixtureRollbackIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private GoodsReceiptLineRepository repository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void isolatedFixtureMustDisappearAfterRollback()
            throws Exception {

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
                    || username.startsWith(
                            "gestiva_test_runner@"
                    )
            );
        }

        Map<String, Object> reference = jdbc.queryForMap("""
            SELECT tenant_id, company_id, supplier_id
            FROM purchase_order
            WHERE supplier_id IS NOT NULL
            ORDER BY id ASC
            LIMIT 1
            """);

        Long tenantId =
                ((Number) reference.get("tenant_id")).longValue();

        Long companyId =
                ((Number) reference.get("company_id")).longValue();

        Long supplierId =
                ((Number) reference.get("supplier_id")).longValue();

        String suffix = UUID.randomUUID()
                .toString()
                .substring(0, 12);

        String orderNumber = "IT1881-PO-" + suffix;
        String receiptNumber = "IT1881-GR-" + suffix;

        TransactionTemplate tx =
                new TransactionTemplate(transactionManager);

        tx.setReadOnly(false);

        tx.execute(status -> {

            jdbc.update("""
                INSERT INTO purchase_order (
                    tenant_id,
                    company_id,
                    order_number,
                    order_date,
                    supplier_id,
                    status,
                    currency_code,
                    subtotal_amount,
                    tax_amount,
                    total_amount,
                    created_at,
                    updated_at
                )
                VALUES (
                    ?, ?, ?, ?, ?, 'CONFIRMED', 'EUR',
                    144.00, 31.68, 175.68,
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """,
                tenantId,
                companyId,
                orderNumber,
                Date.valueOf(LocalDate.now()),
                supplierId
            );

            Long orderId = jdbc.queryForObject("""
                SELECT id FROM purchase_order
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND order_number = ?
                """,
                Long.class,
                tenantId,
                companyId,
                orderNumber
            );

            assertNotNull(orderId);

            jdbc.update("""
                INSERT INTO purchase_order_line (
                    tenant_id,
                    purchase_order_id,
                    line_no,
                    description,
                    quantity,
                    conversion_factor,
                    unit_price,
                    discount_pct,
                    tax_pct,
                    line_subtotal,
                    tax_amount,
                    line_total,
                    created_at,
                    updated_at
                )
                VALUES (
                    ?, ?, 1, 'Fixture isolata 1881',
                    2.000, 10.000000,
                    72.00, 0.00, 22.00,
                    144.00, 31.68, 175.68,
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """,
                tenantId,
                orderId
            );

            Long orderLineId = jdbc.queryForObject("""
                SELECT id FROM purchase_order_line
                WHERE tenant_id = ?
                  AND purchase_order_id = ?
                  AND line_no = 1
                """,
                Long.class,
                tenantId,
                orderId
            );

            assertNotNull(orderLineId);

            jdbc.update("""
                INSERT INTO goods_receipt (
                    tenant_id,
                    company_id,
                    receipt_number,
                    receipt_date,
                    purchase_order_id,
                    supplier_id,
                    created_at,
                    updated_at
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?,
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """,
                tenantId,
                companyId,
                receiptNumber,
                Date.valueOf(LocalDate.now()),
                orderId,
                supplierId
            );

            Long receiptId = jdbc.queryForObject("""
                SELECT id FROM goods_receipt
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND receipt_number = ?
                """,
                Long.class,
                tenantId,
                companyId,
                receiptNumber
            );

            assertNotNull(receiptId);

            jdbc.update("""
                INSERT INTO goods_receipt_line (
                    tenant_id,
                    goods_receipt_id,
                    line_no,
                    purchase_order_line_id,
                    description,
                    quantity_received,
                    created_at,
                    updated_at
                )
                VALUES (
                    ?, ?, 1, ?,
                    'Fixture 8 PZ',
                    8.000,
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """,
                tenantId,
                receiptId,
                orderLineId
            );

            BigDecimal quantity =
                    repository.sumReceivedBaseQuantityByOrderLine(
                            tenantId,
                            companyId,
                            orderId,
                            orderLineId
                    );

            assertNotNull(quantity);
            assertEquals(
                    0,
                    new BigDecimal("8.000")
                            .compareTo(quantity)
            );

            System.out.println(
                    "FIXTURE ROLLBACK: ordine temporaneo creato"
            );

            System.out.println(
                    "FIXTURE ROLLBACK: ricevimento temporaneo = "
                    + quantity
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
            tenantId,
            companyId,
            orderNumber
        );

        Long remainingReceipts = jdbc.queryForObject("""
            SELECT COUNT(*)
            FROM goods_receipt
            WHERE tenant_id = ?
              AND company_id = ?
              AND receipt_number = ?
            """,
            Long.class,
            tenantId,
            companyId,
            receiptNumber
        );

        assertEquals(0L, remainingOrders);
        assertEquals(0L, remainingReceipts);

        System.out.println(
                "FIXTURE ROLLBACK: ordine assente dopo rollback"
        );

        System.out.println(
                "FIXTURE ROLLBACK: ricevimento assente dopo rollback"
        );

        System.out.println(
                "FIXTURE ROLLBACK: pulizia verificata"
        );
    }
}
