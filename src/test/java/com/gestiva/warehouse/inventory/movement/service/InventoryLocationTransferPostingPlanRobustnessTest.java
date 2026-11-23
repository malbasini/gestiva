package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferPostingPlanRobustnessTest {

    private static InventoryLocationTransferFifoPlanner.Allocation
    allocation(long id, String quantity, String cost) {
        return new InventoryLocationTransferFifoPlanner.Allocation(
                id,
                new BigDecimal(quantity),
                new BigDecimal(cost),
                LocalDate.of(2026, 10, 1)
        );
    }

    @Test
    void mustRejectQuantityDifferentFromAllocations() {
        var allocations = List.of(
                allocation(11L, "4.000", "7.2000"),
                allocation(12L, "2.000", "8.1000")
        );

        assertThrows(
                BusinessException.class,
                () -> InventoryLocationTransferPostingPlan
                        .fromAllocations(
                                allocations,
                                new BigDecimal("7.000")
                        )
        );
    }

    @Test
    void mustRejectInvalidUnitCost() {
        var allocations = List.of(
                allocation(11L, "2.000", "-1.0000")
        );

        assertThrows(
                BusinessException.class,
                () -> InventoryLocationTransferPostingPlan
                        .fromAllocations(
                                allocations,
                                new BigDecimal("2.000")
                        )
        );
    }

    @Test
    void mustRejectExcessQuantityPrecision() {
        var allocations = List.of(
                allocation(11L, "1.0001", "7.2000")
        );

        assertThrows(
                BusinessException.class,
                () -> InventoryLocationTransferPostingPlan
                        .fromAllocations(
                                allocations,
                                new BigDecimal("1.0001")
                        )
        );
    }

    @Test
    void mustRejectIncorrectTotalCostInDirectConstructor() {
        var allocations = List.of(
                allocation(11L, "5.000", "7.2000")
        );

        assertThrows(
                BusinessException.class,
                () -> new InventoryLocationTransferPostingPlan(
                        allocations,
                        new BigDecimal("5.000"),
                        new BigDecimal("35.0000")
                )
        );
    }

    @Test
    void mustPreserveImmutableSnapshotOfAllocations() {
        var original = new ArrayList<
                InventoryLocationTransferFifoPlanner.Allocation>();

        original.add(
                allocation(11L, "5.000", "7.2000")
        );

        var plan = InventoryLocationTransferPostingPlan
                .fromAllocations(
                        original,
                        new BigDecimal("5.000")
                );

        original.add(
                allocation(12L, "2.000", "8.1000")
        );

        assertEquals(1, plan.allocations().size());

        assertThrows(
                UnsupportedOperationException.class,
                () -> plan.allocations().clear()
        );

        assertEquals(
                new BigDecimal("36.0000"),
                plan.totalCost()
        );
    }

    @Test
    void mustPreserveOriginalLayerDatesAndIndividualCosts() {
        var first = new InventoryLocationTransferFifoPlanner.Allocation(
                11L,
                new BigDecimal("5.000"),
                new BigDecimal("7.2000"),
                LocalDate.of(2026, 9, 15)
        );

        var second = new InventoryLocationTransferFifoPlanner.Allocation(
                12L,
                new BigDecimal("2.000"),
                new BigDecimal("8.1000"),
                LocalDate.of(2026, 10, 5)
        );

        var plan = InventoryLocationTransferPostingPlan
                .fromAllocations(
                        List.of(first, second),
                        new BigDecimal("7.000")
                );

        assertEquals(
                List.of(first, second),
                plan.allocations()
        );

        assertEquals(
                LocalDate.of(2026, 9, 15),
                plan.allocations().get(0).layerDate()
        );

        assertEquals(
                LocalDate.of(2026, 10, 5),
                plan.allocations().get(1).layerDate()
        );

        assertEquals(
                new BigDecimal("7.2000"),
                plan.allocations().get(0).unitCost()
        );

        assertEquals(
                new BigDecimal("8.1000"),
                plan.allocations().get(1).unitCost()
        );

        assertEquals(
                new BigDecimal("52.2000"),
                plan.totalCost()
        );
    }
}
