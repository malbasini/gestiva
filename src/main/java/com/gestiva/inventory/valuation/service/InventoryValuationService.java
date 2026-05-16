package com.gestiva.inventory.valuation.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.inventory.movement.entity.InventoryMovement;
import com.gestiva.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.inventory.valuation.entity.InventoryLayer;
import com.gestiva.inventory.valuation.entity.InventoryLayerConsumption;
import com.gestiva.inventory.valuation.model.InventoryValuationMethod;
import com.gestiva.inventory.valuation.model.InventoryValuationResult;
import com.gestiva.inventory.valuation.repository.InventoryLayerConsumptionRepository;
import com.gestiva.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.platform.tenant.entity.Tenant;
import com.gestiva.platform.tenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class InventoryValuationService {

    private final TenantRepository tenantRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryLayerRepository inventoryLayerRepository;
    private final InventoryLayerConsumptionRepository inventoryLayerConsumptionRepository;

    public InventoryValuationService(TenantRepository tenantRepository,
                                     InventoryMovementRepository inventoryMovementRepository,
                                     InventoryLayerRepository inventoryLayerRepository,
                                     InventoryLayerConsumptionRepository inventoryLayerConsumptionRepository) {
        this.tenantRepository = tenantRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.inventoryLayerRepository = inventoryLayerRepository;
        this.inventoryLayerConsumptionRepository = inventoryLayerConsumptionRepository;
    }

    public void applyInboundValuation(Long tenantId, Long movementId) {
        InventoryMovement movement = inventoryMovementRepository.findByTenantIdAndId(tenantId, movementId)
                .orElseThrow(() -> new BusinessException("Movimento di magazzino non trovato."));

        if (!isInbound(movement.getMovementType())) {
            throw new BusinessException("Il movimento non è di ingresso.");
        }

        if (movement.getUnitCost() == null || movement.getUnitCost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Il movimento di ingresso non ha un costo unitario valido.");
        }

        List<InventoryLayer> existingLayers = inventoryLayerRepository
                .findByTenantIdAndSourceMovementIdOrderByIdAsc(tenantId, movement.getId());

        if (!existingLayers.isEmpty()) {
            return;
        }

        InventoryLayer layer = new InventoryLayer();
        layer.setTenantId(tenantId);
        layer.setItemId(movement.getItemId());
        layer.setSourceMovementId(movement.getId());
        layer.setLayerDate(movement.getMovementDate());
        layer.setOriginalQty(scaleQty(movement.getQuantity()));
        layer.setRemainingQty(scaleQty(movement.getQuantity()));
        layer.setUnitCost(scaleCost(movement.getUnitCost()));
        layer.setClosed(false);

        inventoryLayerRepository.save(layer);

        BigDecimal totalCost = scaleCost(movement.getUnitCost().multiply(movement.getQuantity()));
        movement.setTotalCost(totalCost);
        inventoryMovementRepository.save(movement);
    }

    public InventoryValuationResult applyOutboundValuation(Long tenantId, Long movementId) {
        InventoryMovement movement = inventoryMovementRepository.findByTenantIdAndId(tenantId, movementId)
                .orElseThrow(() -> new BusinessException("Movimento di magazzino non trovato."));

        if (!isOutbound(movement.getMovementType())) {
            throw new BusinessException("Il movimento non è di uscita.");
        }

        List<InventoryLayerConsumption> existingConsumptions =
                inventoryLayerConsumptionRepository.findByTenantIdAndOutMovementIdOrderByIdAsc(tenantId, movement.getId());

        if (!existingConsumptions.isEmpty()) {
            return new InventoryValuationResult(
                    nvlCost(movement.getTotalCost()),
                    nvlCost(movement.getUnitCost())
            );
        }

        InventoryValuationMethod method = resolveValuationMethod(tenantId);

        List<InventoryLayer> candidateLayers = switch (method) {
            case FIFO -> inventoryLayerRepository
                    .findByTenantIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(tenantId, movement.getItemId());
            case LIFO -> inventoryLayerRepository
                    .findByTenantIdAndItemIdAndClosedFalseOrderByLayerDateDescIdDesc(tenantId, movement.getItemId());
            case AVERAGE -> throw new BusinessException("Metodo AVERAGE non ancora implementato.");
        };

        BigDecimal qtyToConsume = scaleQty(movement.getQuantity());
        BigDecimal remaining = qtyToConsume;
        BigDecimal totalCost = zeroCost();

        for (InventoryLayer layer : candidateLayers) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal available = scaleQty(layer.getRemainingQty());
            if (available.compareTo(BigDecimal.ZERO) <= 0) {
                layer.setClosed(true);
                inventoryLayerRepository.save(layer);
                continue;
            }

            BigDecimal consumedQty = available.min(remaining).setScale(3, RoundingMode.HALF_UP);
            BigDecimal unitCost = scaleCost(layer.getUnitCost());
            BigDecimal consumedTotalCost = scaleCost(consumedQty.multiply(unitCost));

            InventoryLayerConsumption consumption = new InventoryLayerConsumption();
            consumption.setTenantId(tenantId);
            consumption.setOutMovementId(movement.getId());
            consumption.setLayerId(layer.getId());
            consumption.setConsumedQty(consumedQty);
            consumption.setUnitCost(unitCost);
            consumption.setTotalCost(consumedTotalCost);
            inventoryLayerConsumptionRepository.save(consumption);

            BigDecimal newRemainingQty = scaleQty(layer.getRemainingQty().subtract(consumedQty));
            layer.setRemainingQty(newRemainingQty);
            layer.setClosed(newRemainingQty.compareTo(BigDecimal.ZERO) == 0);
            inventoryLayerRepository.save(layer);

            remaining = scaleQty(remaining.subtract(consumedQty));
            totalCost = scaleCost(totalCost.add(consumedTotalCost));
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Giacenza insufficiente per valorizzare lo scarico di magazzino.");
        }

        BigDecimal unitCost = qtyToConsume.compareTo(BigDecimal.ZERO) > 0
                ? scaleCost(totalCost.divide(qtyToConsume, 4, RoundingMode.HALF_UP))
                : zeroCost();

        movement.setTotalCost(totalCost);
        movement.setUnitCost(unitCost);
        inventoryMovementRepository.save(movement);

        return new InventoryValuationResult(totalCost, unitCost);
    }

    private InventoryValuationMethod resolveValuationMethod(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException("Tenant non trovato."));

        String value = String.valueOf(tenant.getInventoryValuationMethod());
        if (value == null || value.isBlank()) {
            return InventoryValuationMethod.FIFO;
        }

        try {
            return InventoryValuationMethod.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Metodo di valorizzazione non valido per il tenant: " + value);
        }
    }

    private boolean isInbound(String movementType) {
        return "IN".equalsIgnoreCase(movementType)
                || "ADJUSTMENT_IN".equalsIgnoreCase(movementType);
    }

    private boolean isOutbound(String movementType) {
        return "OUT".equalsIgnoreCase(movementType)
                || "ADJUSTMENT_OUT".equalsIgnoreCase(movementType);
    }

    private BigDecimal scaleQty(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP)
                : value.setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleCost(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP)
                : value.setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroCost() {
        return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal nvlCost(BigDecimal value) {
        return value == null ? zeroCost() : scaleCost(value);
    }
}