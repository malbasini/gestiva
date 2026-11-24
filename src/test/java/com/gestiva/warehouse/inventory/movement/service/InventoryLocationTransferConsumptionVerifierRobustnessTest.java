package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayerConsumption;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferConsumptionVerifierRobustnessTest {

    private static InventoryLocationTransferPostingPlan plan() {
        var first =
                new InventoryLocationTransferFifoPlanner.Allocation(
                        11L,
                        new BigDecimal("5.000"),
                        new BigDecimal("7.2000"),
                        LocalDate.of(2026, 10, 1)
                );

        var second =
                new InventoryLocationTransferFifoPlanner.Allocation(
                        12L,
                        new BigDecimal("2.000"),
                        new BigDecimal("8.1000"),
                        LocalDate.of(2026, 10, 5)
                );

        return InventoryLocationTransferPostingPlan
                .fromAllocations(
                        List.of(first, second),
                        new BigDecimal("7.000")
                );
    }

    private static InventoryLayerConsumption consumption(
            Long layerId,
            String quantity,
            String unitCost,
            String totalCost) {

        var consumption = new InventoryLayerConsumption();

        consumption.setLayerId(layerId);
        consumption.setConsumedQty(new BigDecimal(quantity));
        consumption.setUnitCost(new BigDecimal(unitCost));
        consumption.setTotalCost(new BigDecimal(totalCost));

        return consumption;
    }

    private static InventoryLayerConsumption first() {
        return consumption(
                11L, "5.000", "7.2000", "36.0000"
        );
    }

    private static InventoryLayerConsumption second() {
        return consumption(
                12L, "2.000", "8.1000", "16.2000"
        );
    }

    private static void reject(
            InventoryLocationTransferPostingPlan postingPlan,
            List<InventoryLayerConsumption> consumptions) {

        assertThrows(
                BusinessException.class,
                () -> InventoryLocationTransferConsumptionVerifier
                        .verify(postingPlan, consumptions)
        );
    }

    @Test
    void mustRejectNullPlan() {
        reject(null, List.of(first(), second()));
    }

    @Test
    void mustRejectNullConsumptions() {
        reject(plan(), null);
    }

    @Test
    void mustRejectEmptyConsumptions() {
        reject(plan(), List.of());
    }

    @Test
    void mustRejectMissingPlannedConsumption() {
        reject(plan(), List.of(first()));
    }

    @Test
    void mustRejectUnexpectedLayer() {
        reject(
                plan(),
                List.of(
                        first(),
                        consumption(
                                99L, "2.000", "8.1000", "16.2000"
                        )
                )
        );
    }

    @Test
    void mustRejectDuplicateSourceLayer() {
        reject(
                plan(),
                List.of(first(), first())
        );
    }

    @Test
    void mustRejectDifferentQuantity() {
        reject(
                plan(),
                List.of(
                        consumption(
                                11L, "4.000", "7.2000", "28.8000"
                        ),
                        second()
                )
        );
    }

    @Test
    void mustRejectDifferentUnitCost() {
        reject(
                plan(),
                List.of(
                        consumption(
                                11L, "5.000", "7.3000", "36.5000"
                        ),
                        second()
                )
        );
    }

    @Test
    void mustRejectDifferentTotalCost() {
        reject(
                plan(),
                List.of(
                        consumption(
                                11L, "5.000", "7.2000", "35.0000"
                        ),
                        second()
                )
        );
    }

    @Test
    void mustAcceptCorrectConsumptionsInDifferentListOrder() {
        assertDoesNotThrow(
                () -> InventoryLocationTransferConsumptionVerifier.verify(
                        plan(),
                        List.of(second(), first())
                )
        );
    }
}
