package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayerConsumption;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferConsumptionContextIsolationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long WAREHOUSE = 5L;
    private static final Long OUT_MOVEMENT = 900L;

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

        var result = new InventoryLayerConsumption();

        result.setTenantId(TENANT);
        result.setCompanyId(COMPANY);
        result.setWarehouseId(WAREHOUSE);
        result.setOutMovementId(OUT_MOVEMENT);

        result.setLayerId(layerId);
        result.setConsumedQty(new BigDecimal(quantity));
        result.setUnitCost(new BigDecimal(unitCost));
        result.setTotalCost(new BigDecimal(totalCost));

        return result;
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

    private static void verify(
            List<InventoryLayerConsumption> consumptions) {

        InventoryLocationTransferConsumptionVerifier.verify(
                plan(),
                consumptions,
                TENANT,
                COMPANY,
                WAREHOUSE,
                OUT_MOVEMENT
        );
    }

    private static void reject(
            List<InventoryLayerConsumption> consumptions) {

        assertThrows(
                BusinessException.class,
                () -> verify(consumptions)
        );
    }

    @Test
    void mustAcceptMatchingContextAndFifoValues() {
        assertDoesNotThrow(
                () -> verify(List.of(first(), second()))
        );
    }

    @Test
    void mustRejectDifferentTenant() {
        var invalid = first();
        invalid.setTenantId(3L);

        reject(List.of(invalid, second()));
    }

    @Test
    void mustRejectDifferentCompany() {
        var invalid = first();
        invalid.setCompanyId(3L);

        reject(List.of(invalid, second()));
    }

    @Test
    void mustRejectDifferentWarehouse() {
        var invalid = first();
        invalid.setWarehouseId(6L);

        reject(List.of(invalid, second()));
    }

    @Test
    void mustRejectDifferentOutMovement() {
        var invalid = first();
        invalid.setOutMovementId(901L);

        reject(List.of(invalid, second()));
    }

    @Test
    void mustRejectMissingConsumptionTenant() {
        var invalid = first();
        invalid.setTenantId(null);

        reject(List.of(invalid, second()));
    }

    @Test
    void mustRejectMissingConsumptionCompany() {
        var invalid = first();
        invalid.setCompanyId(null);

        reject(List.of(invalid, second()));
    }

    @Test
    void mustRejectMissingConsumptionWarehouse() {
        var invalid = first();
        invalid.setWarehouseId(null);

        reject(List.of(invalid, second()));
    }

    @Test
    void mustRejectMissingConsumptionOutMovement() {
        var invalid = first();
        invalid.setOutMovementId(null);

        reject(List.of(invalid, second()));
    }

    @Test
    void mustRejectInvalidRequestedOutMovement() {
        assertThrows(
                BusinessException.class,
                () -> InventoryLocationTransferConsumptionVerifier.verify(
                        plan(),
                        List.of(first(), second()),
                        TENANT,
                        COMPANY,
                        WAREHOUSE,
                        0L
                )
        );
    }
}
