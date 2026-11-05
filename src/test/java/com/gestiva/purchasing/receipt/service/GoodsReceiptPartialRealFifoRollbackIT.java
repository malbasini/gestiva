package com.gestiva.purchasing.receipt.service;

import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = {
    "gestiva.purchasing.partial-goods-receipt.enabled=true"
})
class GoodsReceiptPartialRealFifoRollbackIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long SUPPLIER = 1L;
    private static final Long WAREHOUSE = 1L;
    private static final Long ITEM = 1L;

    @Autowired
    private GoodsReceiptPartialPersistenceWriter writer;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager manager;

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private void sameNumber(
            String expected,
            BigDecimal actual) {

        assertNotNull(actual);
        assertEquals(
                0,
                bd(expected).compareTo(actual),
                "Valore economico o quantità non coerente."
        );
    }

    private int count(String sql, Object... args) {
        Integer result = jdbc.queryForObject(
                sql, Integer.class, args
        );

        assertNotNull(result);
        return result;
    }

    private void checkTestEnvironment() throws Exception {

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

        assertEquals(
                1,
                count("""
                    SELECT COUNT(*)
                    FROM supplier
                    WHERE id = ?
                      AND tenant_id = ?
                    """, SUPPLIER, TENANT)
        );

        assertEquals(
                1,
                count("""
                    SELECT COUNT(*)
                    FROM warehouse
                    WHERE id = ?
                      AND tenant_id = ?
                      AND company_id = ?
                      AND active = 1
                    """, WAREHOUSE, TENANT, COMPANY)
        );

        assertEquals(
                1,
                count("""
                    SELECT COUNT(*)
                    FROM item
                    WHERE id = ?
                      AND tenant_id = ?
                      AND item_type = 'PRODUCT'
                      AND active = 1
                      AND base_uom_id = 1
                    """, ITEM, TENANT)
        );
    }

    private long insertOrder(String number) {

        KeyHolder keys = new GeneratedKeyHolder();

        jdbc.update(connection -> {

            var statement = connection.prepareStatement(
                    """
                    INSERT INTO purchase_order (
                        tenant_id,
                        company_id,
                        order_number,
                        order_date,
                        supplier_id,
                        status,
                        currency_code,
                        subtotal_amount,
                        tax_amount,
                        total_amount,
                        created_at,
                        updated_at
                    )
                    VALUES (
                        ?, ?, ?, CURRENT_DATE,
                        ?, 'CONFIRMED', 'EUR',
                        144.00, 31.68, 175.68,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                    )
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );

            statement.setLong(1, TENANT);
            statement.setLong(2, COMPANY);
            statement.setString(3, number);
            statement.setLong(4, SUPPLIER);

            return statement;

        }, keys);

        assertNotNull(keys.getKey());
        return keys.getKey().longValue();
    }

    private long insertOrderLine(long orderId) {

        KeyHolder keys = new GeneratedKeyHolder();

        jdbc.update(connection -> {

            var statement = connection.prepareStatement(
                    """
                    INSERT INTO purchase_order_line (
                        tenant_id,
                        purchase_order_id,
                        line_no,
                        item_id,
                        uom_id,
                        conversion_factor,
                        description,
                        quantity,
                        unit_price,
                        discount_pct,
                        tax_pct,
                        line_subtotal,
                        tax_amount,
                        line_total,
                        created_at,
                        updated_at
                    )
                    VALUES (
                        ?, ?, 1, ?, 1, 1.000000,
                        'Fixture rollback FIFO reale',
                        20.000, 7.20, 0.00, 22.00,
                        144.00, 31.68, 175.68,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                    )
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );

            statement.setLong(1, TENANT);
            statement.setLong(2, orderId);
            statement.setLong(3, ITEM);

            return statement;

        }, keys);

        assertNotNull(keys.getKey());
        return keys.getKey().longValue();
    }

    private GoodsReceiptPartialDocumentAssembler.DocumentPlan
    plan(
            long orderId,
            long lineId,
            String receiptNumber) {

        GoodsReceipt header = new GoodsReceipt();

        header.setTenantId(TENANT);
        header.setCompanyId(COMPANY);
        header.setPurchaseOrderId(orderId);
        header.setSupplierId(SUPPLIER);
        header.setWarehouseId(WAREHOUSE);
        header.setLocationId(null);
        header.setReceiptNumber(receiptNumber);
        header.setReceiptDate(LocalDate.of(2026, 11, 5));

        PurchaseOrderLine orderLine =
                mock(PurchaseOrderLine.class);

        when(orderLine.getId()).thenReturn(lineId);
        when(orderLine.getTenantId()).thenReturn(TENANT);
        when(orderLine.getPurchaseOrderId())
                .thenReturn(orderId);
        when(orderLine.getLineNo()).thenReturn(1);
        when(orderLine.getItemId()).thenReturn(ITEM);
        when(orderLine.getDescription())
                .thenReturn("Fixture rollback FIFO reale");

        var planned =
                GoodsReceiptPartialFifoLineCalculator.calculate(
                        bd("20.000"),
                        bd("0.000"),
                        bd("8.000"),
                        bd("144.00")
                );

        return GoodsReceiptPartialDocumentAssembler.prepare(
                header,
                List.of(
                        new GoodsReceiptPartialDocumentAssembler.LineInput(
                                orderLine,
                                planned
                        )
                )
        );
    }

    @Test
    void realPostingAndFifoLayerAreRolledBack()
            throws Exception {

        checkTestEnvironment();

        String marker = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 16);

        String orderNumber = "PO-FIFO-RB-" + marker;
        String receiptNumber = "GR-FIFO-RB-" + marker;

        AtomicLong poId = new AtomicLong();
        AtomicLong poLineId = new AtomicLong();
        AtomicLong grId = new AtomicLong();
        AtomicLong movementId = new AtomicLong();
        AtomicLong layerId = new AtomicLong();

        TransactionTemplate transaction =
                new TransactionTemplate(manager);

        transaction.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRED
        );

        transaction.setIsolationLevel(
                TransactionDefinition.ISOLATION_READ_COMMITTED
        );

        transaction.setReadOnly(false);

        // Non intercettiamo posting, movimento o FIFO:
        // vengono eseguiti i componenti Spring reali.
        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> transaction.execute(status -> {

                    long order = insertOrder(orderNumber);
                    long orderLine = insertOrderLine(order);

                    poId.set(order);
                    poLineId.set(orderLine);

                    var documentPlan = plan(
                            order,
                            orderLine,
                            receiptNumber
                    );

                    // Writer reale:
                    // GR + righe + movimento + strato FIFO.
                    long receipt = writer.write(documentPlan);

                    grId.set(receipt);

                    // Assicura che anche gli INSERT JPA
                    // siano stati inviati a MySQL.
                    entityManager.flush();

                    assertEquals(
                            1,
                            count("""
                                SELECT COUNT(*)
                                FROM goods_receipt
                                WHERE id = ?
                                  AND tenant_id = ?
                                  AND company_id = ?
                                """, receipt, TENANT, COMPANY)
                    );

                    assertEquals(
                            1,
                            count("""
                                SELECT COUNT(*)
                                FROM goods_receipt_line
                                WHERE goods_receipt_id = ?
                                  AND tenant_id = ?
                                """, receipt, TENANT)
                    );

                    sameNumber(
                            "8.000",
                            jdbc.queryForObject(
                                    """
                                    SELECT quantity_received
                                    FROM goods_receipt_line
                                    WHERE goods_receipt_id = ?
                                    """,
                                    BigDecimal.class,
                                    receipt
                            )
                    );

                    Long movement = jdbc.queryForObject(
                            """
                            SELECT id
                            FROM inventory_movement
                            WHERE tenant_id = ?
                              AND company_id = ?
                              AND warehouse_id = ?
                              AND item_id = ?
                              AND reference_type = 'GOODS_RECEIPT'
                              AND reference_id = ?
                              AND causal_code = 'PURCHASE_RECEIPT'
                              AND movement_type = 'IN'
                            """,
                            Long.class,
                            TENANT,
                            COMPANY,
                            WAREHOUSE,
                            ITEM,
                            receipt
                    );

                    assertNotNull(movement);
                    movementId.set(movement);

                    sameNumber(
                            "8.000",
                            jdbc.queryForObject(
                                    """
                                    SELECT quantity
                                    FROM inventory_movement
                                    WHERE id = ?
                                    """,
                                    BigDecimal.class,
                                    movement
                            )
                    );

                    sameNumber(
                            "7.2000",
                            jdbc.queryForObject(
                                    """
                                    SELECT unit_cost
                                    FROM inventory_movement
                                    WHERE id = ?
                                    """,
                                    BigDecimal.class,
                                    movement
                            )
                    );

                    sameNumber(
                            "57.6000",
                            jdbc.queryForObject(
                                    """
                                    SELECT total_cost
                                    FROM inventory_movement
                                    WHERE id = ?
                                    """,
                                    BigDecimal.class,
                                    movement
                            )
                    );

                    Long layer = jdbc.queryForObject(
                            """
                            SELECT id
                            FROM inventory_layer
                            WHERE tenant_id = ?
                              AND company_id = ?
                              AND warehouse_id = ?
                              AND item_id = ?
                              AND source_movement_id = ?
                            """,
                            Long.class,
                            TENANT,
                            COMPANY,
                            WAREHOUSE,
                            ITEM,
                            movement
                    );

                    assertNotNull(layer);
                    layerId.set(layer);

                    Map<String, Object> fifo = jdbc.queryForMap(
                            """
                            SELECT original_qty,
                                   remaining_qty,
                                   unit_cost,
                                   closed
                            FROM inventory_layer
                            WHERE id = ?
                            """,
                            layer
                    );

                    sameNumber(
                            "8.000",
                            (BigDecimal) fifo.get("original_qty")
                    );

                    sameNumber(
                            "8.000",
                            (BigDecimal) fifo.get("remaining_qty")
                    );

                    sameNumber(
                            "7.2000",
                            (BigDecimal) fifo.get("unit_cost")
                    );

                    // bit(1): MySQL può restituire byte[].
                    Object closed = fifo.get("closed");

                    boolean isClosed;

                    if (closed instanceof byte[] bytes) {
                        isClosed = bytes.length > 0
                                && bytes[0] != 0;
                    } else if (closed instanceof Boolean value) {
                        isClosed = value;
                    } else if (closed instanceof Number value) {
                        isClosed = value.intValue() != 0;
                    } else {
                        throw new AssertionError(
                                "Tipo colonna closed inatteso: "
                                + closed
                        );
                    }

                    assertFalse(isClosed);

                    System.out.println(
                            "FIFO REAL: testata e riga GR inserite"
                    );
                    System.out.println(
                            "FIFO REAL: movimento IN 8.000 / 7.2000 / 57.6000"
                    );
                    System.out.println(
                            "FIFO REAL: strato FIFO aperto, residuo 8.000"
                    );

                    // Forziamo il fallimento DOPO che MySQL
                    // ha ricevuto tutte e sei le scritture.
                    throw new IllegalStateException(
                            "ROLLBACK_FIFO_1919_INTENZIONALE"
                    );
                })
        );

        assertEquals(
                "ROLLBACK_FIFO_1919_INTENZIONALE",
                failure.getMessage()
        );

        for (AtomicLong id : List.of(
                poId, poLineId, grId, movementId, layerId
        )) {
            assertTrue(id.get() > 0);
        }

        // Letture effettuate dopo la terminazione
        // della transazione e del rollback.
        assertEquals(
                0,
                count(
                        "SELECT COUNT(*) FROM purchase_order WHERE id = ?",
                        poId.get()
                )
        );

        assertEquals(
                0,
                count(
                        """
                        SELECT COUNT(*)
                        FROM purchase_order_line
                        WHERE id = ?
                        """,
                        poLineId.get()
                )
        );

        assertEquals(
                0,
                count(
                        "SELECT COUNT(*) FROM goods_receipt WHERE id = ?",
                        grId.get()
                )
        );

        assertEquals(
                0,
                count(
                        """
                        SELECT COUNT(*)
                        FROM goods_receipt_line
                        WHERE goods_receipt_id = ?
                        """,
                        grId.get()
                )
        );

        assertEquals(
                0,
                count(
                        """
                        SELECT COUNT(*)
                        FROM inventory_movement
                        WHERE id = ?
                        """,
                        movementId.get()
                )
        );

        assertEquals(
                0,
                count(
                        """
                        SELECT COUNT(*)
                        FROM inventory_layer
                        WHERE id = ?
                        """,
                        layerId.get()
                )
        );

        // Controlli aggiuntivi tramite riferimenti
        // documentali, indipendenti dagli ID.
        assertEquals(
                0,
                count(
                        """
                        SELECT COUNT(*)
                        FROM purchase_order
                        WHERE order_number = ?
                          AND tenant_id = ?
                          AND company_id = ?
                        """,
                        orderNumber, TENANT, COMPANY
                )
        );

        assertEquals(
                0,
                count(
                        """
                        SELECT COUNT(*)
                        FROM goods_receipt
                        WHERE receipt_number = ?
                          AND tenant_id = ?
                          AND company_id = ?
                        """,
                        receiptNumber, TENANT, COMPANY
                )
        );

        assertEquals(
                0,
                count(
                        """
                        SELECT COUNT(*)
                        FROM inventory_movement
                        WHERE reference_type = 'GOODS_RECEIPT'
                          AND reference_id = ?
                          AND tenant_id = ?
                          AND company_id = ?
                        """,
                        grId.get(), TENANT, COMPANY
                )
        );

        System.out.println(
                "FIFO ROLLBACK: ordine e riga annullati"
        );
        System.out.println(
                "FIFO ROLLBACK: testata e riga GR annullate"
        );
        System.out.println(
                "FIFO ROLLBACK: movimento annullato"
        );
        System.out.println(
                "FIFO ROLLBACK: strato FIFO annullato"
        );
        System.out.println(
                "FIFO ROLLBACK: nessun documento residuo"
        );
    }
}
