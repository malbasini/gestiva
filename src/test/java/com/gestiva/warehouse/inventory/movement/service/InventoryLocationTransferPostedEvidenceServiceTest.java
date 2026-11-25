package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransfer;
import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransferAllocation;
import com.gestiva.warehouse.inventory.movement.entity.InventoryMovement;
import com.gestiva.warehouse.inventory.movement.repository.InventoryLocationTransferAllocationRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayerConsumption;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerConsumptionRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryLocationTransferPostedEvidenceServiceTest {

    private static final LocalDate DATE =
            LocalDate.of(2026, 11, 24);

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private static final class Fixture {

        final InventoryMovementRepository movements =
                mock(InventoryMovementRepository.class);

        final InventoryLocationTransferAllocationRepository allocations =
                mock(InventoryLocationTransferAllocationRepository.class);

        final InventoryLayerConsumptionRepository consumptions =
                mock(InventoryLayerConsumptionRepository.class);

        final InventoryLayerRepository layers =
                mock(InventoryLayerRepository.class);

        final InventoryLocationTransferPostedEvidenceService service =
                new InventoryLocationTransferPostedEvidenceService(
                        movements,
                        allocations,
                        consumptions,
                        layers
                );

        final InventoryLocationTransfer transfer =
                mock(InventoryLocationTransfer.class);

        final InventoryMovement out =
                mock(InventoryMovement.class);

        final InventoryMovement in =
                mock(InventoryMovement.class);

        final InventoryLocationTransferAllocation allocation =
                mock(InventoryLocationTransferAllocation.class);

        final InventoryLayerConsumption consumption =
                mock(InventoryLayerConsumption.class);

        final InventoryLayer layer =
                mock(InventoryLayer.class);

        Fixture() {
            when(transfer.getId()).thenReturn(700L);
            when(transfer.getTenantId()).thenReturn(2L);
            when(transfer.getCompanyId()).thenReturn(1L);
            when(transfer.getWarehouseId()).thenReturn(5L);
            when(transfer.getItemId()).thenReturn(10L);
            when(transfer.getSourceLocationId()).thenReturn(100L);
            when(transfer.getTargetLocationId()).thenReturn(200L);
            when(transfer.getMovementDate()).thenReturn(DATE);
            when(transfer.getQuantity()).thenReturn(bd("7.000"));
            when(transfer.getStatus()).thenReturn("POSTED");
            when(transfer.getOutMovementId()).thenReturn(701L);
            when(transfer.getInMovementId()).thenReturn(702L);

            movement(out, 701L, 100L, "OUT");
            movement(in, 702L, 200L, "IN");

            when(allocation.getTenantId()).thenReturn(2L);
            when(allocation.getCompanyId()).thenReturn(1L);
            when(allocation.getWarehouseId()).thenReturn(5L);
            when(allocation.getTransferId()).thenReturn(700L);
            when(allocation.getSourceLayerId()).thenReturn(800L);
            when(allocation.getTargetLayerId()).thenReturn(900L);
            when(allocation.getAllocatedQty()).thenReturn(bd("7.000"));
            when(allocation.getUnitCost()).thenReturn(bd("2.5000"));
            when(allocation.getLayerDate()).thenReturn(DATE.minusDays(20));

            when(consumption.getTenantId()).thenReturn(2L);
            when(consumption.getCompanyId()).thenReturn(1L);
            when(consumption.getWarehouseId()).thenReturn(5L);
            when(consumption.getOutMovementId()).thenReturn(701L);
            when(consumption.getLayerId()).thenReturn(800L);
            when(consumption.getConsumedQty()).thenReturn(bd("7.000"));
            when(consumption.getUnitCost()).thenReturn(bd("2.5000"));
            when(consumption.getTotalCost()).thenReturn(bd("17.5000"));

            when(layer.getId()).thenReturn(900L);
            when(layer.getTenantId()).thenReturn(2L);
            when(layer.getCompanyId()).thenReturn(1L);
            when(layer.getWarehouseId()).thenReturn(5L);
            when(layer.getItemId()).thenReturn(10L);
            when(layer.getLocationId()).thenReturn(200L);
            when(layer.getSourceMovementId()).thenReturn(702L);
            when(layer.getLayerDate()).thenReturn(DATE.minusDays(20));
            when(layer.getOriginalQty()).thenReturn(bd("7.000"));
            when(layer.getRemainingQty()).thenReturn(bd("3.000"));
            when(layer.getUnitCost()).thenReturn(bd("2.5000"));
        }

        private void movement(
                InventoryMovement m,
                Long id,
                Long location,
                String type) {

            when(m.getId()).thenReturn(id);
            when(m.getTenantId()).thenReturn(2L);
            when(m.getCompanyId()).thenReturn(1L);
            when(m.getWarehouseId()).thenReturn(5L);
            when(m.getItemId()).thenReturn(10L);
            when(m.getLocationId()).thenReturn(location);
            when(m.getMovementDate()).thenReturn(DATE);
            when(m.getMovementType()).thenReturn(type);
            when(m.getCausalCode()).thenReturn("LOCATION_TRANSFER_" + type);
            when(m.getReferenceType()).thenReturn("LOCATION_TRANSFER");
            when(m.getReferenceId()).thenReturn(700L);
            when(m.getQuantity()).thenReturn(bd("7.000"));
            when(m.getTotalCost()).thenReturn(bd("17.5000"));

            // Un movimento ordinario non è stornato
            // e non rappresenta un movimento compensativo.
            when(m.isReversed()).thenReturn(false);
            when(m.getReversalOfMovementId()).thenReturn(null);
        }

        void completeEvidence() {
            when(movements.findByTenantIdAndCompanyIdAndId(
                    2L, 1L, 701L
            )).thenReturn(Optional.of(out));

            when(movements.findByTenantIdAndCompanyIdAndId(
                    2L, 1L, 702L
            )).thenReturn(Optional.of(in));

            when(allocations.findByTenantIdAndCompanyIdAndTransferIdOrderByIdAsc(
                    2L, 1L, 700L
            )).thenReturn(List.of(allocation));

            when(consumptions.findByTenantIdAndCompanyIdAndWarehouseIdAndOutMovementIdOrderByIdAsc(
                    2L, 1L, 5L, 701L
            )).thenReturn(List.of(consumption));

            when(layers.findByTenantIdAndCompanyIdAndSourceMovementIdOrderByIdAsc(
                    2L, 1L, 702L
            )).thenReturn(List.of(layer));
        }

        void verifyAllQueries() {
            verify(movements).findByTenantIdAndCompanyIdAndId(
                    2L, 1L, 701L
            );
            verify(movements).findByTenantIdAndCompanyIdAndId(
                    2L, 1L, 702L
            );
            verify(allocations)
                    .findByTenantIdAndCompanyIdAndTransferIdOrderByIdAsc(
                            2L, 1L, 700L
                    );
            verify(consumptions)
                    .findByTenantIdAndCompanyIdAndWarehouseIdAndOutMovementIdOrderByIdAsc(
                            2L, 1L, 5L, 701L
                    );
            verify(layers)
                    .findByTenantIdAndCompanyIdAndSourceMovementIdOrderByIdAsc(
                            2L, 1L, 702L
                    );
        }

        void verifyNothingWritten() {
            verifyNoMoreInteractions(
                    movements,
                    allocations,
                    consumptions,
                    layers
            );
        }
    }

    @Test
    void completeEvidencePassesUsingScopedQueries() {
        var f = new Fixture();
        f.completeEvidence();

        assertDoesNotThrow(
                () -> f.service.verifyPostedCompleteness(f.transfer)
        );

        f.verifyAllQueries();
        f.verifyNothingWritten();
    }

    @Test
    void pendingTransferStopsBeforeQueries() {
        var f = new Fixture();
        when(f.transfer.getStatus()).thenReturn("PENDING");

        assertThrows(
                BusinessException.class,
                () -> f.service.verifyPostedCompleteness(f.transfer)
        );

        verifyNoInteractions(
                f.movements,
                f.allocations,
                f.consumptions,
                f.layers
        );
    }

    @Test
    void missingOutMovementStopsBeforeFurtherReads() {
        var f = new Fixture();

        assertThrows(
                BusinessException.class,
                () -> f.service.verifyPostedCompleteness(f.transfer)
        );

        verify(f.movements).findByTenantIdAndCompanyIdAndId(
                2L, 1L, 701L
        );

        verifyNoMoreInteractions(f.movements);
        verifyNoInteractions(
                f.allocations,
                f.consumptions,
                f.layers
        );
    }

    @Test
    void missingInMovementStopsBeforeFifoReads() {
        var f = new Fixture();

        when(f.movements.findByTenantIdAndCompanyIdAndId(
                2L, 1L, 701L
        )).thenReturn(Optional.of(f.out));

        assertThrows(
                BusinessException.class,
                () -> f.service.verifyPostedCompleteness(f.transfer)
        );

        verify(f.movements).findByTenantIdAndCompanyIdAndId(
                2L, 1L, 701L
        );
        verify(f.movements).findByTenantIdAndCompanyIdAndId(
                2L, 1L, 702L
        );

        verifyNoMoreInteractions(f.movements);
        verifyNoInteractions(
                f.allocations,
                f.consumptions,
                f.layers
        );
    }

    @Test
    void missingAllocationsAreRejected() {
        var f = new Fixture();
        f.completeEvidence();

        when(f.allocations
                .findByTenantIdAndCompanyIdAndTransferIdOrderByIdAsc(
                        2L, 1L, 700L
                )).thenReturn(List.of());

        assertThrows(
                BusinessException.class,
                () -> f.service.verifyPostedCompleteness(f.transfer)
        );

        f.verifyAllQueries();
        f.verifyNothingWritten();
    }

    @Test
    void inconsistentMovementContextIsRejected() {
        var f = new Fixture();
        f.completeEvidence();

        when(f.in.getTenantId()).thenReturn(99L);

        assertThrows(
                BusinessException.class,
                () -> f.service.verifyPostedCompleteness(f.transfer)
        );

        f.verifyAllQueries();
        f.verifyNothingWritten();
    }

    @Test
    void inconsistentDestinationLayerIsRejected() {
        var f = new Fixture();
        f.completeEvidence();

        when(f.layer.getLayerDate()).thenReturn(DATE);

        assertThrows(
                BusinessException.class,
                () -> f.service.verifyPostedCompleteness(f.transfer)
        );

        f.verifyAllQueries();
        f.verifyNothingWritten();
    }
}
