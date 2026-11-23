package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.warehouse.inventory.movement.entity.InventoryMovement;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.repository.WarehouseRepository;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryMovementOrdinaryReversalRegressionTest {

    @Test
    void ordinaryOutboundReversalMustRemainAvailable() {

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
        original.setItemId(7L);

        original.setMovementType("OUT");
        original.setCausalCode("SALES_DELIVERY");

        original.setMovementDate(LocalDate.of(2026, 11, 20));
        original.setQuantity(new BigDecimal("7.000"));
        original.setUnitCost(new BigDecimal("7.4571"));
        original.setTotalCost(new BigDecimal("52.2000"));

        original.setReferenceType("SALES_DOCUMENT");
        original.setReferenceId(88L);

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

        when(movementRepository.save(any(InventoryMovement.class)))
                .thenAnswer(invocation -> {
                    InventoryMovement movement =
                            invocation.getArgument(0);

                    if (movement != original) {
                        ReflectionTestUtils.setField(
                                movement, "id", 56L
                        );
                    }

                    return movement;
                });

        InventoryMovementService service =
                new InventoryMovementService(
                        movementRepository,
                        itemRepository,
                        locationValidator,
                        valuationService,
                        warehouseRepository
                );

        Long reversalId = service.reverseMovement(
                2L,
                3L,
                55L,
                LocalDate.of(2026, 11, 23),
                "COLLAUDO-AMX-STORNO-ORDINARIO"
        );

        assertEquals(56L, reversalId);
        assertTrue(original.isReversed());

        ArgumentCaptor<InventoryMovement> movementCaptor =
                ArgumentCaptor.forClass(InventoryMovement.class);

        InOrder order = inOrder(
                movementRepository,
                warehouseRepository,
                locationValidator,
                valuationService
        );

        order.verify(movementRepository)
                .findByTenantIdAndCompanyIdAndId(
                        2L, 3L, 55L
                );

        order.verify(warehouseRepository)
                .findByTenantAndCompanyAndIdForUpdate(
                        2L, 3L, 2L
                );

        order.verify(movementRepository)
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        2L, 3L, 55L
                );

        order.verify(locationValidator).validate(
                2L, 3L, 2L, 1L
        );

        order.verify(valuationService).reverseFifoValuation(
                2L, 3L, 2L, 55L
        );

        order.verify(movementRepository, times(2))
                .save(movementCaptor.capture());

        order.verifyNoMoreInteractions();

        var saved = movementCaptor.getAllValues();

        assertEquals(2, saved.size());

        InventoryMovement reversal = saved.get(0);

        assertNotSame(original, reversal);
        assertSame(original, saved.get(1));

        assertEquals("IN", reversal.getMovementType());
        assertEquals(
                "DOCUMENT_CANCEL_RESTORE",
                reversal.getCausalCode()
        );

        assertEquals(55L, reversal.getReversalOfMovementId());
        assertEquals(2L, reversal.getTenantId());
        assertEquals(3L, reversal.getCompanyId());
        assertEquals(2L, reversal.getWarehouseId());
        assertEquals(1L, reversal.getLocationId());
        assertEquals(7L, reversal.getItemId());

        assertEquals(
                0,
                new BigDecimal("7.000")
                        .compareTo(reversal.getQuantity())
        );

        assertEquals(
                0,
                new BigDecimal("52.2000")
                        .compareTo(reversal.getTotalCost())
        );

        assertEquals(
                "SALES_DOCUMENT",
                reversal.getReferenceType()
        );

        assertEquals(88L, reversal.getReferenceId());
        assertFalse(reversal.isReversed());

        verifyNoInteractions(itemRepository);
    }
}
