package com.gestiva.purchasing.receipt.service;

import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptPartialDocumentRollbackPreflightIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void checksDocumentRollbackPrerequisites() throws Exception {

        try (Connection connection =
                     jdbc.getDataSource().getConnection()) {

            assertEquals("gestiva_test", connection.getCatalog());

            String user = connection.getMetaData().getUserName();

            assertTrue(
                    user.equals("gestiva_test_runner")
                    || user.startsWith("gestiva_test_runner@"),
                    "Utente MySQL non autorizzato."
            );

            assertTrue(
                    connection.getAutoCommit(),
                    "Preflight previsto fuori da transazioni di scrittura."
            );
        }

        Integer existingTables = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name IN (
                      'purchase_order',
                      'goods_receipt',
                      'goods_receipt_line'
                  )
                """,
                Integer.class
        );

        assertEquals(3, existingTables);

        System.out.println(
                "PREFLIGHT MYSQL: tre tabelle documentali presenti"
        );

        List<Map<String, Object>> constraints =
                jdbc.queryForList(
                        """
                        SELECT tc.table_name,
                               tc.constraint_name,
                               tc.constraint_type
                        FROM information_schema.table_constraints tc
                        WHERE tc.table_schema = DATABASE()
                          AND tc.table_name IN (
                              'goods_receipt',
                              'goods_receipt_line'
                          )
                        ORDER BY tc.table_name, tc.constraint_name
                        """
                );

        System.out.println(
                "PREFLIGHT MYSQL: vincoli reali su testata e righe"
        );

        for (var constraint : constraints) {
            System.out.println(
                    "  " + constraint.get("table_name")
                    + " | " + constraint.get("constraint_name")
                    + " | " + constraint.get("constraint_type")
            );
        }

        List<Map<String, Object>> columns = jdbc.queryForList(
                """
                SELECT table_name,
                       column_name,
                       data_type,
                       is_nullable,
                       column_default,
                       extra
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name IN (
                      'goods_receipt',
                      'goods_receipt_line'
                  )
                ORDER BY table_name, ordinal_position
                """
        );

        System.out.println(
                "PREFLIGHT MYSQL: colonne effettive dei documenti"
        );

        for (var column : columns) {
            System.out.println(
                    "  " + column.get("table_name")
                    + "." + column.get("column_name")
                    + " | " + column.get("data_type")
                    + " | nullable=" + column.get("is_nullable")
                    + " | default=" + column.get("column_default")
                    + " | extra=" + column.get("extra")
            );
        }

        // Cerchiamo un ordine realmente esistente
        // senza ricevimenti già registrati.
        List<Map<String, Object>> candidates =
                jdbc.queryForList(
                        """
                        SELECT po.id,
                               po.supplier_id,
                               po.order_number,
                               po.status
                        FROM purchase_order po
                        WHERE po.tenant_id = ?
                          AND po.company_id = ?
                          AND po.status = 'CONFIRMED'
                          AND NOT EXISTS (
                              SELECT 1
                              FROM goods_receipt gr
                              WHERE gr.tenant_id = po.tenant_id
                                AND gr.company_id = po.company_id
                                AND gr.purchase_order_id = po.id
                          )
                        ORDER BY po.id
                        LIMIT 5
                        """,
                        2L, 1L
                );

        System.out.println(
                "PREFLIGHT MYSQL: ordini confermati senza GR = "
                + candidates.size()
                + " (massimo 5 mostrati)"
        );

        for (var candidate : candidates) {
            System.out.println(
                    "  PO id=" + candidate.get("id")
                    + " | numero=" + candidate.get("order_number")
                    + " | fornitore=" + candidate.get("supplier_id")
            );
        }

        boolean uniquePoConstraint = constraints.stream()
                .anyMatch(c ->
                        "uk_goods_receipt_tenant_company_purchase_order"
                                .equals(c.get("constraint_name"))
                );

        System.out.println(
                "PREFLIGHT MYSQL: unicità GR-per-PO = "
                + (uniquePoConstraint ? "PRESENTE" : "NON PRESENTE")
        );

        System.out.println(
                "PREFLIGHT MYSQL: nessuna modifica effettuata"
        );
    }
}
