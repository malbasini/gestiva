package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.util.NumberInputUtils;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.inventory.movement.web.InventoryAdjustmentForm;
import com.gestiva.warehouse.inventory.valuation.service.InventoryAvailabilityService;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@Transactional
public class InventoryAdjustmentService {

    private final InventoryMovementService inventoryMovementService;
    private final InventoryValuationService inventoryValuationService;
    private final InventoryAvailabilityService inventoryAvailabilityService;
    private final CompanyContext companyContext;

    public InventoryAdjustmentService(
            InventoryMovementService inventoryMovementService,
            InventoryValuationService inventoryValuationService,
            InventoryAvailabilityService inventoryAvailabilityService,
            CompanyContext companyContext) {

        this.inventoryMovementService = inventoryMovementService;
        this.inventoryValuationService = inventoryValuationService;
        this.inventoryAvailabilityService = inventoryAvailabilityService;
        this.companyContext = companyContext;
    }

    // Gestiva V2 - Rettifica magazzino multi-società con FIFO
    public Long registerAdjustment(
            Long tenantId,
            InventoryAdjustmentForm form) {

        if (tenantId == null) {
            throw new BusinessException("Tenant non valido.");
        }

        if (form == null) {
            throw new BusinessException("Dati rettifica mancanti.");
        }

        Long companyId = companyContext.requireCurrentCompanyId();

        Long warehouseId = form.getWarehouseId();
        Long itemId = form.getItemId();

        // Gestiva V2: le rettifiche manuali operano
        // attualmente a livello di magazzino.
        // Le ubicazioni saranno gestite quando il FIFO
        // supporterà la valorizzazione per ubicazione.
        if (form.getLocationId() != null) {
            throw new BusinessException(
                    "Le rettifiche per ubicazione non sono ancora supportate."
            );
        }

        Long locationId = null;

        if (warehouseId == null) {
            throw new BusinessException(
                    "Seleziona un magazzino."
            );
        }

        if (itemId == null) {
            throw new BusinessException(
                    "Seleziona un articolo."
            );
        }

        if (form.getMovementDate() == null) {
            throw new BusinessException(
                    "La data del movimento è obbligatoria."
            );
        }

        if (form.getAdjustmentType() == null) {
            throw new BusinessException(
                    "Tipo rettifica non valido."
            );
        }

        BigDecimal quantity = NumberInputUtils.parseDecimal(
                form.getQuantity(),
                "la quantità"
        );

        if (quantity.signum() <= 0) {
            throw new BusinessException(
                    "La quantità deve essere maggiore di zero."
            );
        }

        String movementType;
        String causalCode;
        BigDecimal unitCost = null;

        if ("IN".equalsIgnoreCase(form.getAdjustmentType())) {

            movementType = "ADJUSTMENT_IN";
            causalCode = "MANUAL_ADJUSTMENT_IN";

            unitCost = NumberInputUtils.parseDecimal(
                    form.getUnitCost(),
                    "il costo unitario"
            );

            if (unitCost.signum() <= 0) {
                throw new BusinessException(
                        "Il costo unitario deve essere maggiore di zero."
                );
            }

        } else if ("OUT".equalsIgnoreCase(form.getAdjustmentType())) {

            movementType = "ADJUSTMENT_OUT";
            causalCode = "MANUAL_ADJUSTMENT_OUT";

            inventoryAvailabilityService
                    .validateAvailabilityByCompanyAndWarehouse(
                            tenantId,
                            companyId,
                            warehouseId,
                            itemId,
                            locationId,
                            quantity
                    );

        } else {

            throw new BusinessException(
                    "Tipo rettifica non valido."
            );
        }

        // Registrazione movimento V2 con società, magazzino e ubicazione.
        Long movementId = inventoryMovementService.registerMovement(
                tenantId,
                companyId,
                warehouseId,
                locationId,
                itemId,
                form.getMovementDate(),
                movementType,
                causalCode,
                quantity,
                unitCost,
                "MANUAL_INVENTORY",
                null,
                form.getNotes()
        );

        // Valorizzazione FIFO V2 nella stessa transazione.
        if ("ADJUSTMENT_IN".equals(movementType)) {

            inventoryValuationService.applyInboundValuation(
                    tenantId,
                    companyId,
                    warehouseId,
                    movementId
            );

        } else {

            inventoryValuationService.applyOutboundValuation(
                    tenantId,
                    companyId,
                    warehouseId,
                    movementId
            );
        }

        return movementId;
    }
}