package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferFifoPlannerTest {

    private static InventoryLayer layer(
            Long id,
            Long locationId,
            LocalDate date,
            String quantity,
            String cost) {

        InventoryLayer layer = new InventoryLayer();

        ReflectionTestUtils.setField(layer, "id", id);

        layer.setTenantId(2L);
        layer.setCompanyId(3L);
        layer.setWarehouseId(2L);
        layer.setItemId(7L);
        layer.setLocationId(locationId);

        layer.setLayerDate(date);
        layer.setOriginalQty(new BigDecimal(quantity));
        layer.setRemainingQty(new BigDecimal(quantity));
        layer.setUnitCost(new BigDecimal(cost));
        layer.setClosed(false);

        return layer;
    }

    @Test
    void mustPreserveSourceFifoCompositionWithoutMutatingLayers()
            throws Exception {

        InventoryLayer first = layer(
                201L, 101L,
                LocalDate.of(2026, 10, 1),
                "5.000", "7.2000"
        );

        InventoryLayer otherLocation = layer(
                202L, 102L,
                LocalDate.of(2026, 10, 2),
                "9.000", "6.0000"
        );

        InventoryLayer third = layer(
                203L, 101L,
                LocalDate.of(2026, 10, 3),
                "8.000", "8.1000"
        );

        List<InventoryLayer> candidates =
                List.of(first, otherLocation, third);

        Class<?> plannerClass;

        try {
            plannerClass = Class.forName(
                    "com.gestiva.warehouse.inventory.movement.service."
                    + "InventoryLocationTransferFifoPlanner"
            );
        } catch (ClassNotFoundException exception) {
            fail("STOP-AMZ-PLANNER-NON-IMPLEMENTATO");
            return;
        }

        Object planner = plannerClass
                .getDeclaredConstructor()
                .newInstance();

        Method planMethod = plannerClass.getMethod(
                "plan",
                List.class,
                Long.class,
                BigDecimal.class
        );

        Object result = planMethod.invoke(
                planner,
                candidates,
                101L,
                new BigDecimal("7.000")
        );

        assertInstanceOf(List.class, result);

        List<?> allocations = (List<?>) result;

        assertEquals(2, allocations.size());

        Object firstAllocation = allocations.get(0);
        Object secondAllocation = allocations.get(1);

        assertAllocation(
                firstAllocation,
                201L,
                "5.000",
                "7.2000",
                LocalDate.of(2026, 10, 1)
        );

        assertAllocation(
                secondAllocation,
                203L,
                "2.000",
                "8.1000",
                LocalDate.of(2026, 10, 3)
        );

        // Il piano deve essere puramente preparatorio:
        // nessuna modifica alle entità FIFO originali.
        assertEquals(
                0,
                new BigDecimal("5.000").compareTo(
                        first.getRemainingQty()
                )
        );

        assertEquals(
                0,
                new BigDecimal("9.000").compareTo(
                        otherLocation.getRemainingQty()
                )
        );

        assertEquals(
                0,
                new BigDecimal("8.000").compareTo(
                        third.getRemainingQty()
                )
        );

        assertFalse(first.isClosed());
        assertFalse(otherLocation.isClosed());
        assertFalse(third.isClosed());
    }

    private static void assertAllocation(
            Object allocation,
            Long expectedLayerId,
            String expectedQuantity,
            String expectedCost,
            LocalDate expectedDate) throws Exception {

        Class<?> type = allocation.getClass();

        Object sourceLayerId =
                type.getMethod("sourceLayerId").invoke(allocation);

        Object quantity =
                type.getMethod("quantity").invoke(allocation);

        Object unitCost =
                type.getMethod("unitCost").invoke(allocation);

        Object layerDate =
                type.getMethod("layerDate").invoke(allocation);

        assertEquals(expectedLayerId, sourceLayerId);

        assertInstanceOf(BigDecimal.class, quantity);
        assertInstanceOf(BigDecimal.class, unitCost);

        assertEquals(
                0,
                new BigDecimal(expectedQuantity)
                        .compareTo((BigDecimal) quantity)
        );

        assertEquals(
                0,
                new BigDecimal(expectedCost)
                        .compareTo((BigDecimal) unitCost)
        );

        assertEquals(expectedDate, layerDate);
    }
}
