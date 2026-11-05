package com.gestiva.purchasing.receipt.service;

import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptPartialRollbackFixtureAuditIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void inspectsRequirementsWithoutWriting() throws Exception {

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
                    || username.startsWith("gestiva_test_runner@"),
                    "Utente MySQL non autorizzato."
            );
        }

        System.out.println(
                "FIXTURE AUDIT: database protetto confermato"
        );

        List<Map<String, Object>> columns =
                jdbc.queryForList("""
                    SELECT table_name,
                           column_name,
                           column_type,
                           is_nullable,
                           column_default,
                           extra
                    FROM information_schema.columns
                    WHERE table_schema = DATABASE()
                      AND table_name IN (
                          'purchase_order',
                          'purchase_order_line',
                          'supplier',
                          'warehouse'
                      )
                    ORDER BY table_name, ordinal_position
                    """);

        assertFalse(columns.isEmpty());

        System.out.println(
                "FIXTURE AUDIT: struttura tabelle coinvolte"
        );

        for (var column : columns) {
            System.out.println(
                    "  " + column.get("table_name")
                    + "." + column.get("column_name")
                    + " | " + column.get("column_type")
                    + " | nullable=" + column.get("is_nullable")
                    + " | default=" + column.get("column_default")
                    + " | extra=" + column.get("extra")
            );
        }

        List<Map<String, Object>> constraints =
                jdbc.queryForList("""
                    SELECT kcu.table_name,
                           kcu.constraint_name,
                           kcu.column_name,
                           kcu.referenced_table_name,
                           kcu.referenced_column_name
                    FROM information_schema.key_column_usage kcu
                    WHERE kcu.table_schema = DATABASE()
                      AND kcu.table_name IN (
                          'purchase_order',
                          'purchase_order_line',
                          'goods_receipt',
                          'goods_receipt_line'
                      )
                      AND kcu.referenced_table_name IS NOT NULL
                    ORDER BY kcu.table_name,
                             kcu.constraint_name,
                             kcu.ordinal_position
                    """);

        System.out.println(
                "FIXTURE AUDIT: foreign key effettive"
        );

        for (var constraint : constraints) {
            System.out.println(
                    "  " + constraint.get("table_name")
                    + "." + constraint.get("column_name")
                    + " -> "
                    + constraint.get("referenced_table_name")
                    + "." + constraint.get("referenced_column_name")
                    + " | " + constraint.get("constraint_name")
            );
        }

        List<Map<String, Object>> statuses =
                jdbc.queryForList("""
                    SELECT status, COUNT(*) AS totale
                    FROM purchase_order
                    WHERE tenant_id = ?
                      AND company_id = ?
                    GROUP BY status
                    ORDER BY status
                    """, 2L, 1L);

        System.out.println(
                "FIXTURE AUDIT: stati ordini tenant 2 / company 1"
        );

        for (var status : statuses) {
            System.out.println(
                    "  " + status.get("status")
                    + " | totale=" + status.get("totale")
            );
        }

        List<Map<String, Object>> orders =
                jdbc.queryForList("""
                    SELECT po.id,
                           po.order_number,
                           po.status,
                           po.supplier_id,
                           COUNT(DISTINCT pol.id) AS righe,
                           COUNT(DISTINCT gr.id) AS ricevimenti
                    FROM purchase_order po
                    LEFT JOIN purchase_order_line pol
                      ON pol.tenant_id = po.tenant_id
                     AND pol.purchase_order_id = po.id
                    LEFT JOIN goods_receipt gr
                      ON gr.tenant_id = po.tenant_id
                     AND gr.company_id = po.company_id
                     AND gr.purchase_order_id = po.id
                    WHERE po.tenant_id = ?
                      AND po.company_id = ?
                    GROUP BY po.id,
                             po.order_number,
                             po.status,
                             po.supplier_id
                    ORDER BY po.id DESC
                    LIMIT 10
                    """, 2L, 1L);

        System.out.println(
                "FIXTURE AUDIT: ultimi 10 ordini e relazioni"
        );

        for (var order : orders) {
            System.out.println(
                    "  PO id=" + order.get("id")
                    + " | numero=" + order.get("order_number")
                    + " | stato=" + order.get("status")
                    + " | fornitore=" + order.get("supplier_id")
                    + " | righe=" + order.get("righe")
                    + " | GR=" + order.get("ricevimenti")
            );
        }

        List<Map<String, Object>> warehouses =
                jdbc.queryForList("""
                    SELECT id, code, active
                    FROM warehouse
                    WHERE tenant_id = ?
                      AND company_id = ?
                    ORDER BY id
                    LIMIT 10
                    """, 2L, 1L);

        System.out.println(
                "FIXTURE AUDIT: magazzini della società"
        );

        for (var warehouse : warehouses) {
            System.out.println(
                    "  magazzino=" + warehouse.get("id")
                    + " | codice=" + warehouse.get("code")
                    + " | attivo=" + warehouse.get("active")
            );
        }

        System.out.println(
                "FIXTURE AUDIT: nessun INSERT / UPDATE / DELETE"
        );
        System.out.println(
                "FIXTURE AUDIT: nessuna migrazione"
        );
    }
}
