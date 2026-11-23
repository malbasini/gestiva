package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferFifoContextRobustnessTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 3L;
    private static final Long WAREHOUSE = 2L;
    private static final Long ITEM = 7L;
    private static final Long SOURCE = 101L;

    private final InventoryLocationTransferFifoPlanner planner =
            new InventoryLocationTransferFifoPlanner();

    private static InventoryLayer layer(
            Long id,
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long itemId,
            Long locationId,
            LocalDate date) {

        InventoryLayer result = new InventoryLayer();

        ReflectionTestUtils.setField(result, "id", id);

        result.setTenantId(tenantId);
        result.setCompanyId(companyId);
        result.setWarehouseId(warehouseId);
        result.setItemId(itemId);
        result.setLocationId(locationId);

        result.setLayerDate(date);
        result.setOriginalQty(new BigDecimal("10.000"));
        result.setRemainingQty(new BigDecimal("10.000"));
        result.setUnitCost(new BigDecimal("7.2000"));
        result.setClosed(false);

        return result;
    }

    private static InventoryLayer validLayer() {
        return layer(
                201L,
                TENANT,
                COMPANY,
                WAREHOUSE,
                ITEM,
                SOURCE,
                LocalDate.of(2026, 10, 1)
        );
    }

    private void assertRejectedWithoutMutation(
            List<InventoryLayer> candidates) {

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> planner.plan(
                        candidates,
                        TENANT,
                        COMPANY,
                        WAREHOUSE,
                        ITEM,
                        SOURCE,
                        new BigDecimal("3.000")
                )
        );

        assertEquals(
                "Il layer FIFO non appartiene al contesto del trasferimento.",
                exception.getMessage()
        );

        for (InventoryLayer candidate : candidates) {
            assertEquals(
                    0,
                    new BigDecimal("10.000").compareTo(
                            candidate.getRemainingQty()
                    )
            );

            assertFalse(candidate.isClosed());
        }
    }

    @Test
    void mustRejectLayerBelongingToAnotherCompany() {

        InventoryLayer foreign = layer(
                201L, TENANT, 99L, WAREHOUSE, ITEM,
                SOURCE, LocalDate.of(2026, 10, 1)
        );

        assertRejectedWithoutMutation(List.of(foreign));
    }

    @Test
    void mustRejectLayerBelongingToAnotherWarehouse() {

        InventoryLayer foreign = layer(
                201L, TENANT, COMPANY, 99L, ITEM,
                SOURCE, LocalDate.of(2026, 10, 1)
        );

        assertRejectedWithoutMutation(List.of(foreign));
    }

    @Test
    void mustRejectLayerBelongingToAnotherItem() {

        InventoryLayer foreign = layer(
                201L, TENANT, COMPANY, WAREHOUSE, 99L,
                SOURCE, LocalDate.of(2026, 10, 1)
        );

        assertRejectedWithoutMutation(List.of(foreign));
    }

    @Test
    void mustValidateEveryCandidateEvenAfterQuantityIsSatisfied() {

        InventoryLayer valid = validLayer();

        // La prima riga è sufficiente a coprire tutte le 3 PZ.
        // Il secondo layer deve comunque essere validato.
        InventoryLayer foreign = layer(
                202L, TENANT, 99L, WAREHOUSE, ITEM,
                102L, LocalDate.of(2026, 10, 2)
        );

        assertRejectedWithoutMutation(
                List.of(valid, foreign)
        );
    }

    @Test
    void mustRejectMissingRequiredContext() {

        InventoryLayer valid = validLayer();

        Long[][] invalidContexts = {
                {null, COMPANY, WAREHOUSE, ITEM},
                {TENANT, null, WAREHOUSE, ITEM},
                {TENANT, COMPANY, null, ITEM},
                {TENANT, COMPANY, WAREHOUSE, null}
        };

        for (Long[] context : invalidContexts) {

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> planner.plan(
                            List.of(valid),
                            context[0],
                            context[1],
                            context[2],
                            context[3],
                            SOURCE,
                            new BigDecimal("3.000")
                    )
            );

            assertEquals(
                    "Tenant, società, magazzino e articolo sono obbligatori.",
                    exception.getMessage()
            );
        }

        assertEquals(
                0,
                new BigDecimal("10.000").compareTo(
                        valid.getRemainingQty()
                )
        );

        assertFalse(valid.isClosed());
    }
}
