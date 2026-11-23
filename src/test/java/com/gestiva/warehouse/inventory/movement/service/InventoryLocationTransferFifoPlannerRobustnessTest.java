package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferFifoPlannerRobustnessTest {

    private final InventoryLocationTransferFifoPlanner planner =
            new InventoryLocationTransferFifoPlanner();

    private InventoryLayer layer(
            long id,
            long location,
            String remaining,
            String unitCost,
            boolean closed) {

        InventoryLayer result = new InventoryLayer();

        ReflectionTestUtils.setField(result, "id", id);

        result.setTenantId(2L);
        result.setCompanyId(3L);
        result.setWarehouseId(2L);
        result.setItemId(7L);
        result.setLocationId(location);

        result.setLayerDate(LocalDate.of(2026, 10, 1));
        result.setOriginalQty(new BigDecimal("20.000"));
        result.setRemainingQty(new BigDecimal(remaining));
        result.setUnitCost(new BigDecimal(unitCost));
        result.setClosed(closed);

        return result;
    }

    @Test
    void insufficientQuantityInSourceMustBeRejected() {

        InventoryLayer source =
                layer(201L, 101L, "3.000", "7.2000", false);

        InventoryLayer anotherLocation =
                layer(202L, 102L, "20.000", "8.1000", false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> planner.plan(
                        List.of(source, anotherLocation),
                        101L,
                        new BigDecimal("7.000")
                )
        );

        assertEquals(
                "Strati FIFO insufficienti nell'ubicazione di origine.",
                exception.getMessage()
        );

        assertEquals(
                0,
                new BigDecimal("3.000")
                        .compareTo(source.getRemainingQty())
        );

        assertEquals(
                0,
                new BigDecimal("20.000")
                        .compareTo(anotherLocation.getRemainingQty())
        );
    }

    @Test
    void quantityWithMoreThanThreeDecimalsMustBeRejected() {

        InventoryLayer source =
                layer(201L, 101L, "10.000", "7.2000", false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> planner.plan(
                        List.of(source),
                        101L,
                        new BigDecimal("1.0001")
                )
        );

        assertEquals(
                "La quantità da trasferire non può superare tre decimali.",
                exception.getMessage()
        );
    }

    @Test
    void closedLayerMustBeSkipped() {

        InventoryLayer closed =
                layer(201L, 101L, "5.000", "7.2000", true);

        InventoryLayer open =
                layer(202L, 101L, "6.000", "8.1000", false);

        var allocations = planner.plan(
                List.of(closed, open),
                101L,
                new BigDecimal("2.000")
        );

        assertEquals(1, allocations.size());
        assertEquals(202L, allocations.get(0).sourceLayerId());

        assertEquals(
                0,
                new BigDecimal("2.000")
                        .compareTo(allocations.get(0).quantity())
        );

        assertTrue(closed.isClosed());
    }

    @Test
    void exhaustedLayerMustBeSkipped() {

        InventoryLayer exhausted =
                layer(201L, 101L, "0.000", "7.2000", false);

        InventoryLayer available =
                layer(202L, 101L, "4.000", "8.1000", false);

        var allocations = planner.plan(
                List.of(exhausted, available),
                101L,
                new BigDecimal("3.000")
        );

        assertEquals(1, allocations.size());
        assertEquals(202L, allocations.get(0).sourceLayerId());

        assertEquals(
                0,
                new BigDecimal("3.000")
                        .compareTo(allocations.get(0).quantity())
        );
    }

    @Test
    void negativeRemainingQuantityMustBeRejected() {

        InventoryLayer invalid =
                layer(201L, 101L, "-1.000", "7.2000", false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> planner.plan(
                        List.of(invalid),
                        101L,
                        new BigDecimal("1.000")
                )
        );

        assertEquals(
                "Il layer FIFO presenta una quantità residua negativa.",
                exception.getMessage()
        );
    }

    @Test
    void resultMustBeImmutable() {

        InventoryLayer source =
                layer(201L, 101L, "5.000", "7.2000", false);

        var allocations = planner.plan(
                List.of(source),
                101L,
                new BigDecimal("2.000")
        );

        assertEquals(1, allocations.size());

        assertThrows(
                UnsupportedOperationException.class,
                () -> allocations.clear()
        );

        assertEquals(
                0,
                new BigDecimal("5.000")
                        .compareTo(source.getRemainingQty())
        );
    }

    @Test
    void missingSourceLocationMustBeRejected() {

        InventoryLayer source =
                layer(201L, 101L, "5.000", "7.2000", false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> planner.plan(
                        List.of(source),
                        null,
                        new BigDecimal("2.000")
                )
        );

        assertEquals(
                "Layer FIFO e ubicazione di origine sono obbligatori.",
                exception.getMessage()
        );
    }
}
