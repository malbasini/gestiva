package com.gestiva.purchasing.receipt.repository;

import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptConcurrentFixtureDiagnosticIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void inspectFixtureRequirementsWithoutWrites()
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

        for (String table : new String[] {
                "purchase_order",
                "purchase_order_line",
                "goods_receipt",
                "goods_receipt_line"
        }) {

            System.out.println(
                    "FIXTURE SCHEMA: " + table
            );

            List<Map<String, Object>> columns =
                    jdbc.queryForList("""
                        SELECT
                            COLUMN_NAME,
                            COLUMN_TYPE,
                            IS_NULLABLE,
                            COLUMN_DEFAULT,
                            EXTRA
                        FROM information_schema.COLUMNS
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = ?
                        ORDER BY ORDINAL_POSITION
                        """,
                        table
                    );

            assertFalse(
                    columns.isEmpty(),
                    "Tabella non trovata: " + table
            );

            for (Map<String, Object> column : columns) {
                System.out.println(
                        "  "
                        + column.get("COLUMN_NAME")
                        + " | " + column.get("COLUMN_TYPE")
                        + " | NULL="
                        + column.get("IS_NULLABLE")
                        + " | DEFAULT="
                        + column.get("COLUMN_DEFAULT")
                        + " | EXTRA="
                        + column.get("EXTRA")
                );
            }
        }

        List<Map<String, Object>> orderLines =
                jdbc.queryForList("""
                    SELECT
                        po.id AS order_id,
                        po.tenant_id,
                        po.company_id,
                        po.status,
                        pol.id AS order_line_id,
                        pol.quantity,
                        pol.conversion_factor,
                        (
                            SELECT COUNT(*)
                            FROM goods_receipt gr
                            WHERE gr.tenant_id = po.tenant_id
                              AND gr.company_id = po.company_id
                              AND gr.purchase_order_id = po.id
                        ) AS existing_receipts
                    FROM purchase_order po
                    JOIN purchase_order_line pol
                      ON pol.purchase_order_id = po.id
                     AND pol.tenant_id = po.tenant_id
                    ORDER BY po.id, pol.id
                    LIMIT 10
                    """);

        System.out.println(
                "FIXTURE CANDIDATES: " + orderLines.size()
        );

        for (Map<String, Object> row : orderLines) {
            System.out.println(
                    "FIXTURE CANDIDATE: " + row
            );
        }

        System.out.println(
                "FIXTURE DIAGNOSTIC: READ ONLY"
        );
    }
}
