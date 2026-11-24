package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferDestinationLayerFactoryTest {

    @Test
    void destinationLayerMustPreserveOriginalFifoProperties()
            throws Exception {

        Class<?> factory;

        try {
            factory = Class.forName(
                    "com.gestiva.warehouse.inventory.movement.service."
                    + "InventoryLocationTransferDestinationLayerFactory"
            );
        } catch (ClassNotFoundException exception) {
            fail("STOP-AOJ-DESTINATION-LAYER-FACTORY-NON-IMPLEMENTATA");
            return;
        }

        Method create = factory.getDeclaredMethod(
                "create",
                Long.class, // tenantId
                Long.class, // companyId
                Long.class, // warehouseId
                Long.class, // itemId
                Long.class, // targetLocationId
                Long.class, // inboundMovementId
                InventoryLocationTransferFifoPlanner.Allocation.class
        );

        assertTrue(
                Modifier.isStatic(create.getModifiers()),
                "STOP-AOJ-FACTORY-NON-STATICA"
        );

        assertEquals(
                InventoryLayer.class,
                create.getReturnType()
        );

        var allocation =
                new InventoryLocationTransferFifoPlanner.Allocation(
                        11L,
                        new BigDecimal("3.000"),
                        new BigDecimal("7.2000"),
                        LocalDate.of(2026, 9, 15)
                );

        InventoryLayer layer;

        try {
            layer = (InventoryLayer) create.invoke(
                    null,
                    2L,   // tenant
                    1L,   // company
                    5L,   // warehouse
                    10L,  // item
                    200L, // target location
                    900L, // inbound movement
                    allocation
            );
        } catch (InvocationTargetException exception) {
            throw new AssertionError(
                    "STOP-AOJ-FACTORY-ERRORE-CREAZIONE",
                    exception.getCause()
            );
        }

        assertNotNull(layer);

        // Identificativo generato soltanto alla persistenza.
        assertNull(layer.getId());

        assertEquals(2L, layer.getTenantId());
        assertEquals(1L, layer.getCompanyId());
        assertEquals(5L, layer.getWarehouseId());
        assertEquals(10L, layer.getItemId());
        assertEquals(200L, layer.getLocationId());

        // Il nuovo layer appartiene al movimento IN,
        // non al vecchio movimento di origine.
        assertEquals(900L, layer.getSourceMovementId());

        // La data FIFO deve restare quella del layer storico.
        assertEquals(
                LocalDate.of(2026, 9, 15),
                layer.getLayerDate()
        );

        assertEquals(
                new BigDecimal("3.000"),
                layer.getOriginalQty()
        );

        assertEquals(
                new BigDecimal("3.000"),
                layer.getRemainingQty()
        );

        assertEquals(
                new BigDecimal("7.2000"),
                layer.getUnitCost()
        );

        assertFalse(layer.isClosed());
    }
}
