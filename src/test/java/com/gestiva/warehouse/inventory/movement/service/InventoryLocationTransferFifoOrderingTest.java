package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferFifoOrderingTest {

    private static InventoryLayer layer(
            Long id,
            LocalDate date,
            String quantity) {

        InventoryLayer result = new InventoryLayer();

        ReflectionTestUtils.setField(result, "id", id);

        result.setTenantId(2L);
        result.setCompanyId(3L);
        result.setWarehouseId(2L);
        result.setItemId(7L);
        result.setLocationId(101L);

        result.setLayerDate(date);
        result.setOriginalQty(new BigDecimal(quantity));
        result.setRemainingQty(new BigDecimal(quantity));
        result.setUnitCost(new BigDecimal("7.2000"));
        result.setClosed(false);

        return result;
    }

    @Test
    void mustRejectLayersNotOrderedByDateAndId() {

        InventoryLayer newer = layer(
                202L,
                LocalDate.of(2026, 10, 20),
                "5.000"
        );

        InventoryLayer older = layer(
                201L,
                LocalDate.of(2026, 10, 10),
                "5.000"
        );

        InventoryLocationTransferFifoPlanner planner =
                new InventoryLocationTransferFifoPlanner();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> planner.plan(
                        List.of(newer, older),
                        101L,
                        new BigDecimal("3.000")
                )
        );

        assertEquals(
                "I layer FIFO non sono ordinati per data e ID.",
                exception.getMessage()
        );

        assertEquals(
                0,
                new BigDecimal("5.000")
                        .compareTo(newer.getRemainingQty())
        );

        assertEquals(
                0,
                new BigDecimal("5.000")
                        .compareTo(older.getRemainingQty())
        );

        assertFalse(newer.isClosed());
        assertFalse(older.isClosed());
    }
}
