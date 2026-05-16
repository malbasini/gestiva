package com.gestiva.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.inventory.movement.web.InventoryAdjustmentForm;
import com.gestiva.inventory.valuation.service.InventoryValuationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryAdjustmentService {

    private final InventoryMovementService inventoryMovementService;
    private final InventoryValuationService inventoryValuationService;

    public InventoryAdjustmentService(InventoryMovementService inventoryMovementService,
                                      InventoryValuationService inventoryValuationService) {

        this.inventoryMovementService = inventoryMovementService;
        this.inventoryValuationService = inventoryValuationService;
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

        Long movementId = inventoryMovementService.registerMovement(
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
        if ("ADJUSTMENT_IN".equalsIgnoreCase(movementType)) {
            inventoryValuationService.applyInboundValuation(tenantId, movementId);
        } else {
            inventoryValuationService.applyOutboundValuation(tenantId, movementId);
        }
        return movementId;
    }
}