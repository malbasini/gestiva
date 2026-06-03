package com.gestiva.inventory.item.web;

import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.inventory.movement.repository.InventoryMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemMovementWebService {

    private final InventoryMovementRepository inventoryMovementRepository;

    public ItemMovementWebService(InventoryMovementRepository inventoryMovementRepository) {
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    public List<RecentInventoryMovementView> findRecentMovements(Long tenantId, Long itemId) {
        return inventoryMovementRepository
                .findTop10ByTenantIdAndItemIdOrderByMovementDateDescIdDesc(tenantId, itemId)
                .stream()
                .map(m -> {
                    RecentInventoryMovementView v = new RecentInventoryMovementView();
                    v.setFormattedMovementDate(PdfFormatUtils.formatDate(m.getMovementDate()));
                    v.setMovementType(m.getMovementType());
                    v.setMovementTypeLabel(toMovementTypeLabel(m.getMovementType()));
                    v.setCausalCode(m.getCausalCode());
                    v.setCausalCodeLabel(toCausalCodeLabel(m.getCausalCode()));
                    v.setFormattedQuantity(PdfFormatUtils.formatDecimalTrimmed(m.getQuantity(),0));
                    v.setReferenceType(m.getReferenceType());
                    v.setNotes(m.getNotes());
                    v.setInbound(isInbound(m.getMovementType()));
                    return v;
                })
                .toList();
    }

    private boolean isInbound(String movementType) {
        if (movementType == null) return false;
        return "IN".equalsIgnoreCase(movementType)
                || "ADJUSTMENT_IN".equalsIgnoreCase(movementType);
    }

    private String toMovementTypeLabel(String movementType) {
        if (movementType == null) return "";
        return switch (movementType.toUpperCase()) {
            case "IN" -> "Carico";
            case "OUT" -> "Scarico";
            case "ADJUSTMENT_IN" -> "Rettifica +";
            case "ADJUSTMENT_OUT" -> "Rettifica -";
            default -> movementType;
        };
    }

    private String toCausalCodeLabel(String causalCode) {
        if (causalCode == null) return "";
        return switch (causalCode.toUpperCase()) {
            case "PURCHASE_RECEIPT" -> "Ricezione acquisto";
            case "SALES_DELIVERY" -> "Consegna cliente";
            case "MANUAL_ADJUSTMENT_IN" -> "Rettifica inventario in aumento";
            case "MANUAL_ADJUSTMENT_OUT" -> "Rettifica inventario in diminuzione";
            case "DOCUMENT_CANCEL_RESTORE" -> "Ripristino da annullamento documento";
            default -> causalCode;
        };
    }
}