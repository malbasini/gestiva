package com.gestiva.inventory.movement.service;

import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.inventory.item.repository.ItemRepository;
import com.gestiva.inventory.movement.entity.InventoryMovement;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.inventory.valuation.repository.InventoryMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@Transactional
public class InventoryMovementService {

    private final InventoryMovementRepository inventoryMovementRepository;
    private final ItemRepository itemRepository;

    public InventoryMovementService(InventoryMovementRepository inventoryMovementRepository,
                                    ItemRepository itemRepository) {
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.itemRepository = itemRepository;
    }

    public Long registerMovement(Long tenantId,
                                 Long itemId,
                                 LocalDate movementDate,
                                 String movementType,
                                 String causalCode,
                                 BigDecimal quantity,
                                 BigDecimal unitCost,
                                 String referenceType,
                                 Long referenceId,
                                 String notes) {

        var item = itemRepository.findByTenantIdAndId(tenantId, itemId)
                .orElseThrow(() -> new BusinessException("Articolo non trovato"));

        if (!item.isTrackStock()) {
            throw new BusinessException("L'articolo non è gestito a magazzino.");
        }

        BigDecimal qty = scaleQty(quantity);
        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("La quantità deve essere maggiore di zero.");
        }

        BigDecimal cost = unitCost == null ? null : scaleCost(unitCost);
        BigDecimal totalCost = cost != null
                ? scaleCost(cost.multiply(qty))
                : null;

        InventoryMovement movement = new InventoryMovement();
        movement.setTenantId(tenantId);
        movement.setItemId(itemId);
        movement.setMovementDate(movementDate);
        movement.setMovementType(movementType);
        movement.setCausalCode(causalCode);
        movement.setQuantity(qty);
        movement.setUnitCost((cost));
        movement.setTotalCost(totalCost);
        movement.setReferenceType(referenceType);
        movement.setReferenceId(referenceId);
        movement.setNotes(notes);
        movement.setReversed(false);
        movement.setReversalOfMovementId(null);

        return inventoryMovementRepository.save(movement).getId();
    }

    public Long reverseMovement(Long tenantId, Long movementId, LocalDate reversalDate, String notes) {
        var original = inventoryMovementRepository.findByTenantIdAndId(tenantId, movementId)
                .orElseThrow(() -> new BusinessException("Movimento di magazzino non trovato"));

        if (original.isReversed()) {
            throw new BusinessException("Il movimento è già stato stornato.");
        }

        InventoryMovement reversal = new InventoryMovement();
        reversal.setTenantId(tenantId);
        reversal.setItemId(original.getItemId());
        reversal.setMovementDate(reversalDate);
        reversal.setMovementType(reverseType(original.getMovementType()));
        reversal.setCausalCode("DOCUMENT_CANCEL_RESTORE");
        reversal.setQuantity(original.getQuantity());
        reversal.setUnitCost(original.getUnitCost());
        reversal.setTotalCost(original.getTotalCost());
        reversal.setReferenceType(original.getReferenceType());
        reversal.setReferenceId(original.getReferenceId());
        reversal.setNotes(notes);
        reversal.setReversed(false);
        reversal.setReversalOfMovementId(original.getId());

        InventoryMovement savedReversal = inventoryMovementRepository.save(reversal);

        original.setReversed(true);
        inventoryMovementRepository.save(original);

        return savedReversal.getId();
    }

    private String reverseType(String movementType) {
        return switch (movementType) {
            case "IN" -> "OUT";
            case "OUT" -> "IN";
            case "ADJUSTMENT_IN" -> "ADJUSTMENT_OUT";
            case "ADJUSTMENT_OUT" -> "ADJUSTMENT_IN";
            default -> throw new BusinessException("Tipo movimento non reversibile: " + movementType);
        };
    }

    private BigDecimal scaleQty(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP)
                : value.setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleCost(BigDecimal value) {
        return value == null ? null : value.setScale(4, RoundingMode.HALF_UP);
    }
}