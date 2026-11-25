package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryLocationTransferRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryLocationTransferAllocationRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerConsumptionRepository;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryLocationTransferAtomicPostingWarehouseLockTest {

    private static final class Fixture {

        final WarehouseRepository warehouse =
                mock(WarehouseRepository.class);

        final InventoryLocationTransferRepository transfer =
                mock(InventoryLocationTransferRepository.class);

        final InventoryLocationTransferAllocationRepository allocation =
                mock(InventoryLocationTransferAllocationRepository.class);

        final InventoryLayerRepository layers =
                mock(InventoryLayerRepository.class);

        final InventoryLayerConsumptionRepository consumptions =
                mock(InventoryLayerConsumptionRepository.class);

        final InventoryMovementService movements =
                mock(InventoryMovementService.class);

        final InventoryValuationService valuation =
                mock(InventoryValuationService.class);

        final InventoryLocationTransferAtomicPostingCoordinator coordinator =
                new InventoryLocationTransferAtomicPostingCoordinator(
                        warehouse,
                        transfer,
                        allocation,
                        layers,
                        consumptions,
                        movements,
                        valuation
                );

        void post(Long tenantId) {
            coordinator.post(
                    tenantId,
                    1L,
                    5L,
                    10L,
                    100L,
                    200L,
                    new BigDecimal("7.000"),
                    LocalDate.of(2026, 11, 24),
                    "Collaudo ordine lock",
                    "APC-TRANSFER-001"
            );
        }

        void verifyNoOtherDependencies() {
            verifyNoInteractions(
                    transfer,
                    allocation,
                    layers,
                    consumptions,
                    movements,
                    valuation
            );
        }
    }

    @Test
    void validRequestMustAcquireWarehouseLockBeforeOtherDependencies() {

        var fixture = new Fixture();

        assertThrows(
                BusinessException.class,
                () -> fixture.post(2L)
        );

        try {
            verify(fixture.warehouse, times(1))
                    .findByTenantAndCompanyAndIdForUpdate(
                            2L, 1L, 5L
                    );
        } catch (AssertionError error) {
            fail(
                    "STOP-APC-WAREHOUSE-LOCK-NON-IMPLEMENTATO",
                    error
            );
        }

        fixture.verifyNoOtherDependencies();
    }

    @Test
    void invalidRequestMustNotAcquireWarehouseLock() {

        var fixture = new Fixture();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.post(0L)
        );

        assertEquals(
                "Tenant non valido.",
                exception.getMessage()
        );

        verifyNoInteractions(
                fixture.warehouse,
                fixture.transfer,
                fixture.allocation,
                fixture.layers,
                fixture.consumptions,
                fixture.movements,
                fixture.valuation
        );
    }
}
