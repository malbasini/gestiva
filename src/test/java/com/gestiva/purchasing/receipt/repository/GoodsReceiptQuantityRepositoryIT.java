package com.gestiva.purchasing.receipt.repository;

import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptQuantityRepositoryIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private GoodsReceiptLineRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    private Long tenantId;
    private Long companyId;
    private Long purchaseOrderId;
    private Long purchaseOrderLineId;

    @BeforeEach
    void verifyDatabaseAndLoadFixture() throws Exception {

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
                gr.tenant_id,
                gr.company_id,
                gr.purchase_order_id,
                grl.purchase_order_line_id
            FROM goods_receipt_line grl
            JOIN goods_receipt gr
              ON gr.id = grl.goods_receipt_id
             AND gr.tenant_id = grl.tenant_id
            ORDER BY gr.id, grl.id
            LIMIT 1
            """);

        assertFalse(
                rows.isEmpty(),
                "Serve almeno una riga di ricevimento "
                + "già presente in gestiva_test"
        );

        var row = rows.get(0);

        tenantId = ((Number) row.get("tenant_id"))
                .longValue();

        companyId = ((Number) row.get("company_id"))
                .longValue();

        purchaseOrderId =
                ((Number) row.get("purchase_order_id"))
                .longValue();

        purchaseOrderLineId =
                ((Number) row.get("purchase_order_line_id"))
                .longValue();
    }

    @Test
    void existingLineMatchesIndependentSqlSum() {

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
            """,
            BigDecimal.class,
            tenantId,
            tenantId,
            companyId,
            purchaseOrderId,
            purchaseOrderLineId
        );

        assertNotNull(expected);

        BigDecimal actual =
                repository.sumReceivedBaseQuantityByOrderLine(
                        tenantId,
                        companyId,
                        purchaseOrderId,
                        purchaseOrderLineId
                );

        assertNotNull(actual);
        assertEquals(0, expected.compareTo(actual));

        System.out.println(
                "JPQL MYSQL: quantità ricevuta = " + actual
        );
    }

    @Test
    void unknownOrderLineReturnsNull() {

        BigDecimal actual =
                repository.sumReceivedBaseQuantityByOrderLine(
                        tenantId,
                        companyId,
                        purchaseOrderId,
                        Long.MAX_VALUE
                );

        assertNull(actual);

        System.out.println(
                "JPQL MYSQL: riga inesistente esclusa"
        );
    }

    @Test
    void otherCompanyReturnsNull() {

        BigDecimal actual =
                repository.sumReceivedBaseQuantityByOrderLine(
                        tenantId,
                        Long.MAX_VALUE,
                        purchaseOrderId,
                        purchaseOrderLineId
                );

        assertNull(actual);

        System.out.println(
                "JPQL MYSQL: società diversa esclusa"
        );
    }

    @Test
    void otherTenantReturnsNull() {

        BigDecimal actual =
                repository.sumReceivedBaseQuantityByOrderLine(
                        Long.MAX_VALUE,
                        companyId,
                        purchaseOrderId,
                        purchaseOrderLineId
                );

        assertNull(actual);

        System.out.println(
                "JPQL MYSQL: tenant diverso escluso"
        );
    }
}
