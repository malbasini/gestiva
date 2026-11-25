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

class InventoryLocationTransferAtomicPostingPostedIdReuseTest {

    private static final LocalDate DATE =
            LocalDate.of(2026, 11, 25);

    private static final String KEY = "AWF-POSTED-001";

    private final WarehouseRepository warehouses =
            mock(WarehouseRepository.class);

    private final InventoryLocationTransferRepository transfers =
            mock(InventoryLocationTransferRepository.class);

    private final InventoryLocationTransferAllocationRepository allocations =
            mock(InventoryLocationTransferAllocationRepository.class);

    private final InventoryLayerRepository layers =
            mock(InventoryLayerRepository.class);

    private final InventoryLayerConsumptionRepository consumptions =
            mock(InventoryLayerConsumptionRepository.class);

    private final InventoryMovementService movements =
            mock(InventoryMovementService.class);

    private final InventoryValuationService valuation =
            mock(InventoryValuationService.class);

    private final InventoryLocationTransferPostedEvidenceService evidence =
            mock(InventoryLocationTransferPostedEvidenceService.class);

    private final InventoryLocationTransferAtomicPostingWriter writer =
            mock(InventoryLocationTransferAtomicPostingWriter.class);

    private final InventoryLocationTransferAtomicPostingCoordinator coordinator =
            new InventoryLocationTransferAtomicPostingCoordinator(
                    warehouses,
                    transfers,
                    allocations,
                    layers,
                    consumptions,
                    movements,
                    valuation,
                    evidence,
                    writer
            );

    private InventoryLocationTransfer existing(
            Long transferId, String status, BigDecimal quantity) {

        InventoryLocationTransfer transfer =
                mock(InventoryLocationTransfer.class);

        when(transfer.getId()).thenReturn(transferId);
        when(transfer.getTenantId()).thenReturn(2L);
        when(transfer.getCompanyId()).thenReturn(1L);
        when(transfer.getWarehouseId()).thenReturn(5L);
        when(transfer.getItemId()).thenReturn(10L);
        when(transfer.getSourceLocationId()).thenReturn(100L);
        when(transfer.getTargetLocationId()).thenReturn(200L);
        when(transfer.getQuantity()).thenReturn(quantity);
        when(transfer.getMovementDate()).thenReturn(DATE);
        when(transfer.getStatus()).thenReturn(status);
        when(transfer.getOutMovementId()).thenReturn(101L);
        when(transfer.getInMovementId()).thenReturn(102L);

        return transfer;
    }

    private void provideWarehouseAndTransfer(
            InventoryLocationTransfer existing) throws Exception {

        var method = WarehouseRepository.class.getMethod(
                "findByTenantAndCompanyAndIdForUpdate",
                Long.class, Long.class, Long.class
        );

        var type = (ParameterizedType) method.getGenericReturnType();
        Class<?> entityType =
                (Class<?>) type.getActualTypeArguments()[0];

        Object warehouse = mock(entityType);

        doReturn(Optional.of(warehouse))
                .when(warehouses)
                .findByTenantAndCompanyAndIdForUpdate(
                        2L, 1L, 5L);

        when(transfers.findByTenantIdAndCompanyIdAndOperationKey(
                2L, 1L, KEY))
                .thenReturn(Optional.of(existing));
    }

    private Long post(BigDecimal quantity) {
        return coordinator.post(
                2L, 1L, 5L, 10L, 100L, 200L,
                quantity, DATE, "Test AWF", KEY
        );
    }

    @Test
    void coherentPostedReturnsExistingIdWithoutWriting()
            throws Exception {

        var transfer = existing(
                77L, "POSTED", new BigDecimal("7.000"));

        provideWarehouseAndTransfer(transfer);

        Long result = post(new BigDecimal("7.000"));

        assertEquals(77L, result);

        verify(evidence).verifyPostedCompleteness(transfer);
        verifyNoInteractions(writer);
        verifyNoInteractions(
                allocations, layers, consumptions, movements, valuation);
    }

    @Test
    void differentRequestCannotReuseId()
            throws Exception {

        var transfer = existing(
                77L, "POSTED", new BigDecimal("7.000"));

        provideWarehouseAndTransfer(transfer);

        assertThrows(BusinessException.class,
                () -> post(new BigDecimal("8.000")));

        verifyNoInteractions(evidence, writer);
    }

    @Test
    void pendingTransferCannotReuseId()
            throws Exception {

        var transfer = existing(
                77L, "PENDING", new BigDecimal("7.000"));

        provideWarehouseAndTransfer(transfer);

        assertThrows(BusinessException.class,
                () -> post(new BigDecimal("7.000")));

        verifyNoInteractions(evidence, writer);
    }

    @Test
    void inconsistentEvidenceCannotReuseId()
            throws Exception {

        var transfer = existing(
                77L, "POSTED", new BigDecimal("7.000"));

        provideWarehouseAndTransfer(transfer);

        doThrow(new BusinessException("Evidenze FIFO incoerenti."))
                .when(evidence)
                .verifyPostedCompleteness(transfer);

        BusinessException failure = assertThrows(
                BusinessException.class,
                () -> post(new BigDecimal("7.000")));

        assertEquals(
                "Evidenze FIFO incoerenti.",
                failure.getMessage());

        verifyNoInteractions(writer);
    }

    @Test
    void invalidPostedIdCannotBeReturned()
            throws Exception {

        var transfer = existing(
                null, "POSTED", new BigDecimal("7.000"));

        provideWarehouseAndTransfer(transfer);

        BusinessException failure = assertThrows(
                BusinessException.class,
                () -> post(new BigDecimal("7.000")));

        assertEquals(
                "Il trasferimento POSTED non ha un ID valido.",
                failure.getMessage());

        verify(evidence).verifyPostedCompleteness(transfer);
        verifyNoInteractions(writer);
    }
}
