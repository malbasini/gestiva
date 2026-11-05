package com.gestiva.purchasing.receipt.service;

import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptPartialRollbackResidueIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    private int count(String sql, Object... parameters) {
        Integer result = jdbc.queryForObject(
                sql,
                Integer.class,
                parameters
        );
        assertNotNull(result);
        return result;
    }

    @Test
    void noTemporaryRollbackDocumentsRemain()
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
                    || username.startsWith("gestiva_test_runner@")
            );
        }

        // Controlliamo i prefissi distintivi impiegati
        // esclusivamente dalle fixture dei passi
        // 1915 e 1919.

        int purchaseOrders = count(
                """
                SELECT COUNT(*)
                FROM purchase_order
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND (
                      order_number LIKE 'PO-RB-%'
                      OR order_number LIKE 'PO-FIFO-RB-%'
                  )
                """,
                2L, 1L
        );

        int receipts = count(
                """
                SELECT COUNT(*)
                FROM goods_receipt
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND (
                      receipt_number LIKE 'GR-RB-%'
                      OR receipt_number LIKE 'GR-FIFO-RB-%'
                  )
                """,
                2L, 1L
        );

        assertEquals(
                0, purchaseOrders,
                "Ordini temporanei di rollback ancora presenti."
        );

        assertEquals(
                0, receipts,
                "Ricevimenti temporanei di rollback ancora presenti."
        );

        // I movimenti usano reference_type/reference_id.
        // Verifichiamo anche che non esistano movimenti
        // riferiti a ricevimenti temporanei rimasti.
        int movements = count(
                """
                SELECT COUNT(*)
                FROM inventory_movement m
                JOIN goods_receipt gr
                  ON gr.id = m.reference_id
                 AND gr.tenant_id = m.tenant_id
                 AND gr.company_id = m.company_id
                WHERE m.tenant_id = ?
                  AND m.company_id = ?
                  AND m.reference_type = 'GOODS_RECEIPT'
                  AND (
                      gr.receipt_number LIKE 'GR-RB-%'
                      OR gr.receipt_number LIKE 'GR-FIFO-RB-%'
                  )
                """,
                2L, 1L
        );

        assertEquals(
                0, movements,
                "Movimenti associati a fixture temporanee."
        );

        System.out.println(
                "REGRESSION MYSQL: nessun PO temporaneo residuo"
        );
        System.out.println(
                "REGRESSION MYSQL: nessun GR temporaneo residuo"
        );
        System.out.println(
                "REGRESSION MYSQL: nessun movimento associato a GR temporanei"
        );
        System.out.println(
                "REGRESSION MYSQL: verifiche residue in sola lettura"
        );
    }
}
