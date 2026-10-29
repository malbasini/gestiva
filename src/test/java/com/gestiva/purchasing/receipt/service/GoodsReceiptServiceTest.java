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
    void shouldRejectDuplicateGoodsReceipt() {

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

        order.setStatus("CONFIRMED");

        when(purchaseOrderRepository.findLockedByTenantIdAndCompanyIdAndId(
                tenantId,
                companyId,
                purchaseOrderId
        )).thenReturn(java.util.Optional.of(order));

        when(goodsReceiptRepository
                .existsByTenantIdAndCompanyIdAndPurchaseOrderId(
                        tenantId,
                        companyId,
                        purchaseOrderId
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
                "Esiste già una ricezione merci per questo ordine.",
                exception.getMessage()
        );

        verify(goodsReceiptRepository)
                .existsByTenantIdAndCompanyIdAndPurchaseOrderId(
                        tenantId,
                        companyId,
                        purchaseOrderId
                );

        verify(goodsReceiptRepository, never())
                .save(any());

        verifyNoInteractions(
                goodsReceiptLineRepository,
                purchaseOrderLineRepository,
                itemRepository,
                inventoryDocumentPostingService,
                inventoryMovementRepository,
                documentSequenceService
        );
    }


    @Test
    void shouldRejectWarehouseOutsideCurrentCompany() {

        Long tenantId = 2L;
        Long companyId = 1L;
        Long purchaseOrderId = 100L;
        Long warehouseId = 99L;

        when(environment.getActiveProfiles())
                .thenReturn(new String[]{"test"});

        when(environment.getProperty(
                "gestiva.purchasing.goods-receipt.enabled",
                Boolean.class,
                false
        )).thenReturn(true);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(companyId);

        when(warehouseRepository.findByTenantIdAndCompanyIdAndId(
                tenantId,
                companyId,
                warehouseId
        )).thenReturn(java.util.Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createFromPurchaseOrder(
                        tenantId,
                        purchaseOrderId,
                        warehouseId
                )
        );

        assertEquals(
                "Magazzino non trovato nella società corrente.",
                exception.getMessage()
        );

        verify(warehouseRepository)
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        warehouseId
                );

        verifyNoInteractions(
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
    void shouldRejectInactiveWarehouse() {

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

        warehouse.setActive(false);

        when(warehouseRepository.findByTenantIdAndCompanyIdAndId(
                tenantId,
                companyId,
                warehouseId
        )).thenReturn(java.util.Optional.of(warehouse));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createFromPurchaseOrder(
                        tenantId,
                        purchaseOrderId,
                        warehouseId
                )
        );

        assertEquals(
                "Il magazzino selezionato non è attivo.",
                exception.getMessage()
        );

        verify(warehouseRepository)
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        warehouseId
                );

        verifyNoInteractions(
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
    void shouldRejectMissingWarehouse() {

        Long tenantId = 2L;
        Long companyId = 1L;
        Long purchaseOrderId = 100L;

        when(environment.getActiveProfiles())
                .thenReturn(new String[]{"test"});

        when(environment.getProperty(
                "gestiva.purchasing.goods-receipt.enabled",
                Boolean.class,
                false
        )).thenReturn(true);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(companyId);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createFromPurchaseOrder(
                        tenantId,
                        purchaseOrderId,
                        null
                )
        );

        assertEquals(
                "Seleziona un magazzino di destinazione.",
                exception.getMessage()
        );

        verify(companyContext)
                .requireCurrentCompanyId();

        verifyNoInteractions(
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
    void shouldRejectPurchaseOrderWithoutLines() {

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

        order.setStatus("CONFIRMED");

        when(purchaseOrderRepository.findLockedByTenantIdAndCompanyIdAndId(
                tenantId,
                companyId,
                purchaseOrderId
        )).thenReturn(java.util.Optional.of(order));

        when(goodsReceiptRepository
                .existsByTenantIdAndCompanyIdAndPurchaseOrderId(
                        tenantId,
                        companyId,
                        purchaseOrderId
                )).thenReturn(false);

        when(purchaseOrderLineRepository
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        tenantId,
                        purchaseOrderId
                )).thenReturn(java.util.Collections.emptyList());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createFromPurchaseOrder(
                        tenantId,
                        purchaseOrderId,
                        warehouseId
                )
        );

        assertEquals(
                "L'ordine fornitore non contiene righe.",
                exception.getMessage()
        );

        verify(purchaseOrderLineRepository)
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        tenantId,
                        purchaseOrderId
                );

        verify(goodsReceiptRepository, never())
                .save(any());

        verifyNoInteractions(
                goodsReceiptLineRepository,
                itemRepository,
                inventoryDocumentPostingService,
                inventoryMovementRepository,
                documentSequenceService
        );
    }


    @Test
    void shouldConvertUomAndCalculateNetFifoCost() {

        Long tenantId = 2L;
        Long companyId = 1L;
        Long purchaseOrderId = 100L;
        Long purchaseOrderLineId = 200L;
        Long warehouseId = 1L;
        Long receiptId = 300L;

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

        order.setId(purchaseOrderId);
        order.setStatus("CONFIRMED");
        order.setSupplierId(10L);
        order.setOrderNumber("PO-TEST-UOM");

        when(purchaseOrderRepository.findLockedByTenantIdAndCompanyIdAndId(
                tenantId,
                companyId,
                purchaseOrderId
        )).thenReturn(java.util.Optional.of(order));

        when(goodsReceiptRepository
                .existsByTenantIdAndCompanyIdAndPurchaseOrderId(
                        tenantId,
                        companyId,
                        purchaseOrderId
                )).thenReturn(false);

        com.gestiva.purchasing.order.entity.PurchaseOrderLine orderLine =
                new com.gestiva.purchasing.order.entity.PurchaseOrderLine();

        orderLine.setId(purchaseOrderLineId);
        orderLine.setPurchaseOrderId(purchaseOrderId);
        orderLine.setUomId(5L);
        orderLine.setQuantity(new java.math.BigDecimal("2"));
        orderLine.setConversionFactor(
                new java.math.BigDecimal("10")
        );
        orderLine.setLineSubtotal(
                new java.math.BigDecimal("144.00")
        );

        // Nessun articolo inventariale simulato:
        // il test verifica il calcolo della riga e
        // l'invocazione del servizio di registrazione.
        orderLine.setItemId(null);

        when(purchaseOrderLineRepository
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        tenantId,
                        purchaseOrderId
                )).thenReturn(java.util.List.of(orderLine));

        when(documentSequenceService.nextNumber(
                tenantId,
                companyId,
                DocumentSequenceService.GOODS_RECEIPT
        )).thenReturn("GR-TEST-UOM");

        when(goodsReceiptRepository.save(
                any(com.gestiva.purchasing.receipt.entity.GoodsReceipt.class)
        )).thenAnswer(invocation -> {

            com.gestiva.purchasing.receipt.entity.GoodsReceipt receipt =
                    invocation.getArgument(0);

            receipt.setId(receiptId);

            return receipt;
        });

        Long result = service.createFromPurchaseOrder(
                tenantId,
                purchaseOrderId,
                warehouseId
        );

        assertEquals(receiptId, result);

        org.mockito.ArgumentCaptor<
                com.gestiva.purchasing.receipt.entity.GoodsReceiptLine
        > lineCaptor = org.mockito.ArgumentCaptor.forClass(
                com.gestiva.purchasing.receipt.entity.GoodsReceiptLine.class
        );

        verify(goodsReceiptLineRepository, times(1))
                .save(lineCaptor.capture());

        com.gestiva.purchasing.receipt.entity.GoodsReceiptLine savedLine =
                lineCaptor.getValue();

        assertEquals(
                0,
                new java.math.BigDecimal("20.000")
                        .compareTo(savedLine.getQuantityReceived())
        );

        assertEquals(
                0,
                new java.math.BigDecimal("7.2000")
                        .compareTo(savedLine.getUnitCost())
        );

        assertEquals(
                0,
                new java.math.BigDecimal("144.0000")
                        .compareTo(savedLine.getTotalCost())
        );

        verify(goodsReceiptRepository, times(1))
                .save(any(
                        com.gestiva.purchasing.receipt.entity.GoodsReceipt.class
                ));

        verify(inventoryDocumentPostingService, times(1))
                .postPurchaseReceiptFromGoodsReceiptV2(
                        tenantId,
                        companyId,
                        receiptId
                );

        verifyNoInteractions(
                itemRepository,
                inventoryMovementRepository
        );
    }


    @Test
    void shouldRejectMissingUomSnapshot() {

        Long tenantId = 2L;
        Long companyId = 1L;
        Long purchaseOrderId = 100L;
        Long warehouseId = 1L;
        Long receiptId = 300L;

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

        order.setId(purchaseOrderId);
        order.setStatus("CONFIRMED");
        order.setSupplierId(10L);
        order.setOrderNumber("PO-TEST-MISSING-UOM");

        when(purchaseOrderRepository.findLockedByTenantIdAndCompanyIdAndId(
                tenantId,
                companyId,
                purchaseOrderId
        )).thenReturn(java.util.Optional.of(order));

        when(goodsReceiptRepository
                .existsByTenantIdAndCompanyIdAndPurchaseOrderId(
                        tenantId,
                        companyId,
                        purchaseOrderId
                )).thenReturn(false);

        com.gestiva.purchasing.order.entity.PurchaseOrderLine orderLine =
                new com.gestiva.purchasing.order.entity.PurchaseOrderLine();

        orderLine.setId(200L);
        orderLine.setPurchaseOrderId(purchaseOrderId);
        orderLine.setUomId(null);
        orderLine.setQuantity(new java.math.BigDecimal("2"));
        orderLine.setConversionFactor(
                new java.math.BigDecimal("10")
        );
        orderLine.setLineSubtotal(
                new java.math.BigDecimal("144.00")
        );

        when(purchaseOrderLineRepository
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        tenantId,
                        purchaseOrderId
                )).thenReturn(java.util.List.of(orderLine));

        when(documentSequenceService.nextNumber(
                tenantId,
                companyId,
                DocumentSequenceService.GOODS_RECEIPT
        )).thenReturn("GR-TEST-MISSING-UOM");

        when(goodsReceiptRepository.save(
                any(com.gestiva.purchasing.receipt.entity.GoodsReceipt.class)
        )).thenAnswer(invocation -> {

            com.gestiva.purchasing.receipt.entity.GoodsReceipt receipt =
                    invocation.getArgument(0);

            receipt.setId(receiptId);

            return receipt;
        });

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createFromPurchaseOrder(
                        tenantId,
                        purchaseOrderId,
                        warehouseId
                )
        );

        assertEquals(
                "Ricevimento non consentito: snapshot UOM "
                + "assente o incompleto nella riga ordine.",
                exception.getMessage()
        );

        verify(goodsReceiptRepository, times(1))
                .save(any(
                        com.gestiva.purchasing.receipt.entity.GoodsReceipt.class
                ));

        verifyNoInteractions(
                goodsReceiptLineRepository,
                itemRepository,
                inventoryDocumentPostingService,
                inventoryMovementRepository
        );
    }


    @Test
    void shouldRejectMissingUomConversionFactor() {

        Long tenantId = 2L;
        Long companyId = 1L;
        Long purchaseOrderId = 100L;
        Long warehouseId = 1L;
        Long receiptId = 300L;

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

        order.setId(purchaseOrderId);
        order.setStatus("CONFIRMED");
        order.setSupplierId(10L);
        order.setOrderNumber("PO-TEST-MISSING-FACTOR");

        when(purchaseOrderRepository.findLockedByTenantIdAndCompanyIdAndId(
                tenantId,
                companyId,
                purchaseOrderId
        )).thenReturn(java.util.Optional.of(order));

        when(goodsReceiptRepository
                .existsByTenantIdAndCompanyIdAndPurchaseOrderId(
                        tenantId,
                        companyId,
                        purchaseOrderId
                )).thenReturn(false);

        com.gestiva.purchasing.order.entity.PurchaseOrderLine orderLine =
                new com.gestiva.purchasing.order.entity.PurchaseOrderLine();

        orderLine.setId(200L);
        orderLine.setPurchaseOrderId(purchaseOrderId);
        orderLine.setUomId(5L);

        // Snapshot UOM incompleto:
        // il fattore di conversione è assente.
        orderLine.setConversionFactor(null);

        orderLine.setQuantity(
                new java.math.BigDecimal("2")
        );

        orderLine.setLineSubtotal(
                new java.math.BigDecimal("144.00")
        );

        when(purchaseOrderLineRepository
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        tenantId,
                        purchaseOrderId
                )).thenReturn(java.util.List.of(orderLine));

        when(documentSequenceService.nextNumber(
                tenantId,
                companyId,
                DocumentSequenceService.GOODS_RECEIPT
        )).thenReturn("GR-TEST-MISSING-FACTOR");

        when(goodsReceiptRepository.save(
                any(com.gestiva.purchasing.receipt.entity.GoodsReceipt.class)
        )).thenAnswer(invocation -> {

            com.gestiva.purchasing.receipt.entity.GoodsReceipt receipt =
                    invocation.getArgument(0);

            receipt.setId(receiptId);

            return receipt;
        });

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createFromPurchaseOrder(
                        tenantId,
                        purchaseOrderId,
                        warehouseId
                )
        );

        assertEquals(
                "Ricevimento non consentito: snapshot UOM "
                + "assente o incompleto nella riga ordine.",
                exception.getMessage()
        );

        verify(goodsReceiptRepository, times(1))
                .save(any(
                        com.gestiva.purchasing.receipt.entity.GoodsReceipt.class
                ));

        verifyNoInteractions(
                goodsReceiptLineRepository,
                itemRepository,
                inventoryDocumentPostingService,
                inventoryMovementRepository
        );
    }


    @Test
    void shouldRejectZeroUomConversionFactor() {

        Long tenantId = 2L;
        Long companyId = 1L;
        Long purchaseOrderId = 100L;
        Long warehouseId = 1L;
        Long receiptId = 300L;

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

        order.setId(purchaseOrderId);
        order.setStatus("CONFIRMED");
        order.setSupplierId(10L);
        order.setOrderNumber("PO-TEST-ZERO-FACTOR");

        when(purchaseOrderRepository.findLockedByTenantIdAndCompanyIdAndId(
                tenantId,
                companyId,
                purchaseOrderId
        )).thenReturn(java.util.Optional.of(order));

        when(goodsReceiptRepository
                .existsByTenantIdAndCompanyIdAndPurchaseOrderId(
                        tenantId,
                        companyId,
                        purchaseOrderId
                )).thenReturn(false);

        com.gestiva.purchasing.order.entity.PurchaseOrderLine orderLine =
                new com.gestiva.purchasing.order.entity.PurchaseOrderLine();

        orderLine.setId(200L);
        orderLine.setPurchaseOrderId(purchaseOrderId);
        orderLine.setUomId(5L);

        // Fattore di conversione non valido.
        orderLine.setConversionFactor(
                java.math.BigDecimal.ZERO
        );

        orderLine.setQuantity(
                new java.math.BigDecimal("2")
        );

        orderLine.setLineSubtotal(
                new java.math.BigDecimal("144.00")
        );

        when(purchaseOrderLineRepository
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        tenantId,
                        purchaseOrderId
                )).thenReturn(java.util.List.of(orderLine));

        when(documentSequenceService.nextNumber(
                tenantId,
                companyId,
                DocumentSequenceService.GOODS_RECEIPT
        )).thenReturn("GR-TEST-ZERO-FACTOR");

        when(goodsReceiptRepository.save(
                any(com.gestiva.purchasing.receipt.entity.GoodsReceipt.class)
        )).thenAnswer(invocation -> {

            com.gestiva.purchasing.receipt.entity.GoodsReceipt receipt =
                    invocation.getArgument(0);

            receipt.setId(receiptId);

            return receipt;
        });

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createFromPurchaseOrder(
                        tenantId,
                        purchaseOrderId,
                        warehouseId
                )
        );

        assertEquals(
                "Quantità, conversione o costo netto non validi "
                + "nella riga ordine.",
                exception.getMessage()
        );

        verify(goodsReceiptRepository, times(1))
                .save(any(
                        com.gestiva.purchasing.receipt.entity.GoodsReceipt.class
                ));

        verifyNoInteractions(
                goodsReceiptLineRepository,
                itemRepository,
                inventoryDocumentPostingService,
                inventoryMovementRepository
        );
    }


    @Test
    void shouldRejectNegativeUomConversionFactor() {

        Long tenantId = 2L;
        Long companyId = 1L;
        Long purchaseOrderId = 100L;
        Long warehouseId = 1L;
        Long receiptId = 300L;

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

        order.setId(purchaseOrderId);
        order.setStatus("CONFIRMED");
        order.setSupplierId(10L);
        order.setOrderNumber("PO-TEST-NEGATIVE-FACTOR");

        when(purchaseOrderRepository.findLockedByTenantIdAndCompanyIdAndId(
                tenantId,
                companyId,
                purchaseOrderId
        )).thenReturn(java.util.Optional.of(order));

        when(goodsReceiptRepository
                .existsByTenantIdAndCompanyIdAndPurchaseOrderId(
                        tenantId,
                        companyId,
                        purchaseOrderId
                )).thenReturn(false);

        com.gestiva.purchasing.order.entity.PurchaseOrderLine orderLine =
                new com.gestiva.purchasing.order.entity.PurchaseOrderLine();

        orderLine.setId(200L);
        orderLine.setPurchaseOrderId(purchaseOrderId);
        orderLine.setUomId(5L);

        // Fattore di conversione non valido.
        orderLine.setConversionFactor(
                new java.math.BigDecimal("-10")
        );

        orderLine.setQuantity(
                new java.math.BigDecimal("2")
        );

        orderLine.setLineSubtotal(
                new java.math.BigDecimal("144.00")
        );

        when(purchaseOrderLineRepository
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        tenantId,
                        purchaseOrderId
                )).thenReturn(java.util.List.of(orderLine));

        when(documentSequenceService.nextNumber(
                tenantId,
                companyId,
                DocumentSequenceService.GOODS_RECEIPT
        )).thenReturn("GR-TEST-NEGATIVE-FACTOR");

        when(goodsReceiptRepository.save(
                any(com.gestiva.purchasing.receipt.entity.GoodsReceipt.class)
        )).thenAnswer(invocation -> {

            com.gestiva.purchasing.receipt.entity.GoodsReceipt receipt =
                    invocation.getArgument(0);

            receipt.setId(receiptId);

            return receipt;
        });

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createFromPurchaseOrder(
                        tenantId,
                        purchaseOrderId,
                        warehouseId
                )
        );

        assertEquals(
                "Quantità, conversione o costo netto non validi "
                + "nella riga ordine.",
                exception.getMessage()
        );

        verify(goodsReceiptRepository, times(1))
                .save(any(
                        com.gestiva.purchasing.receipt.entity.GoodsReceipt.class
                ));

        verifyNoInteractions(
                goodsReceiptLineRepository,
                itemRepository,
                inventoryDocumentPostingService,
                inventoryMovementRepository
        );
    }



    @Test
    void shouldRejectZeroCommercialQuantity() {

        Long tenantId = 2L;
        Long companyId = 1L;
        Long purchaseOrderId = 100L;
        Long warehouseId = 1L;
        Long receiptId = 300L;

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

        order.setId(purchaseOrderId);
        order.setStatus("CONFIRMED");
        order.setSupplierId(10L);
        order.setOrderNumber("PO-TEST-ZERO-QUANTITY");

        when(purchaseOrderRepository.findLockedByTenantIdAndCompanyIdAndId(
                tenantId,
                companyId,
                purchaseOrderId
        )).thenReturn(java.util.Optional.of(order));

        when(goodsReceiptRepository
                .existsByTenantIdAndCompanyIdAndPurchaseOrderId(
                        tenantId,
                        companyId,
                        purchaseOrderId
                )).thenReturn(false);

        com.gestiva.purchasing.order.entity.PurchaseOrderLine orderLine =
                new com.gestiva.purchasing.order.entity.PurchaseOrderLine();

        orderLine.setId(200L);
        orderLine.setPurchaseOrderId(purchaseOrderId);
        orderLine.setUomId(5L);

        // Fattore di conversione non valido.
        orderLine.setConversionFactor(
                new java.math.BigDecimal("10")
        );

        orderLine.setQuantity(
                java.math.BigDecimal.ZERO
        );

        orderLine.setLineSubtotal(
                new java.math.BigDecimal("144.00")
        );

        when(purchaseOrderLineRepository
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        tenantId,
                        purchaseOrderId
                )).thenReturn(java.util.List.of(orderLine));

        when(documentSequenceService.nextNumber(
                tenantId,
                companyId,
                DocumentSequenceService.GOODS_RECEIPT
        )).thenReturn("GR-TEST-ZERO-QUANTITY");

        when(goodsReceiptRepository.save(
                any(com.gestiva.purchasing.receipt.entity.GoodsReceipt.class)
        )).thenAnswer(invocation -> {

            com.gestiva.purchasing.receipt.entity.GoodsReceipt receipt =
                    invocation.getArgument(0);

            receipt.setId(receiptId);

            return receipt;
        });

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createFromPurchaseOrder(
                        tenantId,
                        purchaseOrderId,
                        warehouseId
                )
        );

        assertEquals(
                "Quantità, conversione o costo netto non validi "
                + "nella riga ordine.",
                exception.getMessage()
        );

        verify(goodsReceiptRepository, times(1))
                .save(any(
                        com.gestiva.purchasing.receipt.entity.GoodsReceipt.class
                ));

        verifyNoInteractions(
                goodsReceiptLineRepository,
                itemRepository,
                inventoryDocumentPostingService,
                inventoryMovementRepository
        );
    }



    @Test
    void shouldRejectNegativeCommercialQuantity() {

        Long tenantId = 2L;
        Long companyId = 1L;
        Long purchaseOrderId = 100L;
        Long warehouseId = 1L;
        Long receiptId = 300L;

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

        order.setId(purchaseOrderId);
        order.setStatus("CONFIRMED");
        order.setSupplierId(10L);
        order.setOrderNumber("PO-TEST-NEGATIVE-QUANTITY");

        when(purchaseOrderRepository.findLockedByTenantIdAndCompanyIdAndId(
                tenantId,
                companyId,
                purchaseOrderId
        )).thenReturn(java.util.Optional.of(order));

        when(goodsReceiptRepository
                .existsByTenantIdAndCompanyIdAndPurchaseOrderId(
                        tenantId,
                        companyId,
                        purchaseOrderId
                )).thenReturn(false);

        com.gestiva.purchasing.order.entity.PurchaseOrderLine orderLine =
                new com.gestiva.purchasing.order.entity.PurchaseOrderLine();

        orderLine.setId(200L);
        orderLine.setPurchaseOrderId(purchaseOrderId);
        orderLine.setUomId(5L);

        // Fattore di conversione non valido.
        orderLine.setConversionFactor(
                new java.math.BigDecimal("10")
        );

        orderLine.setQuantity(
                new java.math.BigDecimal("-2")
        );

        orderLine.setLineSubtotal(
                new java.math.BigDecimal("144.00")
        );

        when(purchaseOrderLineRepository
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        tenantId,
                        purchaseOrderId
                )).thenReturn(java.util.List.of(orderLine));

        when(documentSequenceService.nextNumber(
                tenantId,
                companyId,
                DocumentSequenceService.GOODS_RECEIPT
        )).thenReturn("GR-TEST-NEGATIVE-QUANTITY");

        when(goodsReceiptRepository.save(
                any(com.gestiva.purchasing.receipt.entity.GoodsReceipt.class)
        )).thenAnswer(invocation -> {

            com.gestiva.purchasing.receipt.entity.GoodsReceipt receipt =
                    invocation.getArgument(0);

            receipt.setId(receiptId);

            return receipt;
        });

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createFromPurchaseOrder(
                        tenantId,
                        purchaseOrderId,
                        warehouseId
                )
        );

        assertEquals(
                "Quantità, conversione o costo netto non validi "
                + "nella riga ordine.",
                exception.getMessage()
        );

        verify(goodsReceiptRepository, times(1))
                .save(any(
                        com.gestiva.purchasing.receipt.entity.GoodsReceipt.class
                ));

        verifyNoInteractions(
                goodsReceiptLineRepository,
                itemRepository,
                inventoryDocumentPostingService,
                inventoryMovementRepository
        );
    }




    @Test
    void shouldRejectMissingNetSubtotal() {

        Long tenantId = 2L;
        Long companyId = 1L;
        Long purchaseOrderId = 100L;
        Long warehouseId = 1L;
        Long receiptId = 300L;

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

        order.setId(purchaseOrderId);
        order.setStatus("CONFIRMED");
        order.setSupplierId(10L);
        order.setOrderNumber("PO-TEST-MISSING-NET");

        when(purchaseOrderRepository.findLockedByTenantIdAndCompanyIdAndId(
                tenantId,
                companyId,
                purchaseOrderId
        )).thenReturn(java.util.Optional.of(order));

        when(goodsReceiptRepository
                .existsByTenantIdAndCompanyIdAndPurchaseOrderId(
                        tenantId,
                        companyId,
                        purchaseOrderId
                )).thenReturn(false);

        com.gestiva.purchasing.order.entity.PurchaseOrderLine orderLine =
                new com.gestiva.purchasing.order.entity.PurchaseOrderLine();

        orderLine.setId(200L);
        orderLine.setPurchaseOrderId(purchaseOrderId);
        orderLine.setUomId(5L);

        // Fattore di conversione non valido.
        orderLine.setConversionFactor(
                new java.math.BigDecimal("10")
        );

        orderLine.setQuantity(
                new java.math.BigDecimal("2")
        );

        orderLine.setLineSubtotal(
                null
        );

        when(purchaseOrderLineRepository
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        tenantId,
                        purchaseOrderId
                )).thenReturn(java.util.List.of(orderLine));

        when(documentSequenceService.nextNumber(
                tenantId,
                companyId,
                DocumentSequenceService.GOODS_RECEIPT
        )).thenReturn("GR-TEST-MISSING-NET");

        when(goodsReceiptRepository.save(
                any(com.gestiva.purchasing.receipt.entity.GoodsReceipt.class)
        )).thenAnswer(invocation -> {

            com.gestiva.purchasing.receipt.entity.GoodsReceipt receipt =
                    invocation.getArgument(0);

            receipt.setId(receiptId);

            return receipt;
        });

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createFromPurchaseOrder(
                        tenantId,
                        purchaseOrderId,
                        warehouseId
                )
        );

        assertEquals(
                "Quantità, conversione o costo netto non validi "
                + "nella riga ordine.",
                exception.getMessage()
        );

        verify(goodsReceiptRepository, times(1))
                .save(any(
                        com.gestiva.purchasing.receipt.entity.GoodsReceipt.class
                ));

        verifyNoInteractions(
                goodsReceiptLineRepository,
                itemRepository,
                inventoryDocumentPostingService,
                inventoryMovementRepository
        );
    }




    @Test
    void shouldRejectZeroNetSubtotal() {

        Long tenantId = 2L;
        Long companyId = 1L;
        Long purchaseOrderId = 100L;
        Long warehouseId = 1L;
        Long receiptId = 300L;

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

        order.setId(purchaseOrderId);
        order.setStatus("CONFIRMED");
        order.setSupplierId(10L);
        order.setOrderNumber("PO-TEST-ZERO-NET");

        when(purchaseOrderRepository.findLockedByTenantIdAndCompanyIdAndId(
                tenantId,
                companyId,
                purchaseOrderId
        )).thenReturn(java.util.Optional.of(order));

        when(goodsReceiptRepository
                .existsByTenantIdAndCompanyIdAndPurchaseOrderId(
                        tenantId,
                        companyId,
                        purchaseOrderId
                )).thenReturn(false);

        com.gestiva.purchasing.order.entity.PurchaseOrderLine orderLine =
                new com.gestiva.purchasing.order.entity.PurchaseOrderLine();

        orderLine.setId(200L);
        orderLine.setPurchaseOrderId(purchaseOrderId);
        orderLine.setUomId(5L);

        // Fattore di conversione non valido.
        orderLine.setConversionFactor(
                new java.math.BigDecimal("10")
        );

        orderLine.setQuantity(
                new java.math.BigDecimal("2")
        );

        orderLine.setLineSubtotal(java.math.BigDecimal.ZERO);

        when(purchaseOrderLineRepository
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        tenantId,
                        purchaseOrderId
                )).thenReturn(java.util.List.of(orderLine));

        when(documentSequenceService.nextNumber(
                tenantId,
                companyId,
                DocumentSequenceService.GOODS_RECEIPT
        )).thenReturn("GR-TEST-ZERO-NET");

        when(goodsReceiptRepository.save(
                any(com.gestiva.purchasing.receipt.entity.GoodsReceipt.class)
        )).thenAnswer(invocation -> {

            com.gestiva.purchasing.receipt.entity.GoodsReceipt receipt =
                    invocation.getArgument(0);

            receipt.setId(receiptId);

            return receipt;
        });

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createFromPurchaseOrder(
                        tenantId,
                        purchaseOrderId,
                        warehouseId
                )
        );

        assertEquals(
                "Quantità, conversione o costo netto non validi "
                + "nella riga ordine.",
                exception.getMessage()
        );

        verify(goodsReceiptRepository, times(1))
                .save(any(
                        com.gestiva.purchasing.receipt.entity.GoodsReceipt.class
                ));

        verifyNoInteractions(
                goodsReceiptLineRepository,
                itemRepository,
                inventoryDocumentPostingService,
                inventoryMovementRepository
        );
    }





    @Test
    void shouldRejectNegativeNetSubtotal() {

        Long tenantId = 2L;
        Long companyId = 1L;
        Long purchaseOrderId = 100L;
        Long warehouseId = 1L;
        Long receiptId = 300L;

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

        order.setId(purchaseOrderId);
        order.setStatus("CONFIRMED");
        order.setSupplierId(10L);
        order.setOrderNumber("PO-TEST-NEGATIVE-NET");

        when(purchaseOrderRepository.findLockedByTenantIdAndCompanyIdAndId(
                tenantId,
                companyId,
                purchaseOrderId
        )).thenReturn(java.util.Optional.of(order));

        when(goodsReceiptRepository
                .existsByTenantIdAndCompanyIdAndPurchaseOrderId(
                        tenantId,
                        companyId,
                        purchaseOrderId
                )).thenReturn(false);

        com.gestiva.purchasing.order.entity.PurchaseOrderLine orderLine =
                new com.gestiva.purchasing.order.entity.PurchaseOrderLine();

        orderLine.setId(200L);
        orderLine.setPurchaseOrderId(purchaseOrderId);
        orderLine.setUomId(5L);

        // Fattore di conversione non valido.
        orderLine.setConversionFactor(
                new java.math.BigDecimal("10")
        );

        orderLine.setQuantity(
                new java.math.BigDecimal("2")
        );

        orderLine.setLineSubtotal(new java.math.BigDecimal("-144.00"));

        when(purchaseOrderLineRepository
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        tenantId,
                        purchaseOrderId
                )).thenReturn(java.util.List.of(orderLine));

        when(documentSequenceService.nextNumber(
                tenantId,
                companyId,
                DocumentSequenceService.GOODS_RECEIPT
        )).thenReturn("GR-TEST-NEGATIVE-NET");

        when(goodsReceiptRepository.save(
                any(com.gestiva.purchasing.receipt.entity.GoodsReceipt.class)
        )).thenAnswer(invocation -> {

            com.gestiva.purchasing.receipt.entity.GoodsReceipt receipt =
                    invocation.getArgument(0);

            receipt.setId(receiptId);

            return receipt;
        });

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createFromPurchaseOrder(
                        tenantId,
                        purchaseOrderId,
                        warehouseId
                )
        );

        assertEquals(
                "Quantità, conversione o costo netto non validi "
                + "nella riga ordine.",
                exception.getMessage()
        );

        verify(goodsReceiptRepository, times(1))
                .save(any(
                        com.gestiva.purchasing.receipt.entity.GoodsReceipt.class
                ));

        verifyNoInteractions(
                goodsReceiptLineRepository,
                itemRepository,
                inventoryDocumentPostingService,
                inventoryMovementRepository
        );
    }






    @Test
    void shouldRejectInexactUomConversion() {

        Long tenantId = 2L;
        Long companyId = 1L;
        Long purchaseOrderId = 100L;
        Long warehouseId = 1L;
        Long receiptId = 300L;

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

        order.setId(purchaseOrderId);
        order.setStatus("CONFIRMED");
        order.setSupplierId(10L);
        order.setOrderNumber("PO-TEST-INEXACT-UOM");

        when(purchaseOrderRepository.findLockedByTenantIdAndCompanyIdAndId(
                tenantId,
                companyId,
                purchaseOrderId
        )).thenReturn(java.util.Optional.of(order));

        when(goodsReceiptRepository
                .existsByTenantIdAndCompanyIdAndPurchaseOrderId(
                        tenantId,
                        companyId,
                        purchaseOrderId
                )).thenReturn(false);

        com.gestiva.purchasing.order.entity.PurchaseOrderLine orderLine =
                new com.gestiva.purchasing.order.entity.PurchaseOrderLine();

        orderLine.setId(200L);
        orderLine.setPurchaseOrderId(purchaseOrderId);
        orderLine.setUomId(5L);

        // Fattore di conversione non valido.
        orderLine.setConversionFactor(new java.math.BigDecimal("1.00005"));

        orderLine.setQuantity(
                new java.math.BigDecimal("2")
        );

        orderLine.setLineSubtotal(new java.math.BigDecimal("144.00"));

        when(purchaseOrderLineRepository
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        tenantId,
                        purchaseOrderId
                )).thenReturn(java.util.List.of(orderLine));

        when(documentSequenceService.nextNumber(
                tenantId,
                companyId,
                DocumentSequenceService.GOODS_RECEIPT
        )).thenReturn("GR-TEST-INEXACT-UOM");

        when(goodsReceiptRepository.save(
                any(com.gestiva.purchasing.receipt.entity.GoodsReceipt.class)
        )).thenAnswer(invocation -> {

            com.gestiva.purchasing.receipt.entity.GoodsReceipt receipt =
                    invocation.getArgument(0);

            receipt.setId(receiptId);

            return receipt;
        });

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createFromPurchaseOrder(
                        tenantId,
                        purchaseOrderId,
                        warehouseId
                )
        );

        assertEquals(
                "La quantità commerciale non è convertibile "
                + "esattamente nell'unità base.",
                exception.getMessage()
        );

        verify(goodsReceiptRepository, times(1))
                .save(any(
                        com.gestiva.purchasing.receipt.entity.GoodsReceipt.class
                ));

        verifyNoInteractions(
                goodsReceiptLineRepository,
                itemRepository,
                inventoryDocumentPostingService,
                inventoryMovementRepository
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
