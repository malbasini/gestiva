package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.logistics.ddt.repository.DeliveryNoteLineRepository;
import com.gestiva.logistics.ddt.repository.DeliveryNoteRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.service.InventoryAvailabilityService;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;
import com.gestiva.warehouse.item.entity.Item;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.repository.WarehouseRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryReceiptPostingLocationPropagationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 3L;
    private static final Long WAREHOUSE = 50L;
    private static final Long LOCATION = 101L;
    private static final Long RECEIPT = 77L;
    private static final Long ITEM = 200L;
    private static final Long MOVEMENT = 901L;
    private static final LocalDate DATE = LocalDate.of(2026, 11, 19);

    @Mock InventoryMovementService movementService;
    @Mock InventoryMovementRepository movementRepository;
    @Mock ItemRepository itemRepository;
    @Mock GoodsReceiptLineRepository receiptLineRepository;
    @Mock GoodsReceiptRepository receiptRepository;
    @Mock DeliveryNoteLineRepository deliveryNoteLineRepository;
    @Mock InventoryValuationService valuationService;
    @Mock InventoryAvailabilityService availabilityService;
    @Mock WarehouseRepository warehouseRepository;
    @Mock DeliveryNoteRepository deliveryNoteRepository;
    @Mock InventoryLocationValidationService locationValidator;

    @InjectMocks
    InventoryDocumentPostingService service;

    private void prepareReceipt(Long locationId, boolean withStockLine) {
        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setWarehouseId(WAREHOUSE);
        receipt.setLocationId(locationId);
        receipt.setReceiptDate(DATE);
        receipt.setReceiptNumber("GR-LOCATION-TEST");

        Warehouse warehouse = mock(Warehouse.class);
        when(warehouse.isActive()).thenReturn(true);

        when(receiptRepository.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, RECEIPT
        )).thenReturn(Optional.of(receipt));

        when(warehouseRepository.findByTenantAndCompanyAndIdForUpdate(
                TENANT, COMPANY, WAREHOUSE
        )).thenReturn(Optional.of(warehouse));

        if (!withStockLine) {
            return;
        }

        GoodsReceiptLine line = new GoodsReceiptLine();
        line.setItemId(ITEM);
        line.setQuantityReceived(new BigDecimal("1.000"));
        line.setUnitCost(new BigDecimal("7.2000"));

        when(receiptLineRepository
                .findByTenantIdAndCompanyIdAndGoodsReceiptIdOrderByLineNoAsc(
                        TENANT, COMPANY, RECEIPT
                )).thenReturn(List.of(line));

        Item item = mock(Item.class);
        when(item.isTrackStock()).thenReturn(true);

        when(itemRepository.findByTenantIdAndId(
                TENANT, ITEM
        )).thenReturn(Optional.of(item));

        when(movementService.registerMovement(
                eq(TENANT),
                eq(COMPANY),
                eq(WAREHOUSE),
                eq(locationId),
                eq(ITEM),
                eq(DATE),
                eq("IN"),
                eq("PURCHASE_RECEIPT"),
                eq(new BigDecimal("1.000")),
                eq(new BigDecimal("7.2000")),
                eq("GOODS_RECEIPT"),
                eq(RECEIPT),
                anyString()
        )).thenReturn(MOVEMENT);
    }

    @Test
    void explicitLocationReachesInboundMovementAndFifo() {
        prepareReceipt(LOCATION, true);

        service.postPurchaseReceiptFromGoodsReceiptV2(
                TENANT, COMPANY, RECEIPT
        );

        var sequence = inOrder(locationValidator, movementService, valuationService);

        sequence.verify(locationValidator)
                .validateLocation(TENANT, WAREHOUSE, LOCATION);

        sequence.verify(movementService).registerMovement(
                eq(TENANT),
                eq(COMPANY),
                eq(WAREHOUSE),
                eq(LOCATION),
                eq(ITEM),
                eq(DATE),
                eq("IN"),
                eq("PURCHASE_RECEIPT"),
                eq(new BigDecimal("1.000")),
                eq(new BigDecimal("7.2000")),
                eq("GOODS_RECEIPT"),
                eq(RECEIPT),
                anyString()
        );

        sequence.verify(valuationService).applyInboundValuation(
                TENANT, COMPANY, WAREHOUSE, MOVEMENT
        );

        verifyNoMoreInteractions(movementService, valuationService);
    }

    @Test
    void nullLocationRemainsCompatibleWithHistoricalReceipts() {
        prepareReceipt(null, true);

        service.postPurchaseReceiptFromGoodsReceiptV2(
                TENANT, COMPANY, RECEIPT
        );

        verify(locationValidator).validateLocation(
                TENANT, WAREHOUSE, null
        );

        verify(movementService).registerMovement(
                eq(TENANT),
                eq(COMPANY),
                eq(WAREHOUSE),
                isNull(),
                eq(ITEM),
                eq(DATE),
                eq("IN"),
                eq("PURCHASE_RECEIPT"),
                eq(new BigDecimal("1.000")),
                eq(new BigDecimal("7.2000")),
                eq("GOODS_RECEIPT"),
                eq(RECEIPT),
                anyString()
        );

        verify(valuationService).applyInboundValuation(
                TENANT, COMPANY, WAREHOUSE, MOVEMENT
        );
    }

    @Test
    void rejectedLocationPreventsMovementAndFifo() {
        prepareReceipt(LOCATION, false);

        doThrow(new BusinessException("Ubicazione non valida."))
                .when(locationValidator)
                .validateLocation(TENANT, WAREHOUSE, LOCATION);

        assertThrows(
                BusinessException.class,
                () -> service.postPurchaseReceiptFromGoodsReceiptV2(
                        TENANT, COMPANY, RECEIPT
                )
        );

        verifyNoInteractions(
                receiptLineRepository,
                itemRepository,
                movementService,
                valuationService
        );
    }
}
