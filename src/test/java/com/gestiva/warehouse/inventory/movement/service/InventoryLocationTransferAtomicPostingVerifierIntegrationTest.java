package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransfer;
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

class InventoryLocationTransferAtomicPostingVerifierIntegrationTest {

    private static final LocalDate DATE =
            LocalDate.of(2026, 11, 24);

    private static final String KEY = "APP-TRANSFER-001";

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

        final InventoryLocationTransferPostedEvidenceService evidence =
                mock(InventoryLocationTransferPostedEvidenceService.class);

        final InventoryLocationTransferAtomicPostingCoordinator coordinator =
                new InventoryLocationTransferAtomicPostingCoordinator(
                        warehouse,
                        transfers,
                        allocations,
                        layers,
                        consumptions,
                        movements,
                        valuation,
                        evidence
                );

        void provideWarehouse() throws Exception {
            var method = WarehouseRepository.class.getMethod(
                    "findByTenantAndCompanyAndIdForUpdate",
                    Long.class,
                    Long.class,
                    Long.class
            );

            var genericType = assertInstanceOf(
                    ParameterizedType.class,
                    method.getGenericReturnType()
            );

            Class<?> entityType = assertInstanceOf(
                    Class.class,
                    genericType.getActualTypeArguments()[0]
            );

            Object warehouseEntity = mock(entityType);

            doReturn(Optional.of(warehouseEntity))
                    .when(warehouse)
                    .findByTenantAndCompanyAndIdForUpdate(
                            2L, 1L, 5L
                    );
        }

        void provideExistingTransfer(InventoryLocationTransfer existing) {
            when(transfers.findByTenantIdAndCompanyIdAndOperationKey(
                    2L, 1L, KEY
            )).thenReturn(Optional.of(existing));
        }

        BusinessException postAndCapture(BigDecimal quantity) {
            return assertThrows(
                    BusinessException.class,
                    () -> coordinator.post(
                            2L,
                            1L,
                            5L,
                            10L,
                            100L,
                            200L,
                            quantity,
                            DATE,
                            "Collaudo APP",
                            KEY
                    )
            );
        }

        void verifyOnlyReads() {
            verify(warehouse, times(1))
                    .findByTenantAndCompanyAndIdForUpdate(
                            2L, 1L, 5L
                    );

            verify(transfers, times(1))
                    .findByTenantIdAndCompanyIdAndOperationKey(
                            2L, 1L, KEY
                    );

            verifyNoMoreInteractions(warehouse, transfers);

            verifyNoInteractions(
                    allocations,
                    layers,
                    consumptions,
                    movements,
                    valuation
            );
        }

        void verifyNoDependencies() {
            verifyNoInteractions(
                    warehouse,
                    transfers,
                    allocations,
                    layers,
                    consumptions,
                    movements,
                    valuation
            );
        }
    }

    private static InventoryLocationTransfer existingTransfer() {
        var transfer = new InventoryLocationTransfer();

        transfer.setTenantId(2L);
        transfer.setCompanyId(1L);
        transfer.setWarehouseId(5L);
        transfer.setItemId(10L);
        transfer.setSourceLocationId(100L);
        transfer.setTargetLocationId(200L);
        transfer.setQuantity(new BigDecimal("7.000"));
        transfer.setMovementDate(DATE);
        transfer.setOperationKey(KEY);

        return transfer;
    }

    @Test
    void existingKeyWithDifferentQuantityMustRejectConflict()
            throws Exception {

        var fixture = new Fixture();
        fixture.provideWarehouse();

        var existing = existingTransfer();
        existing.setStatus("POSTED");
        existing.setOutMovementId(101L);
        existing.setInMovementId(102L);

        fixture.provideExistingTransfer(existing);

        BusinessException exception =
                fixture.postAndCapture(new BigDecimal("8.000"));

        try {
            assertEquals(
                    "La chiave di idempotenza è già associata "
                            + "a un trasferimento con parametri differenti.",
                    exception.getMessage()
            );
        } catch (AssertionError error) {
            fail(
                    "STOP-APP-REQUEST-VERIFIER-NON-COLLEGATO",
                    error
            );
        }

        fixture.verifyOnlyReads();
    }

    @Test
    void matchingPendingTransferMustBeRejectedByStateVerifier()
            throws Exception {

        var fixture = new Fixture();
        fixture.provideWarehouse();

        var existing = existingTransfer();
        existing.setStatus("PENDING");

        fixture.provideExistingTransfer(existing);

        BusinessException exception =
                fixture.postAndCapture(new BigDecimal("7.000"));

        try {
            assertEquals(
                    "Il trasferimento è ancora in stato PENDING.",
                    exception.getMessage()
            );
        } catch (AssertionError error) {
            fail(
                    "STOP-APP-STATE-VERIFIER-NON-COLLEGATO",
                    error
            );
        }

        fixture.verifyOnlyReads();
    }

    @Test
    void matchingPostedTransferMustStillBeBlocked()
            throws Exception {

        var fixture = new Fixture();
        fixture.provideWarehouse();

        var existing = existingTransfer();
        existing.setStatus("POSTED");
        existing.setOutMovementId(101L);
        existing.setInMovementId(102L);

        fixture.provideExistingTransfer(existing);

        BusinessException exception =
                fixture.postAndCapture(new BigDecimal("7.000"));

        assertEquals(
                "Riutilizzo del trasferimento POSTED "
                        + "non ancora abilitato.",
                exception.getMessage()
        );

        fixture.verifyOnlyReads();
        verify(fixture.evidence)
                .verifyPostedCompleteness(existing);
        verifyNoMoreInteractions(fixture.evidence);
    }

    @Test
    void inconsistentPostedEvidenceMustBlockIdReuse()
            throws Exception {

        var fixture = new Fixture();
        fixture.provideWarehouse();

        var existing = existingTransfer();
        existing.setStatus("POSTED");
        existing.setOutMovementId(101L);
        existing.setInMovementId(102L);

        fixture.provideExistingTransfer(existing);

        doThrow(new BusinessException(
                "Evidenze FIFO incoerenti."
        )).when(fixture.evidence)
                .verifyPostedCompleteness(existing);

        BusinessException exception =
                fixture.postAndCapture(new BigDecimal("7.000"));

        assertEquals(
                "Evidenze FIFO incoerenti.",
                exception.getMessage()
        );

        fixture.verifyOnlyReads();

        verify(fixture.evidence)
                .verifyPostedCompleteness(existing);
        verifyNoMoreInteractions(fixture.evidence);
    }

    @Test
    void invalidRequestMustStopBeforeAnyRepositoryAccess() {

        var fixture = new Fixture();

        BusinessException exception =
                fixture.postAndCapture(BigDecimal.ZERO);

        assertEquals(
                "La quantità da trasferire deve essere positiva.",
                exception.getMessage()
        );

        fixture.verifyNoDependencies();
    }
}
