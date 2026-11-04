package com.gestiva.purchasing.receipt.repository;

import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptPreviousQuantityRepositoryIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private GoodsReceiptLineRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    private Long tenantId;
    private Long companyId;
    private Long purchaseOrderId;
    private Long purchaseOrderLineId;
    private Long goodsReceiptId;

    @BeforeEach
    void verifyDatabaseAndSelectReceipt() throws Exception {

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
                    ),
                    "Account MySQL non autorizzato"
            );
        }

        var rows = jdbc.queryForList("""
            SELECT
                gr.id AS receipt_id,
                gr.tenant_id,
                gr.company_id,
                gr.purchase_order_id,
                grl.purchase_order_line_id
            FROM goods_receipt gr
            JOIN goods_receipt_line grl
              ON grl.goods_receipt_id = gr.id
             AND grl.tenant_id = gr.tenant_id
            ORDER BY gr.id ASC, grl.id ASC
            LIMIT 1
            """);

        assertFalse(
                rows.isEmpty(),
                "Serve almeno un ricevimento con una riga "
                + "in gestiva_test"
        );

        var row = rows.get(0);

        goodsReceiptId =
                ((Number) row.get("receipt_id")).longValue();

        tenantId =
                ((Number) row.get("tenant_id")).longValue();

        companyId =
                ((Number) row.get("company_id")).longValue();

        purchaseOrderId =
                ((Number) row.get("purchase_order_id"))
                        .longValue();

        purchaseOrderLineId =
                ((Number) row.get("purchase_order_line_id"))
                        .longValue();
    }

    private BigDecimal previousQuantity(
            Long tenant,
            Long company,
            Long receiptId) {

        return repository.sumReceivedBeforeReceiptByOrderLine(
                tenant,
                company,
                purchaseOrderId,
                purchaseOrderLineId,
                receiptId
        );
    }

    @Test
    void previousQuantityMatchesIndependentSql() {

        BigDecimal expected = jdbc.queryForObject("""
            SELECT SUM(grl.quantity_received)
            FROM goods_receipt_line grl
            JOIN goods_receipt gr
              ON gr.id = grl.goods_receipt_id
             AND gr.tenant_id = grl.tenant_id
            WHERE grl.tenant_id = ?
              AND gr.tenant_id = ?
              AND gr.company_id = ?
              AND gr.purchase_order_id = ?
              AND grl.purchase_order_line_id = ?
              AND gr.id < ?
            """,
            BigDecimal.class,
            tenantId,
            tenantId,
            companyId,
            purchaseOrderId,
            purchaseOrderLineId,
            goodsReceiptId
        );

        BigDecimal actual = previousQuantity(
                tenantId, companyId, goodsReceiptId
        );

        if (expected == null) {
            assertNull(actual);
        } else {
            assertNotNull(actual);
            assertEquals(0, expected.compareTo(actual));
        }

        System.out.println(
                "JPQL PRECEDENTI: quantità = "
                + (actual == null ? "NULL" : actual)
        );
    }

    @Test
    void firstReceiptHasNoEarlierQuantity() {

        Long earliestId = jdbc.queryForObject("""
            SELECT MIN(gr.id)
            FROM goods_receipt gr
            JOIN goods_receipt_line grl
              ON grl.goods_receipt_id = gr.id
             AND grl.tenant_id = gr.tenant_id
            WHERE gr.tenant_id = ?
              AND gr.company_id = ?
              AND gr.purchase_order_id = ?
              AND grl.purchase_order_line_id = ?
            """,
            Long.class,
            tenantId,
            companyId,
            purchaseOrderId,
            purchaseOrderLineId
        );

        assertNotNull(earliestId);

        assertNull(
                previousQuantity(
                        tenantId, companyId, earliestId
                )
        );

        System.out.println(
                "JPQL PRECEDENTI: primo ricevimento escluso"
        );
    }

    @Test
    void differentCompanyIsExcluded() {

        assertNull(
                previousQuantity(
                        tenantId,
                        Long.MAX_VALUE,
                        goodsReceiptId
                )
        );

        System.out.println(
                "JPQL PRECEDENTI: società diversa esclusa"
        );
    }

    @Test
    void differentTenantIsExcluded() {

        assertNull(
                previousQuantity(
                        Long.MAX_VALUE,
                        companyId,
                        goodsReceiptId
                )
        );

        System.out.println(
                "JPQL PRECEDENTI: tenant diverso escluso"
        );
    }
}
