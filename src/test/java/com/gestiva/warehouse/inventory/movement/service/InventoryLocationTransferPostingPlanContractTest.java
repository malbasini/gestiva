package com.gestiva.warehouse.inventory.movement.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferPostingPlanContractTest {

    @Test
    void postingPlanMustPreserveFifoAllocationsAndValue()
            throws Exception {

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

        List<InventoryLocationTransferFifoPlanner.Allocation>
                allocations = List.of(first, second);

        Class<?> planClass;

        try {
            planClass = Class.forName(
                    "com.gestiva.warehouse.inventory.movement.service."
                    + "InventoryLocationTransferPostingPlan"
            );
        } catch (ClassNotFoundException exception) {
            fail("STOP-ANQ-POSTING-PLAN-NON-IMPLEMENTATO");
            return;
        }

        Method factory = planClass.getMethod(
                "fromAllocations",
                List.class,
                BigDecimal.class
        );

        Object plan;

        try {
            plan = factory.invoke(
                    null,
                    allocations,
                    new BigDecimal("7.000")
            );
        } catch (InvocationTargetException exception) {
            throw new AssertionError(
                    "La costruzione del piano non deve fallire.",
                    exception.getCause()
            );
        }

        assertNotNull(plan);

        BigDecimal quantity = (BigDecimal) planClass
                .getMethod("quantity")
                .invoke(plan);

        BigDecimal totalCost = (BigDecimal) planClass
                .getMethod("totalCost")
                .invoke(plan);

        Object preservedAllocations = planClass
                .getMethod("allocations")
                .invoke(plan);

        assertEquals(
                new BigDecimal("7.000"),
                quantity
        );

        assertEquals(
                new BigDecimal("52.2000"),
                totalCost
        );

        assertEquals(
                allocations,
                preservedAllocations,
                "Il piano deve preservare tutti i layer FIFO."
        );
    }
}
