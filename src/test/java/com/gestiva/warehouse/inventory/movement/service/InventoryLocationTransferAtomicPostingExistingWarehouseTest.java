package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryLocationTransferRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryLocationTransferAllocationRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerConsumptionRepository;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;

import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryLocationTransferAtomicPostingExistingWarehouseTest {

    private static final class Fixture {

        final WarehouseRepository warehouse =
                mock(WarehouseRepository.class);

        final InventoryLocationTransferRepository transfers =
                mock(InventoryLocationTransferRepository.class);

        final InventoryLocationTransferAllocationRepository allocations =
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
                        transfers,
                        allocations,
                        layers,
                        consumptions,
                        movements,
                        valuation
                );

        void provideExistingWarehouse() throws Exception {

            // Ricava il tipo effettivo dell'entità dalla firma
            // del repository senza dipendere dal suo nome.
            var method = WarehouseRepository.class.getMethod(
                    "findByTenantAndCompanyAndIdForUpdate",
                    Long.class,
                    Long.class,
                    Long.class
            );

            var returnType = assertInstanceOf(
                    ParameterizedType.class,
                    method.getGenericReturnType()
            );

            Class<?> entityType = assertInstanceOf(
                    Class.class,
                    returnType.getActualTypeArguments()[0]
            );

            Object existingWarehouse = mock(entityType);

            doReturn(Optional.of(existingWarehouse))
                    .when(warehouse)
                    .findByTenantAndCompanyAndIdForUpdate(
                            2L, 1L, 5L
                    );
        }

        void post(BigDecimal quantity) {
            coordinator.post(
                    2L,
                    1L,
                    5L,
                    10L,
                    100L,
                    200L,
                    quantity,
                    LocalDate.of(2026, 11, 24),
                    "Collaudo magazzino esistente",
                    "APE-TRANSFER-001"
            );
        }

        void assertNoOtherDependencies() {
            verifyNoInteractions(
                    allocations,
                    layers,
                    consumptions,
                    movements,
                    valuation
            );
        }
    }

    @Test
    void existingWarehouseMustStillLeavePostingDisabled()
            throws Exception {

        var fixture = new Fixture();
        fixture.provideExistingWarehouse();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.post(new BigDecimal("7.000"))
        );

        assertEquals(
                "Coordinatore atomico dei trasferimenti FIFO "
                        + "non ancora abilitato.",
                exception.getMessage()
        );

        verify(fixture.warehouse, times(1))
                .findByTenantAndCompanyAndIdForUpdate(
                        2L, 1L, 5L
                );

        verify(fixture.transfers, times(1))
                .findByTenantIdAndCompanyIdAndOperationKey(
                        2L, 1L, "APE-TRANSFER-001"
                );

        verifyNoMoreInteractions(fixture.warehouse);
        verifyNoMoreInteractions(fixture.transfers);
        fixture.assertNoOtherDependencies();
    }

    @Test
    void invalidQuantityMustSkipExistingWarehouseLookup()
            throws Exception {

        var fixture = new Fixture();
        fixture.provideExistingWarehouse();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.post(new BigDecimal("0.000"))
        );

        assertEquals(
                "La quantità da trasferire deve essere positiva.",
                exception.getMessage()
        );

        verifyNoInteractions(
                fixture.warehouse,
                fixture.transfers
        );
        fixture.assertNoOtherDependencies();
    }
}
