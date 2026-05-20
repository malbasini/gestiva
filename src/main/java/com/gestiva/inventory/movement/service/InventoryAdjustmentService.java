package com.gestiva.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.util.NumberInputUtils;
import com.gestiva.inventory.movement.web.InventoryAdjustmentForm;
import com.gestiva.inventory.valuation.service.InventoryAvailabilityService;
import com.gestiva.inventory.valuation.service.InventoryValuationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class InventoryAdjustmentService {

    private final InventoryMovementService inventoryMovementService;
    private final InventoryValuationService inventoryValuationService;
    private final InventoryAvailabilityService inventoryAvailabilityService;

    public InventoryAdjustmentService(InventoryMovementService inventoryMovementService,
                                      InventoryValuationService inventoryValuationService,
                                      InventoryAvailabilityService inventoryAvailabilityService) {
        this.inventoryMovementService = inventoryMovementService;
        this.inventoryValuationService = inventoryValuationService;
        this.inventoryAvailabilityService = inventoryAvailabilityService;
    }

    public Long registerAdjustment(Long tenantId, InventoryAdjustmentForm form) {
        if (form.getAdjustmentType() == null) {
            throw new BusinessException("Tipo rettifica non valido.");
        }

        BigDecimal quantity = NumberInputUtils.parseDecimal(form.getQuantity(), "la quantità");
        if (quantity.signum() <= 0) {
            throw new BusinessException("La quantità deve essere maggiore di zero.");
        }

        String movementType;
        String causalCode;
        BigDecimal unitCost = null;

        if ("IN".equalsIgnoreCase(form.getAdjustmentType())) {
            movementType = "ADJUSTMENT_IN";
            causalCode = "MANUAL_ADJUSTMENT_IN";

            unitCost = NumberInputUtils.parseDecimal(form.getUnitCost(), "il costo unitario");
            if (unitCost.signum() <= 0) {
                throw new BusinessException("Il costo unitario deve essere maggiore di zero.");
            }

        } else if ("OUT".equalsIgnoreCase(form.getAdjustmentType())) {
            movementType = "ADJUSTMENT_OUT";
            causalCode = "MANUAL_ADJUSTMENT_OUT";

            inventoryAvailabilityService.validateAvailability(
                    tenantId,
                    form.getItemId(),
                    quantity
            );
        } else {
            throw new BusinessException("Tipo rettifica non valido.");
        }

        Long movementId = inventoryMovementService.registerMovement(
                tenantId,
                form.getItemId(),
                form.getMovementDate(),
                movementType,
                causalCode,
                quantity,
                unitCost,
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