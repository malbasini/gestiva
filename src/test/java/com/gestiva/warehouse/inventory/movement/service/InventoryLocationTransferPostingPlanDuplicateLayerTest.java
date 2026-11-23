package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferPostingPlanDuplicateLayerTest {

    @Test
    void postingPlanMustRejectRepeatedSourceLayerId() {

        var first =
                new InventoryLocationTransferFifoPlanner.Allocation(
                        11L,
                        new BigDecimal("4.000"),
                        new BigDecimal("7.2000"),
                        LocalDate.of(2026, 10, 1)
                );

        var second =
                new InventoryLocationTransferFifoPlanner.Allocation(
                        11L,
                        new BigDecimal("3.000"),
                        new BigDecimal("7.2000"),
                        LocalDate.of(2026, 10, 1)
                );

        List<InventoryLocationTransferFifoPlanner.Allocation>
                allocations = List.of(first, second);

        try {
            InventoryLocationTransferPostingPlan.fromAllocations(
                    allocations,
                    new BigDecimal("7.000")
            );
        } catch (BusinessException expected) {
            return;
        }

        fail("STOP-ANS-DUPLICATE-LAYER-ACCEPTED");
    }
}
