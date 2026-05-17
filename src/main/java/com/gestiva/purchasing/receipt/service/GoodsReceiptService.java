package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.inventory.movement.service.InventoryDocumentPostingService;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.inventory.item.repository.ItemRepository;
import com.gestiva.inventory.stock.entity.StockMovement;
import com.gestiva.inventory.stock.repository.StockMovementRepository;
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
    private final StockMovementRepository stockMovementRepository;
    private final InventoryDocumentPostingService inventoryDocumentPostingService;

    public GoodsReceiptService(PurchaseOrderRepository purchaseOrderRepository,
                               PurchaseOrderLineRepository purchaseOrderLineRepository,
                               GoodsReceiptRepository goodsReceiptRepository,
                               GoodsReceiptLineRepository goodsReceiptLineRepository,
                               ItemRepository itemRepository,
                               StockMovementRepository stockMovementRepository,
                               InventoryDocumentPostingService inventoryDocumentPostingService) {


        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderLineRepository = purchaseOrderLineRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.goodsReceiptLineRepository = goodsReceiptLineRepository;
        this.itemRepository = itemRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.inventoryDocumentPostingService = inventoryDocumentPostingService;
    }

    public Long createFromPurchaseOrder(Long tenantId, Long purchaseOrderId) {
        var po = purchaseOrderRepository.findByTenantIdAndId(tenantId, purchaseOrderId)
                .orElseThrow(() -> new BusinessException("Ordine fornitore non trovato."));

        if (!"CONFIRMED".equalsIgnoreCase(po.getStatus())) {
            throw new BusinessException("Solo gli ordini fornitore confermati possono essere ricevuti.");
        }

        if (goodsReceiptRepository.existsByTenantIdAndPurchaseOrderId(tenantId, purchaseOrderId)) {
            throw new BusinessException("Esiste già una ricezione merci per questo ordine.");
        }

        var lines = purchaseOrderLineRepository.findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(tenantId, purchaseOrderId);

        if (lines.isEmpty()) {
            throw new BusinessException("L'ordine fornitore non contiene righe.");
        }

        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setTenantId(tenantId);
        receipt.setReceiptNumber(nextReceiptNumber(tenantId));
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

            StockMovement movement = new StockMovement();
            movement.setTenantId(tenantId);
            movement.setItemId(item.getId());
            movement.setMovementDate(savedReceipt.getReceiptDate());
            movement.setDirection("IN");
            movement.setReasonCode("PURCHASE_RECEIPT");
            movement.setQuantity(poLine.getQuantity());
            movement.setNotes("Carico automatico da ricezione merci " + savedReceipt.getReceiptNumber());
            movement.setReferenceType("GOODS_RECEIPT");
            movement.setReferenceId(savedReceipt.getId());
            stockMovementRepository.save(movement);
        }
        inventoryDocumentPostingService.postPurchaseReceiptFromGoodsReceipt(tenantId, savedReceipt);
        return savedReceipt.getId();
    }

    private String nextReceiptNumber(Long tenantId) {
        long next = goodsReceiptRepository.count() + 1;
        String number = "GR-" + String.format("%05d", next);

        while (goodsReceiptRepository.existsByTenantIdAndReceiptNumber(tenantId, number)) {
            next++;
            number = "GR-" + String.format("%05d", next);
        }

        return number;
    }
}
