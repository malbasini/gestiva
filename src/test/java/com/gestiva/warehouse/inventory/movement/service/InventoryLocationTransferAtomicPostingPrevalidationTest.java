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
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryLocationTransferAtomicPostingPrevalidationTest {

    private static final class Request {
        Long tenantId = 2L;
        Long companyId = 1L;
        Long warehouseId = 5L;
        Long itemId = 10L;
        Long sourceLocationId = 100L;
        Long targetLocationId = 200L;
        BigDecimal quantity = new BigDecimal("7.000");
        LocalDate movementDate = LocalDate.of(2026, 11, 24);
        String notes = "Collaudo APB";
        String operationKey = "APB-TRANSFER-001";
    }

    private static final class Fixture {

        final WarehouseRepository warehouseRepository =
                mock(WarehouseRepository.class);

        final InventoryLocationTransferRepository transferRepository =
                mock(InventoryLocationTransferRepository.class);

        final InventoryLocationTransferAllocationRepository allocationRepository =
                mock(InventoryLocationTransferAllocationRepository.class);

        final InventoryLayerRepository layerRepository =
                mock(InventoryLayerRepository.class);

        final InventoryLayerConsumptionRepository consumptionRepository =
                mock(InventoryLayerConsumptionRepository.class);

        final InventoryMovementService movementService =
                mock(InventoryMovementService.class);

        final InventoryValuationService valuationService =
                mock(InventoryValuationService.class);

        final InventoryLocationTransferAtomicPostingCoordinator coordinator =
                new InventoryLocationTransferAtomicPostingCoordinator(
                        warehouseRepository,
                        transferRepository,
                        allocationRepository,
                        layerRepository,
                        consumptionRepository,
                        movementService,
                        valuationService
                );

        void post(Request request) {
            coordinator.post(
                    request.tenantId,
                    request.companyId,
                    request.warehouseId,
                    request.itemId,
                    request.sourceLocationId,
                    request.targetLocationId,
                    request.quantity,
                    request.movementDate,
                    request.notes,
                    request.operationKey
            );
        }

        void assertNoPersistenceInteractions() {
            verifyNoInteractions(
                    warehouseRepository,
                    transferRepository,
                    allocationRepository,
                    layerRepository,
                    consumptionRepository,
                    movementService,
                    valuationService
            );
        }
    }

    private static void reject(
            Consumer<Request> changes,
            String expectedMessage) {

        var request = new Request();
        changes.accept(request);

        var fixture = new Fixture();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.post(request)
        );

        assertEquals(
                expectedMessage,
                exception.getMessage()
        );

        fixture.assertNoPersistenceInteractions();
    }

    @Test
    void invalidTenantMustFailBeforeRepositories() {
        reject(
                request -> request.tenantId = 0L,
                "Tenant non valido."
        );
    }

    @Test
    void zeroQuantityMustFailBeforeRepositories() {
        reject(
                request ->
                        request.quantity = new BigDecimal("0.000"),
                "La quantità da trasferire deve essere positiva."
        );
    }

    @Test
    void sameLocationMustFailBeforeRepositories() {
        reject(
                request ->
                        request.targetLocationId = 100L,
                "Le ubicazioni di origine e destinazione devono essere diverse."
        );
    }

    @Test
    void blankOperationKeyMustFailBeforeRepositories() {
        reject(
                request ->
                        request.operationKey = "   ",
                "La chiave di idempotenza è obbligatoria."
        );
    }

    @Test
    void validRequestMustStillBeBlockedWithoutRepositoryCalls() {
        var request = new Request();
        var fixture = new Fixture();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.post(request)
        );

        assertEquals(
                "Magazzino non trovato nella società attiva.",
                exception.getMessage()
        );

        verify(fixture.warehouseRepository, times(1))
                .findByTenantAndCompanyAndIdForUpdate(
                        2L, 1L, 5L
                );

        verifyNoInteractions(
                fixture.transferRepository,
                fixture.allocationRepository,
                fixture.layerRepository,
                fixture.consumptionRepository,
                fixture.movementService,
                fixture.valuationService
        );
    }
}
