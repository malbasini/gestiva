package com.gestiva.purchasing.receipt.repository;

import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptPreviousQuantityBoundaryIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private GoodsReceiptLineRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    private Long tenantId;
    private Long companyId;
    private Long orderId;
    private Long orderLineId;
    private Long receiptId;
    private BigDecimal receivedQuantity;

    @BeforeEach
    void prepare() throws Exception {

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
                    "Account MySQL non autorizzato."
            );
        }

        List<Map<String, Object>> rows =
                jdbc.queryForList("""
                    SELECT
                        gr.id AS receipt_id,
                        gr.tenant_id,
                        gr.company_id,
                        gr.purchase_order_id,
                        grl.purchase_order_line_id,
                        grl.quantity_received
                    FROM goods_receipt gr
                    JOIN goods_receipt_line grl
                      ON grl.goods_receipt_id = gr.id
                     AND grl.tenant_id = gr.tenant_id
                    ORDER BY gr.id ASC, grl.id ASC
                    LIMIT 1
                    """);

        assertFalse(
                rows.isEmpty(),
                "Nessuna riga ricevimento disponibile."
        );

        Map<String, Object> row = rows.get(0);

        receiptId = ((Number) row.get("receipt_id"))
                .longValue();

        tenantId = ((Number) row.get("tenant_id"))
                .longValue();

        companyId = ((Number) row.get("company_id"))
                .longValue();

        orderId = ((Number) row.get("purchase_order_id"))
                .longValue();

        orderLineId =
                ((Number) row.get("purchase_order_line_id"))
                .longValue();

        receivedQuantity =
                (BigDecimal) row.get("quantity_received");

        assertNotNull(receivedQuantity);
        assertTrue(receiptId < Long.MAX_VALUE);
    }

    private BigDecimal sumBefore(Long boundary) {

        return repository.sumReceivedBeforeReceiptByOrderLine(
                tenantId,
                companyId,
                orderId,
                orderLineId,
                boundary
        );
    }

    @Test
    void currentReceiptIsExcluded() {

        assertNull(sumBefore(receiptId));

        System.out.println(
                "JPQL BOUNDARY: documento corrente escluso"
        );
    }

    @Test
    void higherBoundaryIncludesExistingReceipt() {

        BigDecimal actual = sumBefore(receiptId + 1);

        assertNotNull(actual);

        assertEquals(
                0,
                receivedQuantity.compareTo(actual)
        );

        System.out.println(
                "JPQL BOUNDARY: quantità positiva = "
                + actual
        );
    }

    @Test
    void quantityMatchesIndependentSqlAggregation() {

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
            orderId,
            orderLineId,
            receiptId + 1
        );

        BigDecimal actual = sumBefore(receiptId + 1);

        assertNotNull(expected);
        assertNotNull(actual);

        assertEquals(
                0,
                expected.compareTo(actual)
        );

        System.out.println(
                "JPQL BOUNDARY: somma SQL verificata"
        );
    }

    @Test
    void earlierBoundaryReturnsNoQuantity() {

        assertNull(sumBefore(receiptId - 1));

        System.out.println(
                "JPQL BOUNDARY: soglia precedente esclusa"
        );
    }
}
