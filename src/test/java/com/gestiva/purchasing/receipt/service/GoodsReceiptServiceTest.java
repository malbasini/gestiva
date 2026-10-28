package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.movement.service.InventoryDocumentPostingService;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.location.repository.WarehouseRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoodsReceiptServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private PurchaseOrderLineRepository purchaseOrderLineRepository;

    @Mock
    private GoodsReceiptRepository goodsReceiptRepository;

    @Mock
    private GoodsReceiptLineRepository goodsReceiptLineRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InventoryDocumentPostingService inventoryDocumentPostingService;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private DocumentSequenceService documentSequenceService;

    @Mock
    private CompanyContext companyContext;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private Environment environment;

    @InjectMocks
    private GoodsReceiptService service;

    @Test
    void shouldRejectGoodsReceiptWhenFeatureDisabled() {

        Long tenantId = 2L;
        Long purchaseOrderId = 100L;
        Long warehouseId = 1L;

        when(environment.getActiveProfiles())
                .thenReturn(new String[]{"test"});

        when(environment.getProperty(
                "gestiva.purchasing.goods-receipt.enabled",
                Boolean.class,
                false
        )).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createFromPurchaseOrder(
                        tenantId,
                        purchaseOrderId,
                        warehouseId
                )
        );

        assertEquals(
                "Ricezione merci temporaneamente non disponibile.",
                exception.getMessage()
        );

        verifyNoInteractions(
                companyContext,
                warehouseRepository,
                purchaseOrderRepository,
                purchaseOrderLineRepository,
                goodsReceiptRepository,
                goodsReceiptLineRepository,
                itemRepository,
                inventoryDocumentPostingService,
                inventoryMovementRepository,
                documentSequenceService
        );
    }


    @Test
    void shouldRejectGoodsReceiptForDraftPurchaseOrder() {

        Long tenantId = 2L;
        Long companyId = 1L;
        Long purchaseOrderId = 100L;
        Long warehouseId = 1L;

        when(environment.getActiveProfiles())
                .thenReturn(new String[]{"test"});

        when(environment.getProperty(
                "gestiva.purchasing.goods-receipt.enabled",
                Boolean.class,
                false
        )).thenReturn(true);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(companyId);

        com.gestiva.warehouse.location.entity.Warehouse warehouse =
                new com.gestiva.warehouse.location.entity.Warehouse();

        warehouse.setActive(true);

        when(warehouseRepository.findByTenantIdAndCompanyIdAndId(
                tenantId,
                companyId,
                warehouseId
        )).thenReturn(java.util.Optional.of(warehouse));

        com.gestiva.purchasing.order.entity.PurchaseOrder order =
                new com.gestiva.purchasing.order.entity.PurchaseOrder();

        order.setStatus("DRAFT");

        when(purchaseOrderRepository.findLockedByTenantIdAndCompanyIdAndId(
                tenantId,
                companyId,
                purchaseOrderId
        )).thenReturn(java.util.Optional.of(order));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createFromPurchaseOrder(
                        tenantId,
                        purchaseOrderId,
                        warehouseId
                )
        );

        assertEquals(
                "Solo gli ordini fornitore confermati possono essere ricevuti.",
                exception.getMessage()
        );

        verify(warehouseRepository)
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        warehouseId
                );

        verify(purchaseOrderRepository)
                .findLockedByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        purchaseOrderId
                );

        verifyNoInteractions(
                goodsReceiptRepository,
                goodsReceiptLineRepository,
                purchaseOrderLineRepository,
                itemRepository,
                inventoryDocumentPostingService,
                inventoryMovementRepository,
                documentSequenceService
        );
    }


    @Test
    void shouldRejectGoodsReceiptOutsideTestProfile() {

        Long tenantId = 2L;
        Long purchaseOrderId = 100L;
        Long warehouseId = 1L;

        when(environment.getActiveProfiles())
                .thenReturn(new String[]{"prod"});

        when(environment.getProperty(
                "gestiva.purchasing.goods-receipt.enabled",
                Boolean.class,
                false
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createFromPurchaseOrder(
                        tenantId,
                        purchaseOrderId,
                        warehouseId
                )
        );

        assertEquals(
                "Ricezione merci temporaneamente non disponibile.",
                exception.getMessage()
        );

        verifyNoInteractions(
                companyContext,
                warehouseRepository,
                purchaseOrderRepository,
                purchaseOrderLineRepository,
                goodsReceiptRepository,
                goodsReceiptLineRepository,
                itemRepository,
                inventoryDocumentPostingService,
                inventoryMovementRepository,
                documentSequenceService
        );
    }
}
