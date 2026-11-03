package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.logistics.ddt.repository.DeliveryNoteLineRepository;
import com.gestiva.logistics.ddt.repository.DeliveryNoteRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.service.InventoryAvailabilityService;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.repository.WarehouseRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryDocumentPostingCompanyIsolationTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;
    private static final Long RECEIPT_ID = 77L;
    private static final Long WAREHOUSE_ID = 55L;

    @Mock InventoryMovementService inventoryMovementService;
    @Mock InventoryMovementRepository inventoryMovementRepository;
    @Mock ItemRepository itemRepository;
    @Mock GoodsReceiptLineRepository goodsReceiptLineRepository;
    @Mock GoodsReceiptRepository goodsReceiptRepository;
    @Mock DeliveryNoteLineRepository deliveryNoteLineRepository;
    @Mock InventoryValuationService inventoryValuationService;
    @Mock InventoryAvailabilityService inventoryAvailabilityService;
    @Mock WarehouseRepository warehouseRepository;
    @Mock DeliveryNoteRepository deliveryNoteRepository;

    @InjectMocks
    InventoryDocumentPostingService service;

    private void prepareReceiptAndWarehouse() {
        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setWarehouseId(WAREHOUSE_ID);

        Warehouse warehouse = new Warehouse();
        warehouse.setActive(true);

        when(goodsReceiptRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID, COMPANY_ID, RECEIPT_ID
        )).thenReturn(Optional.of(receipt));

        when(warehouseRepository.findByTenantAndCompanyAndIdForUpdate(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID
        )).thenReturn(Optional.of(warehouse));
    }

    @Test
    void shouldRejectMissingCompanyWithoutRepositoryAccess() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.postPurchaseReceiptFromGoodsReceiptV2(
                        TENANT_ID, null, RECEIPT_ID
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "Tenant, società e ricevimento merci sono obbligatori"
                )
        );

        verifyNoInteractions(
                goodsReceiptRepository,
                warehouseRepository,
                inventoryMovementRepository,
                goodsReceiptLineRepository,
                inventoryMovementService,
                inventoryValuationService
        );
    }

    @Test
    void shouldRejectReceiptOutsideSelectedCompany() {
        when(goodsReceiptRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID, COMPANY_ID, RECEIPT_ID
        )).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.postPurchaseReceiptFromGoodsReceiptV2(
                        TENANT_ID, COMPANY_ID, RECEIPT_ID
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "Ricevimento merci non trovato nella società indicata"
                )
        );

        verify(goodsReceiptRepository)
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, RECEIPT_ID
                );

        verifyNoInteractions(
                warehouseRepository,
                inventoryMovementRepository,
                goodsReceiptLineRepository,
                inventoryMovementService,
                inventoryValuationService
        );
    }

    @Test
    void shouldReadLinesWithCompanyScope() {
        prepareReceiptAndWarehouse();

        when(goodsReceiptLineRepository
                .findByTenantIdAndCompanyIdAndGoodsReceiptIdOrderByLineNoAsc(
                        TENANT_ID, COMPANY_ID, RECEIPT_ID
                ))
                .thenReturn(List.of());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.postPurchaseReceiptFromGoodsReceiptV2(
                        TENANT_ID, COMPANY_ID, RECEIPT_ID
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "Il ricevimento merci non contiene righe"
                )
        );

        verify(goodsReceiptLineRepository)
                .findByTenantIdAndCompanyIdAndGoodsReceiptIdOrderByLineNoAsc(
                        TENANT_ID, COMPANY_ID, RECEIPT_ID
                );


        verifyNoInteractions(
                inventoryMovementService,
                inventoryValuationService
        );
    }

    @Test
    void shouldNotRepostAlreadyPostedReceipt() {
        prepareReceiptAndWarehouse();

        when(inventoryMovementRepository
                .existsByTenantIdAndCompanyIdAndReferenceTypeAndReferenceIdAndCausalCode(
                        TENANT_ID,
                        COMPANY_ID,
                        "GOODS_RECEIPT",
                        RECEIPT_ID,
                        "PURCHASE_RECEIPT"
                ))
                .thenReturn(true);

        assertDoesNotThrow(
                () -> service.postPurchaseReceiptFromGoodsReceiptV2(
                        TENANT_ID, COMPANY_ID, RECEIPT_ID
                )
        );

        verify(inventoryMovementRepository)
                .existsByTenantIdAndCompanyIdAndReferenceTypeAndReferenceIdAndCausalCode(
                        TENANT_ID,
                        COMPANY_ID,
                        "GOODS_RECEIPT",
                        RECEIPT_ID,
                        "PURCHASE_RECEIPT"
                );

        verifyNoInteractions(
                goodsReceiptLineRepository,
                inventoryMovementService,
                inventoryValuationService
        );
    }
}
