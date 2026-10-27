package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.movement.service.InventoryDocumentPostingService;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@Transactional
public class GoodsReceiptService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final GoodsReceiptLineRepository goodsReceiptLineRepository;
    private final ItemRepository itemRepository;
    private final InventoryDocumentPostingService inventoryDocumentPostingService;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final DocumentSequenceService documentSequenceService;
    private final CompanyContext companyContext;
    private final WarehouseRepository warehouseRepository;
    private final Environment environment;


    public GoodsReceiptService(PurchaseOrderRepository purchaseOrderRepository,
                               PurchaseOrderLineRepository purchaseOrderLineRepository,
                               GoodsReceiptRepository goodsReceiptRepository,
                               GoodsReceiptLineRepository goodsReceiptLineRepository,
                               ItemRepository itemRepository,
                               InventoryDocumentPostingService inventoryDocumentPostingService,
                               InventoryMovementRepository inventoryMovementRepository,
                               DocumentSequenceService documentSequenceService,
                               CompanyContext companyContext,
                               WarehouseRepository warehouseRepository,
                               Environment environment) {


        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderLineRepository = purchaseOrderLineRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.goodsReceiptLineRepository = goodsReceiptLineRepository;
        this.itemRepository = itemRepository;
        this.inventoryDocumentPostingService = inventoryDocumentPostingService;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.documentSequenceService = documentSequenceService;
        this.companyContext = companyContext;
        this.warehouseRepository = warehouseRepository;
        this.environment = environment;
    }

    public Long createFromPurchaseOrder(
            Long tenantId,
            Long purchaseOrderId,
            Long warehouseId) {

        // Ricezione merci abilitabile esclusivamente nei collaudi.
        // In tutti gli altri ambienti resta disabilitata.
        boolean testProfileActive = java.util.Arrays.asList(
                environment.getActiveProfiles()
        ).contains("test");

        boolean receiptEnabled = environment.getProperty(
                "gestiva.purchasing.goods-receipt.enabled",
                Boolean.class,
                false
        );

        if (!testProfileActive || !receiptEnabled) {
            throw new BusinessException(
                    "Ricezione merci temporaneamente non disponibile."
            );
        }


        Long companyId = companyContext.requireCurrentCompanyId();

        if (warehouseId == null) {
            throw new BusinessException(
                    "Seleziona un magazzino di destinazione."
            );
        }

        var warehouse = warehouseRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        warehouseId
                )
                .orElseThrow(() -> new BusinessException(
                        "Magazzino non trovato nella società corrente."
                ));

        if (!warehouse.isActive()) {
            throw new BusinessException(
                    "Il magazzino selezionato non è attivo."
            );
        }

        var po = purchaseOrderRepository.findLockedByTenantIdAndCompanyIdAndId(
                tenantId,
                companyId,
                purchaseOrderId
        )
                .orElseThrow(() -> new BusinessException("Ordine fornitore non trovato."));

        if (!"CONFIRMED".equalsIgnoreCase(po.getStatus())) {
            throw new BusinessException("Solo gli ordini fornitore confermati possono essere ricevuti.");
        }

        if (goodsReceiptRepository.existsByTenantIdAndCompanyIdAndPurchaseOrderId(
                tenantId,
                companyId,
                purchaseOrderId
        )) {
            throw new BusinessException("Esiste già una ricezione merci per questo ordine.");
        }

        var lines = purchaseOrderLineRepository.findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(tenantId, purchaseOrderId);

        if (lines.isEmpty()) {
            throw new BusinessException("L'ordine fornitore non contiene righe.");
        }

        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setTenantId(tenantId);
        receipt.setCompanyId(companyId);
        receipt.setWarehouseId(warehouseId);
        receipt.setLocationId(null);
        receipt.setReceiptNumber(nextReceiptNumber(tenantId, companyId));
        receipt.setReceiptDate(LocalDate.now());
        receipt.setPurchaseOrderId(po.getId());
        receipt.setSupplierId(po.getSupplierId());
        receipt.setNotes("Ricezione automatica da ordine fornitore " + po.getOrderNumber());
        GoodsReceipt savedReceipt = goodsReceiptRepository.save(receipt);
        int lineNo = 1;
        for (var poLine : lines) {
            GoodsReceiptLine receiptLine = new GoodsReceiptLine();
            receiptLine.setTenantId(tenantId);
            receiptLine.setGoodsReceiptId(savedReceipt.getId());
            receiptLine.setLineNo(lineNo++);
            receiptLine.setPurchaseOrderLineId(poLine.getId());
            receiptLine.setItemId(poLine.getItemId());
            receiptLine.setDescription(poLine.getDescription());
            // Gestiva V2 - Conversione UOM e costo netto FIFO.
            // Utilizziamo esclusivamente lo snapshot storico dell'ordine.
            BigDecimal commercialQty = poLine.getQuantity();
            BigDecimal conversionFactor = poLine.getConversionFactor();
            BigDecimal netSubtotal = poLine.getLineSubtotal();

            if (poLine.getUomId() == null || conversionFactor == null) {
                throw new BusinessException(
                        "Ricevimento non consentito: snapshot UOM "
                        + "assente o incompleto nella riga ordine."
                );
            }

            if (commercialQty == null
                    || commercialQty.compareTo(BigDecimal.ZERO) <= 0
                    || conversionFactor.compareTo(BigDecimal.ZERO) <= 0
                    || netSubtotal == null
                    || netSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(
                        "Quantità, conversione o costo netto non validi "
                        + "nella riga ordine."
                );
            }

            BigDecimal baseQty;
            try {
                baseQty = commercialQty.multiply(conversionFactor)
                        .setScale(3, RoundingMode.UNNECESSARY);
            } catch (ArithmeticException ex) {
                throw new BusinessException(
                        "La quantità commerciale non è convertibile "
                        + "esattamente nell'unità base."
                );
            }

            if (baseQty.precision() - baseQty.scale() > 12) {
                throw new BusinessException(
                        "La quantità base supera la capacità del magazzino."
                );
            }

            BigDecimal unitCost = netSubtotal.divide(
                    baseQty, 4, RoundingMode.HALF_UP
            );

            if (unitCost.compareTo(BigDecimal.ZERO) <= 0
                    || unitCost.precision() - unitCost.scale() > 11) {
                throw new BusinessException(
                        "Costo unitario FIFO non rappresentabile."
                );
            }

            BigDecimal totalCost = netSubtotal.setScale(
                    4, RoundingMode.UNNECESSARY
            );

            if (totalCost.precision() - totalCost.scale() > 11) {
                throw new BusinessException(
                        "Costo totale netto non rappresentabile."
                );
            }

            // Il movimento FIFO ricalcola il totale come
            // quantità base × costo unitario arrotondato.
            // Verifichiamo anche la capacità di DECIMAL(15,4).
            BigDecimal movementTotalCost = baseQty.multiply(unitCost)
                    .setScale(4, RoundingMode.HALF_UP);

            if (movementTotalCost.precision()
                    - movementTotalCost.scale() > 11) {
                throw new BusinessException(
                        "Costo totale del movimento FIFO "
                        + "non rappresentabile."
                );
            }

            receiptLine.setQuantityReceived(baseQty);
            receiptLine.setUnitCost(unitCost);
            receiptLine.setTotalCost(totalCost);
            goodsReceiptLineRepository.save(receiptLine);
            if (poLine.getItemId() == null) {
                continue;
            }

            var item = itemRepository.findByTenantIdAndId(tenantId, poLine.getItemId()).orElse(null);
            if (item == null || !item.isTrackStock()) {
                continue;
            }
        }
        // Gestiva V2 - Carico merci multi-società e valorizzazione FIFO.
        inventoryDocumentPostingService.postPurchaseReceiptFromGoodsReceiptV2(
                tenantId,
                companyId,
                savedReceipt.getId()
        );
        return savedReceipt.getId();
    }

    private String nextReceiptNumber(Long tenantId, Long companyId) {
        return documentSequenceService.nextNumber(
                tenantId,
                companyId,
                DocumentSequenceService.GOODS_RECEIPT
        );
    }
}
