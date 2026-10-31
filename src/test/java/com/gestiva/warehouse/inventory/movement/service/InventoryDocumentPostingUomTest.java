package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.logistics.ddt.entity.DeliveryNote;
import com.gestiva.logistics.ddt.entity.DeliveryNoteLine;
import com.gestiva.logistics.ddt.repository.DeliveryNoteLineRepository;
import com.gestiva.logistics.ddt.repository.DeliveryNoteRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.service.InventoryAvailabilityService;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;
import com.gestiva.warehouse.item.entity.Item;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryDocumentPostingUomTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long DDT_ID = 100L;
    private static final Long WAREHOUSE = 10L;
    private static final Long ITEM_ID = 20L;

    @Mock private InventoryMovementService movementService;
    @Mock private InventoryMovementRepository movementRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private DeliveryNoteLineRepository lineRepository;
    @Mock private InventoryValuationService valuationService;
    @Mock private InventoryAvailabilityService availabilityService;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private DeliveryNoteRepository deliveryNoteRepository;

    @InjectMocks
    private InventoryDocumentPostingService service;

    private DeliveryNote note;
    private Item item;

    @BeforeEach
    void setup() {
        note = mock(DeliveryNote.class);
        item = mock(Item.class);
    }

    private void prepare(DeliveryNoteLine line) {
        when(deliveryNoteRepository.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, DDT_ID
        )).thenReturn(Optional.of(note));

        when(note.getWarehouseId()).thenReturn(WAREHOUSE);

        when(warehouseRepository.findByTenantAndCompanyAndIdForUpdate(
                TENANT, COMPANY, WAREHOUSE
        )).thenReturn(Optional.of(
                mock(com.gestiva.warehouse.location.entity.Warehouse.class)
        ));

        when(movementRepository
                .existsByTenantIdAndCompanyIdAndReferenceTypeAndReferenceIdAndCausalCode(
                        TENANT, COMPANY, "DELIVERY_NOTE", DDT_ID,
                        "SALES_DELIVERY"
                )).thenReturn(false);

        when(lineRepository
                .findByTenantIdAndCompanyIdAndDeliveryNoteIdOrderByLineNoAsc(
                        TENANT, COMPANY, DDT_ID
                )).thenReturn(List.of(line));

        when(itemRepository.findByTenantIdAndId(TENANT, ITEM_ID))
                .thenReturn(Optional.of(item));

        when(item.isTrackStock()).thenReturn(true);
    }

    private DeliveryNoteLine line(String quantity, String factor) {
        DeliveryNoteLine line = new DeliveryNoteLine();
        line.setLineNo(1);
        line.setItemId(ITEM_ID);
        line.setQuantity(new BigDecimal(quantity));
        line.setConversionFactor(
                factor == null ? null : new BigDecimal(factor)
        );
        return line;
    }

    @Test
    void box2ConFattore10Scarica20Pezzi() {
        prepare(line("2", "10"));

        when(movementService.registerMovement(
                any(), any(), any(), any(), any(), any(),
                eq("OUT"), eq("SALES_DELIVERY"),
                any(BigDecimal.class), any(),
                eq("DELIVERY_NOTE"), eq(DDT_ID), any()
        )).thenReturn(500L);

        service.postSalesDeliveryFromDeliveryNoteV2(
                TENANT, COMPANY, DDT_ID
        );

        verify(availabilityService).validateAvailabilityByCompanyAndWarehouse(
                eq(TENANT), eq(COMPANY), eq(WAREHOUSE),
                eq(ITEM_ID), any(), eq(new BigDecimal("20.000"))
        );

        verify(movementService).registerMovement(
                eq(TENANT), eq(COMPANY), eq(WAREHOUSE),
                any(), eq(ITEM_ID), any(),
                eq("OUT"), eq("SALES_DELIVERY"),
                eq(new BigDecimal("20.000")), any(),
                eq("DELIVERY_NOTE"), eq(DDT_ID), any()
        );

        verify(valuationService).applyOutboundValuation(
                TENANT, COMPANY, WAREHOUSE, 500L
        );
    }

    @Test
    void unitaBaseConFattore1Scarica2Pezzi() {
        prepare(line("2", "1"));

        service.postSalesDeliveryFromDeliveryNoteV2(
                TENANT, COMPANY, DDT_ID
        );

        verify(availabilityService).validateAvailabilityByCompanyAndWarehouse(
                eq(TENANT), eq(COMPANY), eq(WAREHOUSE),
                eq(ITEM_ID), any(), eq(new BigDecimal("2.000"))
        );

        verify(movementService).registerMovement(
                any(), any(), any(), any(), any(), any(),
                eq("OUT"), eq("SALES_DELIVERY"),
                eq(new BigDecimal("2.000")), any(),
                eq("DELIVERY_NOTE"), eq(DDT_ID), any()
        );
    }

    @Test
    void fattoreZeroRifiutatoSenzaMovimenti() {
        prepare(line("2", "0"));

        assertThrows(
                BusinessException.class,
                () -> service.postSalesDeliveryFromDeliveryNoteV2(
                        TENANT, COMPANY, DDT_ID
                )
        );

        verifyNoInteractions(availabilityService, movementService);
    }

    @Test
    void ddtGiaMovimentatoNonGeneraDuplicati() {
        when(deliveryNoteRepository.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, DDT_ID
        )).thenReturn(Optional.of(note));

        when(note.getWarehouseId()).thenReturn(WAREHOUSE);

        when(warehouseRepository.findByTenantAndCompanyAndIdForUpdate(
                TENANT, COMPANY, WAREHOUSE
        )).thenReturn(Optional.of(
                mock(com.gestiva.warehouse.location.entity.Warehouse.class)
        ));

        when(movementRepository
                .existsByTenantIdAndCompanyIdAndReferenceTypeAndReferenceIdAndCausalCode(
                        TENANT, COMPANY, "DELIVERY_NOTE", DDT_ID,
                        "SALES_DELIVERY"
                )).thenReturn(true);

        service.postSalesDeliveryFromDeliveryNoteV2(
                TENANT, COMPANY, DDT_ID
        );

        verifyNoInteractions(
                lineRepository, availabilityService, movementService
        );
    }
}
