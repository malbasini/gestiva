package com.gestiva.purchasing.receipt.repository;

import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptFixtureConstraintsDiagnosticIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void inspectFixtureConstraintsReadOnly() throws Exception {

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
                    "Utente MySQL non autorizzato."
            );
        }

        for (String table : new String[] {
                "purchase_order",
                "purchase_order_line",
                "goods_receipt",
                "goods_receipt_line"
        }) {

            System.out.println(
                    "\nFIXTURE TABLE: " + table
            );

            List<Map<String, Object>> required =
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
                          AND IS_NULLABLE = 'NO'
                        ORDER BY ORDINAL_POSITION
                        """,
                        table
                    );

            for (var row : required) {
                System.out.println(
                        "REQUIRED: "
                        + row.get("COLUMN_NAME")
                        + " | TYPE=" + row.get("COLUMN_TYPE")
                        + " | DEFAULT=" + row.get("COLUMN_DEFAULT")
                        + " | EXTRA=" + row.get("EXTRA")
                );
            }

            List<Map<String, Object>> indexes =
                    jdbc.queryForList("""
                        SELECT
                            INDEX_NAME,
                            GROUP_CONCAT(
                                COLUMN_NAME
                                ORDER BY SEQ_IN_INDEX
                                SEPARATOR ','
                            ) AS COLUMNS_LIST
                        FROM information_schema.STATISTICS
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = ?
                          AND NON_UNIQUE = 0
                        GROUP BY INDEX_NAME
                        ORDER BY INDEX_NAME
                        """,
                        table
                    );

            for (var index : indexes) {
                System.out.println(
                        "UNIQUE: "
                        + index.get("INDEX_NAME")
                        + " | "
                        + index.get("COLUMNS_LIST")
                );
            }
        }

        List<Map<String, Object>> keys =
                jdbc.queryForList("""
                    SELECT
                        TABLE_NAME,
                        COLUMN_NAME,
                        CONSTRAINT_NAME,
                        REFERENCED_TABLE_NAME,
                        REFERENCED_COLUMN_NAME
                    FROM information_schema.KEY_COLUMN_USAGE
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND REFERENCED_TABLE_NAME IS NOT NULL
                      AND (
                          TABLE_NAME IN (
                              'purchase_order',
                              'purchase_order_line',
                              'goods_receipt',
                              'goods_receipt_line'
                          )
                          OR REFERENCED_TABLE_NAME IN (
                              'purchase_order',
                              'purchase_order_line',
                              'goods_receipt',
                              'goods_receipt_line'
                          )
                      )
                    ORDER BY
                        TABLE_NAME,
                        CONSTRAINT_NAME,
                        ORDINAL_POSITION
                    """);

        System.out.println(
                "\nFIXTURE FOREIGN KEYS"
        );

        for (var key : keys) {
            System.out.println(
                    "FK: " + key.get("TABLE_NAME")
                    + "." + key.get("COLUMN_NAME")
                    + " -> "
                    + key.get("REFERENCED_TABLE_NAME")
                    + "." + key.get("REFERENCED_COLUMN_NAME")
                    + " | " + key.get("CONSTRAINT_NAME")
            );
        }

        System.out.println(
                "\nFIXTURE CONSTRAINTS: READ ONLY"
        );
    }
}
