package com.gestiva.purchasing.receipt.service;

import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = {
    "gestiva.purchasing.partial-goods-receipt.enabled=true"
})
class GoodsReceiptPartialCoordinatorEndToEndIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long SUPPLIER = 1L;
    private static final Long WAREHOUSE = 1L;
    private static final Long ITEM = 1L;

    private static final String ROLLBACK_MARKER =
            "COORDINATOR_E2E_1921_ROLLBACK";

    @Autowired
    private GoodsReceiptPartialCoordinator coordinator;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager manager;

    @MockitoBean
    private CompanyContext companyContext;

    private int count(String sql, Object... args) {
        Integer value = jdbc.queryForObject(
                sql, Integer.class, args
        );

        assertNotNull(value);
        return value;
    }

    private void equalDecimal(
            String expected,
            BigDecimal actual) {

        assertNotNull(actual);
        assertEquals(
                0,
                new BigDecimal(expected).compareTo(actual)
        );
    }

    private void verifyEnvironment() throws Exception {

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

        assertEquals(
                1,
                count("""
                    SELECT COUNT(*)
                    FROM supplier
                    WHERE tenant_id = 2 AND id = 1
                    """)
        );

        assertEquals(
                1,
                count("""
                    SELECT COUNT(*)
                    FROM warehouse
                    WHERE tenant_id = 2
                      AND company_id = 1
                      AND id = 1
                      AND active = 1
                    """)
        );

        assertEquals(
                1,
                count("""
                    SELECT COUNT(*)
                    FROM item
                    WHERE tenant_id = 2
                      AND id = 1
                      AND active = 1
                      AND item_type = 'PRODUCT'
                      AND base_uom_id = 1
                    """)
        );
    }

    private long sequenceValue() {
        Long result = jdbc.queryForObject(
                """
                SELECT next_number
                FROM document_sequence
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND document_type = ?
                """,
                Long.class,
                TENANT,
                COMPANY,
                DocumentSequenceService.GOODS_RECEIPT
        );

        assertNotNull(result);
        return result;
    }

    private long insertOrder(String number) {

        KeyHolder keys = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            var ps = connection.prepareStatement(
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

            ps.setLong(1, TENANT);
            ps.setLong(2, COMPANY);
            ps.setString(3, number);
            ps.setLong(4, SUPPLIER);

            return ps;
        }, keys);

        assertNotNull(keys.getKey());
        return keys.getKey().longValue();
    }

    private long insertOrderLine(long orderId) {

        KeyHolder keys = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            var ps = connection.prepareStatement(
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
                        'Fixture coordinatore E2E 1921',
                        20.000, 7.20, 0.00, 22.00,
                        144.00, 31.68, 175.68,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                    )
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setLong(1, TENANT);
            ps.setLong(2, orderId);
            ps.setLong(3, ITEM);

            return ps;
        }, keys);

        assertNotNull(keys.getKey());
        return keys.getKey().longValue();
    }

    private GoodsReceipt header(long orderId) {

        GoodsReceipt receipt = new GoodsReceipt();

        receipt.setTenantId(TENANT);
        receipt.setCompanyId(COMPANY);
        receipt.setPurchaseOrderId(orderId);
        receipt.setSupplierId(SUPPLIER);
        receipt.setWarehouseId(WAREHOUSE);
        receipt.setLocationId(null);
        receipt.setReceiptDate(LocalDate.of(2026, 11, 5));

        return receipt;
    }

    @Test
    void coordinatorCreatesAndRollsBackWholeRealWorkflow()
            throws Exception {

        verifyEnvironment();

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        String marker = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 16);

        String orderNumber = "PO-E2E-RB-" + marker;

        AtomicLong orderId = new AtomicLong();
        AtomicLong orderLineId = new AtomicLong();
        AtomicLong receiptId = new AtomicLong();
        AtomicLong movementId = new AtomicLong();
        AtomicLong layerId = new AtomicLong();

        long sequenceBefore = sequenceValue();

        TransactionTemplate transaction =
                new TransactionTemplate(manager);

        transaction.setIsolationLevel(
                TransactionDefinition.ISOLATION_READ_COMMITTED
        );
        transaction.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRED
        );
        transaction.setReadOnly(false);

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> transaction.execute(status -> {

                    long po = insertOrder(orderNumber);
                    long pol = insertOrderLine(po);

                    orderId.set(po);
                    orderLineId.set(pol);

                    // Da qui il percorso è reale:
                    // coordinator -> planning -> snapshot ->
                    // sequence -> writer -> posting -> FIFO.
                    Long gr = coordinator.create(
                            header(po),
                            Map.of(
                                    pol,
                                    new BigDecimal("8.000")
                            )
                    );

                    assertNotNull(gr);
                    assertTrue(gr > 0);
                    receiptId.set(gr);

                    entityManager.flush();

                    assertEquals(
                            1,
                            count("""
                                SELECT COUNT(*)
                                FROM goods_receipt
                                WHERE id = ?
                                  AND tenant_id = ?
                                  AND company_id = ?
                                  AND purchase_order_id = ?
                                """, gr, TENANT, COMPANY, po)
                    );

                    assertEquals(
                            1,
                            count("""
                                SELECT COUNT(*)
                                FROM goods_receipt_line
                                WHERE goods_receipt_id = ?
                                  AND purchase_order_line_id = ?
                                  AND item_id = ?
                                """, gr, pol, ITEM)
                    );

                    equalDecimal(
                            "8.000",
                            jdbc.queryForObject(
                                    """
                                    SELECT quantity_received
                                    FROM goods_receipt_line
                                    WHERE goods_receipt_id = ?
                                    """,
                                    BigDecimal.class,
                                    gr
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
                            gr
                    );

                    assertNotNull(movement);
                    movementId.set(movement);

                    equalDecimal(
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

                    equalDecimal(
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

                    equalDecimal(
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

                    equalDecimal(
                            "8.000",
                            jdbc.queryForObject(
                                    """
                                    SELECT original_qty
                                    FROM inventory_layer
                                    WHERE id = ?
                                    """,
                                    BigDecimal.class,
                                    layer
                            )
                    );

                    equalDecimal(
                            "8.000",
                            jdbc.queryForObject(
                                    """
                                    SELECT remaining_qty
                                    FROM inventory_layer
                                    WHERE id = ?
                                    """,
                                    BigDecimal.class,
                                    layer
                            )
                    );

                    equalDecimal(
                            "7.2000",
                            jdbc.queryForObject(
                                    """
                                    SELECT unit_cost
                                    FROM inventory_layer
                                    WHERE id = ?
                                    """,
                                    BigDecimal.class,
                                    layer
                            )
                    );

                    assertEquals(
                            sequenceBefore + 1,
                            sequenceValue()
                    );

                    System.out.println(
                            "E2E REAL: coordinatore e snapshot completati"
                    );
                    System.out.println(
                            "E2E REAL: numerazione reale eseguita"
                    );
                    System.out.println(
                            "E2E REAL: testata e riga GR create"
                    );
                    System.out.println(
                            "E2E REAL: movimento IN 8.000 / 57.6000"
                    );
                    System.out.println(
                            "E2E REAL: strato FIFO residuo 8.000"
                    );

                    throw new IllegalStateException(
                            ROLLBACK_MARKER
                    );
                })
        );

        assertEquals(
                ROLLBACK_MARKER,
                failure.getMessage()
        );

        assertTrue(orderId.get() > 0);
        assertTrue(orderLineId.get() > 0);
        assertTrue(receiptId.get() > 0);
        assertTrue(movementId.get() > 0);
        assertTrue(layerId.get() > 0);

        // Letture effettuate dopo il rollback.
        assertEquals(
                0,
                count(
                        "SELECT COUNT(*) FROM purchase_order WHERE id = ?",
                        orderId.get()
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
                        orderLineId.get()
                )
        );

        assertEquals(
                0,
                count(
                        "SELECT COUNT(*) FROM goods_receipt WHERE id = ?",
                        receiptId.get()
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
                        receiptId.get()
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

        assertEquals(
                sequenceBefore,
                sequenceValue(),
                "Numeratore non ripristinato dal rollback."
        );

        assertEquals(
                0,
                count(
                        """
                        SELECT COUNT(*)
                        FROM purchase_order
                        WHERE tenant_id = ?
                          AND company_id = ?
                          AND order_number = ?
                        """,
                        TENANT, COMPANY, orderNumber
                )
        );

        verify(companyContext, atLeastOnce())
                .requireCurrentCompanyId();

        System.out.println(
                "E2E ROLLBACK: ordine e riga annullati"
        );
        System.out.println(
                "E2E ROLLBACK: testata e riga GR annullate"
        );
        System.out.println(
                "E2E ROLLBACK: movimento e strato FIFO annullati"
        );
        System.out.println(
                "E2E ROLLBACK: numeratore ripristinato"
        );
        System.out.println(
                "E2E ROLLBACK: nessun documento temporaneo residuo"
        );
    }

    @Test
    void threePartialReceiptsReconcileAndRollBack()
            throws Exception {

        verifyEnvironment();

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        String marker = UUID.randomUUID()
                .toString().replace("-", "").substring(0, 16);
        String orderNumber = "PO-MULTI-RB-" + marker;

        long sequenceBefore = sequenceValue();

        java.util.List<Long> receiptIds =
                new java.util.ArrayList<>();
        java.util.List<Long> movementIds =
                new java.util.ArrayList<>();
        java.util.List<Long> layerIds =
                new java.util.ArrayList<>();

        AtomicLong orderId = new AtomicLong();
        AtomicLong orderLineId = new AtomicLong();

        TransactionTemplate tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(
                TransactionDefinition.ISOLATION_READ_COMMITTED);
        tx.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRED);
        tx.setReadOnly(false);

        String rollbackMarker = "MULTI_GR_1955_ROLLBACK";

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> tx.execute(status -> {

                    long po = insertOrder(orderNumber);
                    long pol = insertOrderLine(po);

                    orderId.set(po);
                    orderLineId.set(pol);

                    String[] quantities = {
                            "8.000", "7.000", "5.000"
                    };

                    String[] costs = {
                            "57.6000", "50.4000", "36.0000"
                    };

                    for (int i = 0; i < quantities.length; i++) {

                        Long gr = coordinator.create(
                                header(po),
                                Map.of(pol, new BigDecimal(quantities[i]))
                        );

                        assertNotNull(gr);
                        receiptIds.add(gr);

                        entityManager.flush();

                        equalDecimal(
                                quantities[i],
                                jdbc.queryForObject(
                                        """
                                        SELECT quantity_received
                                        FROM goods_receipt_line
                                        WHERE goods_receipt_id = ?
                                          AND purchase_order_line_id = ?
                                        """,
                                        BigDecimal.class,
                                        gr, pol
                                )
                        );

                        equalDecimal(
                                costs[i],
                                jdbc.queryForObject(
                                        """
                                        SELECT total_cost
                                        FROM goods_receipt_line
                                        WHERE goods_receipt_id = ?
                                          AND purchase_order_line_id = ?
                                        """,
                                        BigDecimal.class,
                                        gr, pol
                                )
                        );

                        Long movement = jdbc.queryForObject(
                                """
                                SELECT id
                                FROM inventory_movement
                                WHERE tenant_id = ?
                                  AND company_id = ?
                                  AND reference_type = 'GOODS_RECEIPT'
                                  AND reference_id = ?
                                  AND movement_type = 'IN'
                                  AND causal_code = 'PURCHASE_RECEIPT'
                                """,
                                Long.class,
                                TENANT, COMPANY, gr
                        );

                        assertNotNull(movement);
                        movementIds.add(movement);

                        equalDecimal(
                                quantities[i],
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

                        equalDecimal(
                                costs[i],
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
                                  AND source_movement_id = ?
                                """,
                                Long.class,
                                TENANT, COMPANY, movement
                        );

                        assertNotNull(layer);
                        layerIds.add(layer);

                        equalDecimal(
                                quantities[i],
                                jdbc.queryForObject(
                                        """
                                        SELECT original_qty
                                        FROM inventory_layer
                                        WHERE id = ?
                                        """,
                                        BigDecimal.class,
                                        layer
                                )
                        );

                        System.out.println(
                                "MULTI GR: ricevimento " + (i + 1)
                                + " quantità=" + quantities[i]
                                + " costo=" + costs[i]
                        );
                    }

                    assertEquals(
                            3,
                            count("""
                                  SELECT COUNT(*)
                                  FROM goods_receipt
                                  WHERE tenant_id = ?
                                    AND company_id = ?
                                    AND purchase_order_id = ?
                                  """, TENANT, COMPANY, po)
                    );

                    equalDecimal(
                            "20.000",
                            jdbc.queryForObject(
                                    """
                                    SELECT SUM(quantity_received)
                                    FROM goods_receipt_line
                                    WHERE purchase_order_line_id = ?
                                    """,
                                    BigDecimal.class, pol
                            )
                    );

                    equalDecimal(
                            "144.0000",
                            jdbc.queryForObject(
                                    """
                                    SELECT SUM(total_cost)
                                    FROM goods_receipt_line
                                    WHERE purchase_order_line_id = ?
                                    """,
                                    BigDecimal.class, pol
                            )
                    );

                    assertEquals(
                            sequenceBefore + 3,
                            sequenceValue()
                    );

                    assertThrows(
                            com.gestiva.common.exception.BusinessException.class,
                            () -> coordinator.create(
                                    header(po),
                                    Map.of(pol, new BigDecimal("1.000"))
                            )
                    );

                    assertEquals(
                            3,
                            count("""
                                  SELECT COUNT(*)
                                  FROM goods_receipt
                                  WHERE tenant_id = ?
                                    AND company_id = ?
                                    AND purchase_order_id = ?
                                  """, TENANT, COMPANY, po)
                    );

                    assertEquals(
                            sequenceBefore + 3,
                            sequenceValue()
                    );

                    System.out.println(
                            "MULTI GR: quarto ricevimento rifiutato"
                    );
                    System.out.println(
                            "MULTI GR: 20 PZ / 144.0000 riconciliati"
                    );

                    throw new IllegalStateException(rollbackMarker);
                })
        );

        assertEquals(rollbackMarker, failure.getMessage());

        assertEquals(3, receiptIds.size());
        assertEquals(3, movementIds.size());
        assertEquals(3, layerIds.size());

        assertEquals(
                0,
                count("SELECT COUNT(*) FROM purchase_order WHERE id = ?",
                        orderId.get())
        );

        assertEquals(
                0,
                count("SELECT COUNT(*) FROM purchase_order_line WHERE id = ?",
                        orderLineId.get())
        );

        for (Long gr : receiptIds) {
            assertEquals(
                    0,
                    count("SELECT COUNT(*) FROM goods_receipt WHERE id = ?", gr)
            );
            assertEquals(
                    0,
                    count("""
                          SELECT COUNT(*)
                          FROM goods_receipt_line
                          WHERE goods_receipt_id = ?
                          """, gr)
            );
        }

        for (Long movement : movementIds) {
            assertEquals(
                    0,
                    count("SELECT COUNT(*) FROM inventory_movement WHERE id = ?",
                            movement)
            );
        }

        for (Long layer : layerIds) {
            assertEquals(
                    0,
                    count("SELECT COUNT(*) FROM inventory_layer WHERE id = ?",
                            layer)
            );
        }

        assertEquals(sequenceBefore, sequenceValue());

        assertEquals(
                0,
                count("""
                      SELECT COUNT(*)
                      FROM purchase_order
                      WHERE tenant_id = ?
                        AND company_id = ?
                        AND order_number = ?
                      """, TENANT, COMPANY, orderNumber)
        );

        System.out.println(
                "MULTI GR ROLLBACK: tre ricevimenti, "
                + "tre movimenti, tre strati e numeratore ripristinati"
        );
    }

}
