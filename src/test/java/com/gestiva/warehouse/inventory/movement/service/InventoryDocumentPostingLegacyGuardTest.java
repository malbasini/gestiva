package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.logistics.ddt.entity.DeliveryNote;
import com.gestiva.logistics.ddt.repository.DeliveryNoteLineRepository;
import com.gestiva.logistics.ddt.repository.DeliveryNoteRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.service.InventoryAvailabilityService;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.location.repository.WarehouseRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryDocumentPostingLegacyGuardTest {

    @Mock
    private InventoryMovementService inventoryMovementService;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private GoodsReceiptLineRepository goodsReceiptLineRepository;

    @Mock
    private GoodsReceiptRepository goodsReceiptRepository;

    @Mock
    private DeliveryNoteLineRepository deliveryNoteLineRepository;

    @Mock
    private InventoryValuationService inventoryValuationService;

    @Mock
    private InventoryAvailabilityService inventoryAvailabilityService;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private DeliveryNoteRepository deliveryNoteRepository;

    @InjectMocks
    private InventoryDocumentPostingService service;

    @Test
    void legacySalesDeliveryMustBeBlocked() {

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.postSalesDeliveryFromDeliveryNote(
                        999L, new DeliveryNote()
                )
        );

        assertTrue(ex.getMessage().contains("legacy disabilitato"));

        verifyNoInteractions(
                inventoryMovementService,
                inventoryMovementRepository,
                itemRepository,
                goodsReceiptLineRepository,
                goodsReceiptRepository,
                deliveryNoteLineRepository,
                inventoryValuationService,
                inventoryAvailabilityService,
                warehouseRepository,
                deliveryNoteRepository
        );
    }

    @Test
    void legacyPurchaseReceiptMustBeBlocked() {

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.postPurchaseReceiptFromGoodsReceipt(
                        999L, new GoodsReceipt()
                )
        );

        assertTrue(ex.getMessage().contains("legacy disabilitato"));

        verifyNoInteractions(
                inventoryMovementService,
                inventoryMovementRepository,
                itemRepository,
                goodsReceiptLineRepository,
                goodsReceiptRepository,
                deliveryNoteLineRepository,
                inventoryValuationService,
                inventoryAvailabilityService,
                warehouseRepository,
                deliveryNoteRepository
        );
    }
}
