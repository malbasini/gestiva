package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayerConsumption;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferConsumptionVerifierTest {

    @Test
    void verifierMustMatchPlannedFifoConsumptions()
            throws Exception {

        Class<?> verifier;

        try {
            verifier = Class.forName(
                    "com.gestiva.warehouse.inventory.movement.service."
                    + "InventoryLocationTransferConsumptionVerifier"
            );
        } catch (ClassNotFoundException exception) {
            fail(
                    "STOP-AOL-CONSUMPTION-VERIFIER-NON-IMPLEMENTATO"
            );
            return;
        }

        Method verify = verifier.getDeclaredMethod(
                "verify",
                InventoryLocationTransferPostingPlan.class,
                List.class
        );

        assertTrue(
                Modifier.isStatic(verify.getModifiers()),
                "STOP-AOL-VERIFIER-NON-STATICO"
        );

        assertEquals(
                void.class,
                verify.getReturnType()
        );

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

        var plan = InventoryLocationTransferPostingPlan
                .fromAllocations(
                        List.of(first, second),
                        new BigDecimal("7.000")
                );

        var firstConsumption = consumption(
                11L,
                "5.000",
                "7.2000",
                "36.0000"
        );

        var secondConsumption = consumption(
                12L,
                "2.000",
                "8.1000",
                "16.2000"
        );

        assertEquals(
                new BigDecimal("52.2000"),
                plan.totalCost()
        );

        try {
            verify.invoke(
                    null,
                    plan,
                    List.of(
                            firstConsumption,
                            secondConsumption
                    )
            );
        } catch (InvocationTargetException exception) {
            throw new AssertionError(
                    "STOP-AOL-RICONCILIAZIONE-VALIDA-RIFIUTATA",
                    exception.getCause()
            );
        }
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
}
