package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.movement.entity.InventoryMovement;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.repository.WarehouseRepository;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryMovementTransferReversalGuardTest {

    @Test
    void mustRejectIndividualReversalOfLocationTransferBeforeFifoChanges() {

        InventoryMovementRepository movementRepository =
                mock(InventoryMovementRepository.class);

        ItemRepository itemRepository =
                mock(ItemRepository.class);

        InventoryLocationValidationService locationValidator =
                mock(InventoryLocationValidationService.class);

        InventoryValuationService valuationService =
                mock(InventoryValuationService.class);

        WarehouseRepository warehouseRepository =
                mock(WarehouseRepository.class);

        InventoryMovement original = new InventoryMovement();
        ReflectionTestUtils.setField(original, "id", 55L);

        original.setTenantId(2L);
        original.setCompanyId(3L);
        original.setWarehouseId(2L);
        original.setLocationId(1L);
        original.setItemId(1L);

        original.setMovementType("OUT");
        original.setMovementDate(LocalDate.of(2026, 11, 20));

        original.setReferenceType("LOCATION_TRANSFER");
        original.setReferenceId(71L);

        original.setReversed(false);
        original.setReversalOfMovementId(null);

        when(movementRepository.findByTenantIdAndCompanyIdAndId(
                2L, 3L, 55L
        )).thenReturn(Optional.of(original));

        when(warehouseRepository.findByTenantAndCompanyAndIdForUpdate(
                2L, 3L, 2L
        )).thenReturn(Optional.of(mock(Warehouse.class)));

        when(movementRepository.findByTenantIdAndCompanyIdAndIdForUpdate(
                2L, 3L, 55L
        )).thenReturn(Optional.of(original));

        // Sentinella: se il servizio raggiunge la valorizzazione FIFO,
        // significa che la protezione del trasferimento manca.
        doThrow(new BusinessException(
                "STOP-AMT-FIFO-REACHED"
        )).when(valuationService).reverseFifoValuation(
                2L, 3L, 2L, 55L
        );

        InventoryMovementService service =
                new InventoryMovementService(
                        movementRepository,
                        itemRepository,
                        locationValidator,
                        valuationService,
                        warehouseRepository
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.reverseMovement(
                        2L,
                        3L,
                        55L,
                        LocalDate.of(2026, 11, 23),
                        "COLLAUDO-AMT-TRANSFER-GUARD"
                )
        );

        assertEquals(
                "Il movimento appartiene a un trasferimento "
                + "tra ubicazioni e non può essere stornato singolarmente.",
                exception.getMessage()
        );

        verify(movementRepository)
                .findByTenantIdAndCompanyIdAndId(
                        2L, 3L, 55L
                );

        verify(warehouseRepository)
                .findByTenantAndCompanyAndIdForUpdate(
                        2L, 3L, 2L
                );

        verify(movementRepository)
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        2L, 3L, 55L
                );

        verifyNoInteractions(
                locationValidator,
                valuationService
        );

        verify(movementRepository, never())
                .save(any(InventoryMovement.class));
    }
}
