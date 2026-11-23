package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferFifoContextIsolationTest {

    @Test
    void mustRejectLayerBelongingToAnotherTenant() throws Exception {

        InventoryLayer foreignLayer = new InventoryLayer();

        ReflectionTestUtils.setField(foreignLayer, "id", 201L);

        foreignLayer.setTenantId(999L);
        foreignLayer.setCompanyId(3L);
        foreignLayer.setWarehouseId(2L);
        foreignLayer.setItemId(7L);
        foreignLayer.setLocationId(101L);

        foreignLayer.setLayerDate(
                LocalDate.of(2026, 10, 1)
        );

        foreignLayer.setOriginalQty(
                new BigDecimal("5.000")
        );

        foreignLayer.setRemainingQty(
                new BigDecimal("5.000")
        );

        foreignLayer.setUnitCost(
                new BigDecimal("7.2000")
        );

        foreignLayer.setClosed(false);

        InventoryLocationTransferFifoPlanner planner =
                new InventoryLocationTransferFifoPlanner();

        Method scopedPlan;

        try {
            scopedPlan = InventoryLocationTransferFifoPlanner.class
                    .getMethod(
                            "plan",
                            List.class,
                            Long.class,
                            Long.class,
                            Long.class,
                            Long.class,
                            Long.class,
                            BigDecimal.class
                    );

        } catch (NoSuchMethodException exception) {
            fail("STOP-ANE-CONTRATTO-CONTEXT-NON-IMPLEMENTATO");
            return;
        }

        InvocationTargetException invocation = assertThrows(
                InvocationTargetException.class,
                () -> scopedPlan.invoke(
                        planner,
                        List.of(foreignLayer),
                        2L,
                        3L,
                        2L,
                        7L,
                        101L,
                        new BigDecimal("3.000")
                )
        );

        BusinessException exception = assertInstanceOf(
                BusinessException.class,
                invocation.getCause()
        );

        assertEquals(
                "Il layer FIFO non appartiene al contesto del trasferimento.",
                exception.getMessage()
        );

        assertEquals(
                0,
                new BigDecimal("5.000").compareTo(
                        foreignLayer.getRemainingQty()
                )
        );

        assertFalse(foreignLayer.isClosed());
    }
}
