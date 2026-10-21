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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    public GoodsReceiptService(PurchaseOrderRepository purchaseOrderRepository,
                               PurchaseOrderLineRepository purchaseOrderLineRepository,
                               GoodsReceiptRepository goodsReceiptRepository,
                               GoodsReceiptLineRepository goodsReceiptLineRepository,
                               ItemRepository itemRepository,
                               InventoryDocumentPostingService inventoryDocumentPostingService,
                               InventoryMovementRepository inventoryMovementRepository,
                               DocumentSequenceService documentSequenceService,
                               CompanyContext companyContext,
                               WarehouseRepository warehouseRepository) {


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
    }

    public Long createFromPurchaseOrder(
            Long tenantId,
            Long purchaseOrderId,
            Long warehouseId) {

        // Gestiva V2 - Blocco temporaneo della ricezione merci.
        // La funzionalità sarà riattivata dopo l'adeguamento
        // multi-società, multi-magazzino e FIFO V2.
        if (true) {
            throw new BusinessException(
                    "Ricezione merci temporaneamente non disponibile: migrazione Gestiva V2 in corso."
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

        var po = purchaseOrderRepository.findByTenantIdAndCompanyIdAndId(
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
            receiptLine.setQuantityReceived(poLine.getQuantity());
            receiptLine.setUnitCost(poLine.getUnitPrice());
            receiptLine.setTotalCost(poLine.getUnitPrice().multiply(poLine.getQuantity()));
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
