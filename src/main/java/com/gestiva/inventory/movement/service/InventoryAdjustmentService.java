package com.gestiva.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.inventory.movement.web.InventoryAdjustmentForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryAdjustmentService {

    private final InventoryMovementService inventoryMovementService;

    public InventoryAdjustmentService(InventoryMovementService inventoryMovementService) {
        this.inventoryMovementService = inventoryMovementService;
    }

    public Long registerAdjustment(Long tenantId, InventoryAdjustmentForm form) {
        if (form.getAdjustmentType() == null) {
            throw new BusinessException("Tipo rettifica non valido.");
        }

        String movementType;
        String causalCode;

        if ("IN".equalsIgnoreCase(form.getAdjustmentType())) {
            movementType = "ADJUSTMENT_IN";
            causalCode = "MANUAL_ADJUSTMENT_IN";
        } else if ("OUT".equalsIgnoreCase(form.getAdjustmentType())) {
            movementType = "ADJUSTMENT_OUT";
            causalCode = "MANUAL_ADJUSTMENT_OUT";
        } else {
            throw new BusinessException("Tipo rettifica non valido.");
        }

        return inventoryMovementService.registerMovement(
                tenantId,
                form.getItemId(),
                form.getMovementDate(),
                movementType,
                causalCode,
                form.getQuantity(),
                null,
                "MANUAL_INVENTORY",
                null,
                form.getNotes()
        );
    }
}