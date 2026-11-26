package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransfer;
import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransferAllocation;
import com.gestiva.warehouse.inventory.movement.entity.InventoryMovement;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayerConsumption;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryLocationTransferPostedCompletenessVerifierRobustnessTest {

    private static final LocalDate DATE =
            LocalDate.of(2026, 11, 24);

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private static final class Fixture {

        final InventoryLocationTransfer transfer =
                spy(new InventoryLocationTransfer());

        final InventoryMovement out =
                spy(new InventoryMovement());

        final InventoryMovement in =
                spy(new InventoryMovement());

        final InventoryLocationTransferAllocation allocation =
                new InventoryLocationTransferAllocation();

        final InventoryLayerConsumption consumption =
                new InventoryLayerConsumption();

        final InventoryLayer layer =
                spy(new InventoryLayer());

        final List<InventoryLocationTransferAllocation> allocations =
                new ArrayList<>();

        final List<InventoryLayerConsumption> consumptions =
                new ArrayList<>();

        final List<InventoryLayer> layers =
                new ArrayList<>();

        Fixture() {
            doReturn(700L).when(transfer).getId();
            doReturn(701L).when(out).getId();
            doReturn(702L).when(in).getId();
            doReturn(900L).when(layer).getId();

            transfer.setTenantId(2L);
            transfer.setCompanyId(1L);
            transfer.setWarehouseId(5L);
            transfer.setItemId(10L);
            transfer.setSourceLocationId(100L);
            transfer.setTargetLocationId(200L);
            transfer.setMovementDate(DATE);
            transfer.setQuantity(bd("7.000"));
            transfer.setStatus("POSTED");
            transfer.setOutMovementId(701L);
            transfer.setInMovementId(702L);

            movement(out, 701L, 100L, "OUT");
            movement(in, 702L, 200L, "IN");

            allocation.setTenantId(2L);
            allocation.setCompanyId(1L);
            allocation.setWarehouseId(5L);
            allocation.setTransferId(700L);
            allocation.setSourceLayerId(800L);
            allocation.setTargetLayerId(900L);
            allocation.setAllocatedQty(bd("7.000"));
            allocation.setUnitCost(bd("2.5000"));
            allocation.setLayerDate(DATE.minusDays(20));

            consumption.setTenantId(2L);
            consumption.setCompanyId(1L);
            consumption.setWarehouseId(5L);
            consumption.setOutMovementId(701L);
            consumption.setLayerId(800L);
            consumption.setConsumedQty(bd("7.000"));
            consumption.setUnitCost(bd("2.5000"));
            consumption.setTotalCost(bd("17.5000"));

            layer.setTenantId(2L);
            layer.setCompanyId(1L);
            layer.setWarehouseId(5L);
            layer.setItemId(10L);
            layer.setLocationId(200L);
            layer.setSourceMovementId(702L);
            layer.setLayerDate(DATE.minusDays(20));
            layer.setOriginalQty(bd("7.000"));
            layer.setRemainingQty(bd("7.000"));
            layer.setUnitCost(bd("2.5000"));
            layer.setClosed(false);

            allocations.add(allocation);
            consumptions.add(consumption);
            layers.add(layer);
        }

        private void movement(
                InventoryMovement movement,
                Long id,
                Long location,
                String type) {

            movement.setTenantId(2L);
            movement.setCompanyId(1L);
            movement.setWarehouseId(5L);
            movement.setLocationId(location);
            movement.setItemId(10L);
            movement.setMovementDate(DATE);
            movement.setMovementType(type);
            movement.setCausalCode("LOCATION_TRANSFER_" + type);
            movement.setQuantity(bd("7.000"));
            movement.setTotalCost(bd("17.5000"));
            movement.setReferenceType("LOCATION_TRANSFER");
            movement.setReferenceId(700L);
            movement.setReversed(false);
            movement.setReversalOfMovementId(null);
        }

        void verify() {
            InventoryLocationTransferPostedCompletenessVerifier.verify(
                    transfer, out, in,
                    allocations, consumptions, layers
            );
        }
    }

    private static void passes(Consumer<Fixture> change) {
        var fixture = new Fixture();
        change.accept(fixture);
        assertDoesNotThrow(fixture::verify);
    }

    private static void fails(Consumer<Fixture> change) {
        var fixture = new Fixture();
        change.accept(fixture);
        assertThrows(BusinessException.class, fixture::verify);
    }

    @Test
    void acceptsCompleteTransfer() {
        passes(f -> {});
    }

    @Test
    void acceptsHistoricallyConsumedDestinationLayer() {
        passes(f -> {
            f.layer.setRemainingQty(bd("0.000"));
            f.layer.setClosed(true);
        });
    }

    @Test
    void rejectsPendingTransfer() {
        fails(f -> f.transfer.setStatus("PENDING"));
    }

    @Test
    void rejectsMissingOutMovement() {
        var f = new Fixture();
        assertThrows(
                BusinessException.class,
                () -> InventoryLocationTransferPostedCompletenessVerifier
                        .verify(
                                f.transfer, null, f.in,
                                f.allocations, f.consumptions, f.layers
                        )
        );
    }

    @Test
    void rejectsWrongMovementReference() {
        fails(f -> f.out.setReferenceId(999L));
    }

    @Test
    void rejectsMovementFromDifferentTenant() {
        fails(f -> f.in.setTenantId(99L));
    }

    @Test
    void rejectsWrongDestinationLocation() {
        fails(f -> f.in.setLocationId(999L));
    }

    @Test
    void rejectsReversedMovement() {
        fails(f -> f.out.setReversed(true));
    }

    @Test
    void rejectsMovementQuantityMismatch() {
        fails(f -> f.out.setQuantity(bd("6.000")));
    }

    @Test
    void rejectsMissingAllocations() {
        fails(f -> f.allocations.clear());
    }

    @Test
    void rejectsDuplicateSourceAllocation() {
        fails(f -> f.allocations.add(f.allocation));
    }

    @Test
    void rejectsMissingConsumptions() {
        fails(f -> f.consumptions.clear());
    }

    @Test
    void rejectsWrongConsumedLayer() {
        fails(f -> f.consumption.setLayerId(999L));
    }

    @Test
    void rejectsConsumptionQuantityMismatch() {
        fails(f -> f.consumption.setConsumedQty(bd("6.000")));
    }

    @Test
    void rejectsConsumptionUnitCostMismatch() {
        fails(f -> f.consumption.setUnitCost(bd("3.0000")));
    }

    @Test
    void rejectsMissingTargetLayers() {
        fails(f -> f.layers.clear());
    }

    @Test
    void rejectsWrongTargetMovement() {
        fails(f -> f.layer.setSourceMovementId(999L));
    }

    @Test
    void rejectsChangedTargetOriginalQuantity() {
        fails(f -> f.layer.setOriginalQty(bd("6.000")));
    }

    @Test
    void rejectsChangedHistoricalFifoDate() {
        fails(f -> f.layer.setLayerDate(DATE));
    }

    @Test
    void rejectsMovementTotalCostMismatch() {
        fails(f -> f.in.setTotalCost(bd("16.0000")));
    }

    @Test
    void rejectsRemainingQuantityGreaterThanOriginal() {
        fails(f -> f.layer.setRemainingQty(bd("8.000")));
    }

    private static void unlocatedSource(Fixture f) {
        f.transfer.setSourceLocationId(null);
        f.out.setLocationId(null);
    }

    @Test
    void acceptsCompleteUnlocatedTransfer() {
        passes(f -> unlocatedSource(f));
    }

    @Test
    void rejectsLocatedOutForUnlocatedTransfer() {
        fails(f -> {
            unlocatedSource(f);
            f.out.setLocationId(100L);
        });
    }

    @Test
    void rejectsUnlocatedOutForLocatedTransfer() {
        fails(f -> f.out.setLocationId(null));
    }

    @Test
    void rejectsMatchingZeroSourceIdentifiers() {
        fails(f -> {
            f.transfer.setSourceLocationId(0L);
            f.out.setLocationId(0L);
        });
    }

    @Test
    void rejectsMatchingNegativeSourceIdentifiers() {
        fails(f -> {
            f.transfer.setSourceLocationId(-1L);
            f.out.setLocationId(-1L);
        });
    }

    @Test
    void rejectsUnlocatedTransferWithNullTarget() {
        fails(f -> {
            unlocatedSource(f);
            f.transfer.setTargetLocationId(null);
            f.in.setLocationId(null);
            f.layer.setLocationId(null);
        });
    }

    @Test
    void rejectsUnlocatedTransferWithZeroTarget() {
        fails(f -> {
            unlocatedSource(f);
            f.transfer.setTargetLocationId(0L);
            f.in.setLocationId(0L);
            f.layer.setLocationId(0L);
        });
    }

    @Test
    void rejectsUnlocatedTransferWithCostMismatch() {
        fails(f -> {
            unlocatedSource(f);
            f.in.setTotalCost(bd("16.0000"));
        });
    }

    @Test
    void rejectsUnlocatedTransferWithChangedFifoDate() {
        fails(f -> {
            unlocatedSource(f);
            f.layer.setLayerDate(DATE);
        });
    }

    @Test
    void rejectsUnlocatedTransferWithConsumptionMismatch() {
        fails(f -> {
            unlocatedSource(f);
            f.consumption.setConsumedQty(bd("6.000"));
        });
    }

    @Test
    void rejectsUnlocatedTransferWithForeignCompany() {
        fails(f -> {
            unlocatedSource(f);
            f.out.setCompanyId(99L);
        });
    }
}
