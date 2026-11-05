package com.gestiva.purchasing.receipt.service;

import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptPartialFifoPreflightIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void verifiesFifoPrerequisitesReadOnly() throws Exception {

        try (Connection connection =
                     jdbc.getDataSource().getConnection()) {

            assertEquals("gestiva_test", connection.getCatalog());

            String user = connection.getMetaData().getUserName();

            assertTrue(
                    user.equals("gestiva_test_runner")
                    || user.startsWith("gestiva_test_runner@"),
                    "Utente MySQL non autorizzato."
            );
        }

        System.out.println("FIFO PREFLIGHT: DB protetto confermato");

        List<Map<String, Object>> tables = jdbc.queryForList("""
            SELECT table_name, engine
            FROM information_schema.tables
            WHERE table_schema = DATABASE()
              AND table_name IN (
                  'item',
                  'unit_of_measure',
                  'inventory_movement',
                  'inventory_layer',
                  'goods_receipt',
                  'goods_receipt_line'
              )
            ORDER BY table_name
            """);

        assertEquals(6, tables.size());

        System.out.println("FIFO PREFLIGHT: engine delle tabelle");

        for (var table : tables) {
            System.out.println(
                "  " + table.get("table_name")
                + " | engine=" + table.get("engine")
            );

            assertEquals(
                "InnoDB",
                String.valueOf(table.get("engine")),
                "Il rollback richiede tabelle transazionali InnoDB."
            );
        }

        List<Map<String, Object>> columns = jdbc.queryForList("""
            SELECT table_name,
                   column_name,
                   column_type,
                   is_nullable,
                   column_default,
                   extra
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name IN (
                  'inventory_movement',
                  'inventory_layer',
                  'item'
              )
            ORDER BY table_name, ordinal_position
            """);

        System.out.println(
            "FIFO PREFLIGHT: colonne movimenti, strati e articoli"
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

        List<Map<String, Object>> constraints = jdbc.queryForList("""
            SELECT table_name,
                   constraint_name,
                   column_name,
                   referenced_table_name,
                   referenced_column_name
            FROM information_schema.key_column_usage
            WHERE table_schema = DATABASE()
              AND table_name IN (
                  'item',
                  'inventory_movement',
                  'inventory_layer'
              )
              AND referenced_table_name IS NOT NULL
            ORDER BY table_name,
                     constraint_name,
                     ordinal_position
            """);

        System.out.println("FIFO PREFLIGHT: foreign key");

        for (var constraint : constraints) {
            System.out.println(
                "  " + constraint.get("table_name")
                + "." + constraint.get("column_name")
                + " -> "
                + constraint.get("referenced_table_name")
                + "." + constraint.get("referenced_column_name")
            );
        }

        List<Map<String, Object>> products = jdbc.queryForList("""
            SELECT i.id,
                   i.code,
                   i.name,
                   i.item_type,
                   i.base_uom_id,
                   i.tracking_type,
                   i.active
            FROM item i
            WHERE i.tenant_id = ?
              AND i.item_type = 'PRODUCT'
              AND i.active = 1
            ORDER BY i.id
            LIMIT 10
            """, 2L);

        System.out.println(
            "FIFO PREFLIGHT: prodotti attivi tenant 2 = "
            + products.size() + " (massimo 10 mostrati)"
        );

        for (var product : products) {
            System.out.println(
                "  item=" + product.get("id")
                + " | codice=" + product.get("code")
                + " | tipo=" + product.get("item_type")
                + " | UOM=" + product.get("base_uom_id")
                + " | tracking=" + product.get("tracking_type")
            );
        }

        List<Map<String, Object>> uoms = jdbc.queryForList("""
            SELECT id, code, name, active
            FROM unit_of_measure
            WHERE tenant_id = ?
            ORDER BY id
            LIMIT 10
            """, 2L);

        System.out.println(
            "FIFO PREFLIGHT: unità di misura disponibili"
        );

        for (var uom : uoms) {
            System.out.println(
                "  UOM id=" + uom.get("id")
                + " | codice=" + uom.get("code")
                + " | attiva=" + uom.get("active")
            );
        }

        System.out.println(
            "FIFO PREFLIGHT: nessun dato modificato"
        );
        System.out.println(
            "FIFO PREFLIGHT: nessuna migrazione"
        );
    }
}
