package com.gestiva.purchasing.receipt.repository;

import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptSchemaDiagnosticIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void inspectActualGoodsReceiptConstraints()
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
                    ),
                    "Account MySQL non autorizzato"
            );
        }

        System.out.println(
                "\n========== VINCOLI GOODS_RECEIPT =========="
        );

        List<Map<String, Object>> indexes =
                jdbc.queryForList("""
                    SELECT
                        INDEX_NAME,
                        NON_UNIQUE,
                        GROUP_CONCAT(
                            COLUMN_NAME
                            ORDER BY SEQ_IN_INDEX
                            SEPARATOR ', '
                        ) AS COLUMNS_LIST
                    FROM information_schema.STATISTICS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'goods_receipt'
                    GROUP BY INDEX_NAME, NON_UNIQUE
                    ORDER BY INDEX_NAME
                    """);

        assertFalse(
                indexes.isEmpty(),
                "Nessun indice trovato su goods_receipt."
        );

        boolean uniqueOrderConstraint = false;

        for (var index : indexes) {

            String name =
                    String.valueOf(index.get("INDEX_NAME"));

            int nonUnique =
                    ((Number) index.get("NON_UNIQUE"))
                            .intValue();

            String columns =
                    String.valueOf(index.get("COLUMNS_LIST"));

            System.out.printf(
                    "INDEX=%s | UNIQUE=%s | COLUMNS=%s%n",
                    name,
                    nonUnique == 0,
                    columns
            );

            if (name.equals(
                    "uk_goods_receipt_tenant_company_purchase_order"
            ) && nonUnique == 0) {
                uniqueOrderConstraint = true;
            }
        }

        System.out.println(
                "UNIQUE PER ORDINE PRESENTE: "
                + uniqueOrderConstraint
        );

        System.out.println(
                "\n========== DATI ESISTENTI =========="
        );

        Long receipts = jdbc.queryForObject("""
            SELECT COUNT(*)
            FROM goods_receipt
            """, Long.class);

        Long receiptLines = jdbc.queryForObject("""
            SELECT COUNT(*)
            FROM goods_receipt_line
            """, Long.class);

        Long duplicateOrderGroups = jdbc.queryForObject("""
            SELECT COUNT(*)
            FROM (
                SELECT tenant_id, company_id, purchase_order_id
                FROM goods_receipt
                GROUP BY tenant_id, company_id, purchase_order_id
                HAVING COUNT(*) > 1
            ) grouped_receipts
            """, Long.class);

        System.out.println("RICEVIMENTI: " + receipts);
        System.out.println("RIGHE RICEVIMENTO: " + receiptLines);
        System.out.println(
                "ORDINI CON PIÙ RICEVIMENTI: "
                + duplicateOrderGroups
        );

        System.out.println(
                "\nDIAGNOSTICA SCHEMA COMPLETATA — SOLO LETTURA"
        );
    }
}
