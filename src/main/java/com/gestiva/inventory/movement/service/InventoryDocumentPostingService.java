package com.gestiva.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.inventory.item.repository.ItemRepository;
import com.gestiva.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.logistics.ddt.repository.DeliveryNoteLineRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;
import com.gestiva.logistics.ddt.entity.DeliveryNote;
import com.gestiva.logistics.ddt.entity.DeliveryNoteLine;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional
public class InventoryDocumentPostingService {

    private final InventoryMovementService inventoryMovementService;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final ItemRepository itemRepository;
    private final GoodsReceiptLineRepository goodsReceiptLineRepository;
    private final DeliveryNoteLineRepository deliveryNoteLineRepository;

    public InventoryDocumentPostingService(InventoryMovementService inventoryMovementService,
                                           InventoryMovementRepository inventoryMovementRepository,
                                           ItemRepository itemRepository,
                                           GoodsReceiptLineRepository goodsReceiptLineRepository,
                                           DeliveryNoteLineRepository deliveryNoteLineRepository) {

        this.inventoryMovementService = inventoryMovementService;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.itemRepository = itemRepository;
        this.goodsReceiptLineRepository = goodsReceiptLineRepository;
        this.deliveryNoteLineRepository = deliveryNoteLineRepository;
    }

    public void postSalesDeliveryFromDeliveryNote(Long tenantId, DeliveryNote deliveryNote) {
        if (deliveryNote == null) {
            throw new BusinessException("DDT non valido.");
        }

        if (inventoryMovementRepository.existsByTenantIdAndReferenceTypeAndReferenceIdAndCausalCode(
                tenantId, "DELIVERY_NOTE", deliveryNote.getId(), "SALES_DELIVERY")) {
            return;
        }

        var lines = deliveryNoteLineRepository.findByTenantIdAndDeliveryNoteIdOrderByLineNoAsc(tenantId, deliveryNote.getId());

        if (lines.isEmpty()) {
            return;
        }

        for (DeliveryNoteLine line : lines) {
            if (line.getItemId() == null || isZeroOrNegative(line.getQuantity())) {
                continue;
            }

            var item = itemRepository.findByTenantIdAndId(tenantId, line.getItemId())
                    .orElseThrow(() -> new BusinessException("Articolo non trovato: " + line.getItemId()));

            if (!item.isTrackStock()) {
                continue;
            }

            inventoryMovementService.registerMovement(
                    tenantId,
                    line.getItemId(),
                    deliveryNote.getDdtDate(),
                    "OUT",
                    "SALES_DELIVERY",
                    line.getQuantity(),
                    null,
                    "DELIVERY_NOTE",
                    deliveryNote.getId(),
                    "Scarico da DDT " + deliveryNote.getDdtNumber()
            );
        }
    }

    public void postPurchaseReceiptFromGoodsReceipt(Long tenantId, GoodsReceipt goodsReceipt) {
        if (goodsReceipt == null) {
            throw new BusinessException("Ricezione merci non valida.");
        }

        var lines = goodsReceiptLineRepository.findByTenantIdAndGoodsReceiptIdOrderByLineNoAsc(tenantId, goodsReceipt.getId());

        if (lines.isEmpty()) {
            return;
        }

        for (GoodsReceiptLine line : lines) {
            if (line.getItemId() == null || line.getQuantityReceived() == null || line.getQuantityReceived().signum() <= 0) {
                continue;
            }

            var item = itemRepository.findByTenantIdAndId(tenantId, line.getItemId())
                    .orElseThrow(() -> new BusinessException("Articolo non trovato: " + line.getItemId()));

            if (!item.isTrackStock()) {
                continue;
            }

            inventoryMovementService.registerMovement(
                    tenantId,
                    line.getItemId(),
                    goodsReceipt.getReceiptDate(),
                    "IN",
                    "PURCHASE_RECEIPT",
                    line.getQuantityReceived(),
                    null,
                    "GOODS_RECEIPT",
                    goodsReceipt.getId(),
                    "Carico da ricezione merci " + goodsReceipt.getReceiptNumber()
            );
        }
    }

    public void reverseDocumentMovements(Long tenantId,
                                         String referenceType,
                                         Long referenceId,
                                         LocalDate reversalDate,
                                         String notes) {
        var movements = inventoryMovementRepository
                .findByTenantIdAndReferenceTypeAndReferenceIdOrderByIdAsc(tenantId, referenceType, referenceId);

        for (var movement : movements) {
            if (!movement.isReversed()) {
                inventoryMovementService.reverseMovement(
                        tenantId,
                        movement.getId(),
                        reversalDate,
                        notes
                );
            }
        }
    }

    private boolean isZeroOrNegative(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) <= 0;
    }
}