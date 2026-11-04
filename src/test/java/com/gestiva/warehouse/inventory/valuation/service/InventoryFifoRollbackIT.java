package com.gestiva.warehouse.inventory.valuation.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.support.AbstractMySqlIntegrationTest;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.repository.WarehouseRepository;


import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("integration")
class InventoryFifoRollbackIT extends AbstractMySqlIntegrationTest {

    private static final Long TENANT_ID = 990000001L;
    private static final Long COMPANY_ID = 990000002L;
    private static final Long WAREHOUSE_ID = 990000003L;
    private static final Long ITEM_ID = 990000004L;

    @Autowired
    private InventoryValuationService valuationService;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private WarehouseRepository warehouseRepository;

    @MockitoSpyBean
    private InventoryLayerRepository inventoryLayerRepository;

    @PersistenceContext
    private EntityManager entityManager;


    @Test
    void fifoReversalShouldRollbackLayerUpdateOnCostMismatch()
            throws Exception {

        // Ulteriore verifica, oltre all'initializer obbligatorio.
        try (Connection connection =
                jdbc.getDataSource().getConnection()) {

            String catalog = connection.getCatalog();
            String username =
                    connection.getMetaData().getUserName();

            assertEquals("gestiva_test", catalog);
            assertTrue(
                    username.equals("gestiva_test_runner")
                    || username.startsWith("gestiva_test_runner@"),
                    "Utente MySQL di test non autorizzato"
            );

            assertFalse(connection.getAutoCommit() == false);
        }

        String marker = "IT-FIFO-RBK-" + UUID.randomUUID();

        Long movementId = null;
        Long layerId = null;
        Long consumptionId = null;

        try {

            // 1. Movimento sintetico OUT: 6 pezzi, costo 60.
            jdbc.update("""
                INSERT INTO inventory_movement (
                    tenant_id, company_id, warehouse_id,
                    location_id, item_id, movement_date,
                    movement_type, causal_code,
                    quantity, unit_cost, total_cost,
                    reference_type, notes,
                    reversed, reversal_of_movement_id,
                    created_at, updated_at
                )
                VALUES (
                    ?, ?, ?, NULL, ?, CURRENT_DATE,
                    'OUT', 'IT_FIFO_ROLLBACK',
                    6.000, 10.0000, 60.0000,
                    'INTEGRATION_TEST', ?,
                    0, NULL,
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """,
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID,
                ITEM_ID, marker
            );

            movementId = jdbc.queryForObject("""
                SELECT id
                FROM inventory_movement
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND notes = ?
                """,
                Long.class,
                TENANT_ID, COMPANY_ID, marker
            );

            assertNotNull(movementId);

            // 2. Layer originale: 10 pezzi, residuo 4.
            jdbc.update("""
                INSERT INTO inventory_layer (
                    tenant_id, company_id, warehouse_id,
                    location_id, item_id, source_movement_id,
                    layer_date, original_qty, remaining_qty,
                    unit_cost, closed,
                    created_at, updated_at
                )
                VALUES (
                    ?, ?, ?, NULL, ?, ?,
                    CURRENT_DATE, 10.000, 4.000,
                    10.0000, 0,
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """,
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID,
                ITEM_ID, movementId
            );

            layerId = jdbc.queryForObject("""
                SELECT id
                FROM inventory_layer
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND source_movement_id = ?
                """,
                Long.class,
                TENANT_ID, COMPANY_ID, movementId
            );

            assertNotNull(layerId);

            // 3. Consumo incoerente: quantità 6,
            //    ma costo 59 anziché 60.
            jdbc.update("""
                INSERT INTO inventory_layer_consumption (
                    tenant_id, company_id, warehouse_id,
                    out_movement_id, layer_id,
                    consumed_qty, unit_cost, total_cost,
                    created_at, updated_at
                )
                VALUES (
                    ?, ?, ?, ?, ?,
                    6.000, 10.0000, 59.0000,
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """,
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID,
                movementId, layerId
            );

            consumptionId = jdbc.queryForObject("""
                SELECT id
                FROM inventory_layer_consumption
                WHERE tenant_id = ?
                  AND company_id = ?
                  AND out_movement_id = ?
                  AND layer_id = ?
                """,
                Long.class,
                TENANT_ID, COMPANY_ID,
                movementId, layerId
            );

            assertNotNull(consumptionId);

            // Il lock viene simulato; JPA e MySQL sono reali.
            when(warehouseRepository
                    .findByTenantAndCompanyAndIdForUpdate(
                            TENANT_ID, COMPANY_ID, WAREHOUSE_ID))
                    .thenReturn(Optional.of(mock(Warehouse.class)));

            Long finalMovementId = movementId;


            // Il repository reale salva il layer.
            // Forziamo poi il flush SQL nella stessa transazione.
            AtomicBoolean flushObserved = new AtomicBoolean(false);
            Long finalLayerId = layerId;

            doAnswer(invocation -> {
                InventoryLayer saved = invocation.getArgument(0);

                // Il layer deve essere gestito dal contesto JPA.
                // Hibernate persiste le modifiche con dirty checking.
                assertTrue(
                    entityManager.contains(saved),
                    "Layer non gestito dall'EntityManager"
                );

                // Forza l'UPDATE SQL prima dell'errore FIFO.
                entityManager.flush();

                BigDecimal flushedQty = jdbc.queryForObject(
                    """
                    SELECT remaining_qty
                    FROM inventory_layer
                    WHERE id = ?
                      AND tenant_id = ?
                      AND company_id = ?
                    """,
                    BigDecimal.class,
                    finalLayerId,
                    TENANT_ID,
                    COMPANY_ID
                );

                assertNotNull(flushedQty);
                assertEquals(
                    0,
                    flushedQty.compareTo(
                        new BigDecimal("10.000")
                    ),
                    "FLUSH FALLITO: MySQL non vede 10 pezzi"
                );

                flushObserved.set(true);
                return saved;

            }).when(inventoryLayerRepository)
              .save(any(InventoryLayer.class));

            // 4. Chiamata attraverso il proxy Spring reale.
            BusinessException failure = assertThrows(
                    BusinessException.class,
                    () -> valuationService.reverseFifoValuation(
                            TENANT_ID,
                            COMPANY_ID,
                            WAREHOUSE_ID,
                            finalMovementId
                    )
            );

            assertTrue(
                    flushObserved.get(),
                    "Il flush SQL non è stato osservato"
            );

            assertTrue(
                    failure.getMessage().contains(
                            "valore dei consumi FIFO"),
                    "Attesa mancata quadratura economica"
            );

            // 5. Nuova lettura dopo la transazione fallita.
            BigDecimal remaining = jdbc.queryForObject("""
                SELECT remaining_qty
                FROM inventory_layer
                WHERE id = ?
                  AND tenant_id = ?
                  AND company_id = ?
                """,
                BigDecimal.class,
                layerId, TENANT_ID, COMPANY_ID
            );

            assertNotNull(remaining);

            assertEquals(
                    0,
                    remaining.compareTo(new BigDecimal("4.000")),
                    "ROLLBACK FALLITO: quantità layer modificata"
            );

            Integer closed = jdbc.queryForObject("""
                SELECT CAST(closed AS UNSIGNED)
                FROM inventory_layer
                WHERE id = ?
                  AND tenant_id = ?
                  AND company_id = ?
                """,
                Integer.class,
                layerId, TENANT_ID, COMPANY_ID
            );

            assertEquals(0, closed);

            System.out.println(
                    "ROLLBACK FIFO OK: layer ancora a 4.000 pezzi."
            );

        } finally {

            // Pulizia esclusiva della fixture creata dal test.
            if (consumptionId != null) {
                jdbc.update("""
                    DELETE FROM inventory_layer_consumption
                    WHERE id = ?
                      AND tenant_id = ?
                      AND company_id = ?
                      AND out_movement_id = ?
                    """,
                    consumptionId, TENANT_ID,
                    COMPANY_ID, movementId
                );
            }

            if (layerId != null) {
                jdbc.update("""
                    DELETE FROM inventory_layer
                    WHERE id = ?
                      AND tenant_id = ?
                      AND company_id = ?
                      AND source_movement_id = ?
                    """,
                    layerId, TENANT_ID,
                    COMPANY_ID, movementId
                );
            }

            if (movementId != null) {
                jdbc.update("""
                    DELETE FROM inventory_movement
                    WHERE id = ?
                      AND tenant_id = ?
                      AND company_id = ?
                      AND notes = ?
                    """,
                    movementId, TENANT_ID,
                    COMPANY_ID, marker
                );
            }
        }
    }
}
