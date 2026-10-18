package com.gestiva.warehouse.inventory.valuation.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.movement.entity.InventoryMovement;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryAverageBalance;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayerConsumption;
import com.gestiva.warehouse.inventory.valuation.model.InventoryValuationMethod;
import com.gestiva.warehouse.inventory.valuation.model.InventoryValuationResult;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryAverageBalanceRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerConsumptionRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.security.tenant.entity.Tenant;
import com.gestiva.security.tenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
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
    private final InventoryAverageBalanceRepository inventoryAverageBalanceRepository;
    private final WarehouseRepository warehouseRepository;

    public InventoryValuationService(
            TenantRepository tenantRepository,
            InventoryMovementRepository inventoryMovementRepository,
            InventoryLayerRepository inventoryLayerRepository,
            InventoryLayerConsumptionRepository inventoryLayerConsumptionRepository,
            InventoryAverageBalanceRepository inventoryAverageBalanceRepository,
            WarehouseRepository warehouseRepository) {

        this.tenantRepository = tenantRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.inventoryLayerRepository = inventoryLayerRepository;
        this.inventoryLayerConsumptionRepository = inventoryLayerConsumptionRepository;
        this.inventoryAverageBalanceRepository = inventoryAverageBalanceRepository;
        this.warehouseRepository = warehouseRepository;
    }

    // Gestiva V2 - Recupero movimento con isolamento multi-società
    private InventoryMovement requireCompanyMovement(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long movementId) {

        if (tenantId == null || companyId == null
                || warehouseId == null || movementId == null) {
            throw new BusinessException(
                    "Tenant, società, magazzino e movimento sono obbligatori."
            );
        }

        InventoryMovement movement = inventoryMovementRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        movementId
                )
                .orElseThrow(() -> new BusinessException(
                        "Movimento di magazzino non trovato nella società indicata."
                ));

        if (!warehouseId.equals(movement.getWarehouseId())) {
            throw new BusinessException(
                    "Il movimento non appartiene al magazzino indicato."
            );
        }

        return movement;
    }

    // Gestiva V2 - Blocco transazionale del magazzino
    private void lockWarehouseForValuation(
            Long tenantId,
            Long companyId,
            Long warehouseId) {

        warehouseRepository
                .findByTenantAndCompanyAndIdForUpdate(
                        tenantId,
                        companyId,
                        warehouseId
                )
                .orElseThrow(() -> new BusinessException(
                        "Magazzino non trovato nella società indicata."
                ));
    }

    // Gestiva V2 - Valorizzazione FIFO dei movimenti di ingresso
    public void applyInboundValuation(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long movementId) {

        lockWarehouseForValuation(
                tenantId,
                companyId,
                warehouseId
        );

        InventoryMovement movement = requireCompanyMovement(
                tenantId,
                companyId,
                warehouseId,
                movementId
        );

        if (!isInbound(movement.getMovementType())) {
            throw new BusinessException(
                    "Il movimento non è di ingresso."
            );
        }

        if (movement.getUnitCost() == null
                || movement.getUnitCost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "Il movimento di ingresso non ha un costo unitario valido."
            );
        }

        if (movement.getQuantity() == null
                || movement.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "La quantità del movimento deve essere positiva."
            );
        }

        // Evita la creazione duplicata dello strato FIFO.
        List<InventoryLayer> existingLayers = inventoryLayerRepository
                .findByTenantIdAndCompanyIdAndSourceMovementIdOrderByIdAsc(
                        tenantId,
                        companyId,
                        movementId
                );

        if (!existingLayers.isEmpty()) {
            return;
        }

        InventoryLayer layer = new InventoryLayer();

        layer.setTenantId(tenantId);
        layer.setCompanyId(companyId);
        layer.setWarehouseId(warehouseId);
        layer.setLocationId(movement.getLocationId());

        layer.setItemId(movement.getItemId());
        layer.setSourceMovementId(movement.getId());
        layer.setLayerDate(movement.getMovementDate());

        layer.setOriginalQty(scaleQty(movement.getQuantity()));
        layer.setRemainingQty(scaleQty(movement.getQuantity()));
        layer.setUnitCost(scaleCost(movement.getUnitCost()));

        layer.setClosed(false);

        inventoryLayerRepository.save(layer);

        BigDecimal totalCost = scaleCost(
                movement.getUnitCost().multiply(movement.getQuantity())
        );

        movement.setTotalCost(totalCost);
        inventoryMovementRepository.save(movement);
    }

    // Gestiva V2 - Valorizzazione FIFO dei movimenti di uscita
    public InventoryValuationResult applyOutboundValuation(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long movementId) {

        lockWarehouseForValuation(
                tenantId,
                companyId,
                warehouseId
        );

        InventoryMovement movement = requireCompanyMovement(
                tenantId,
                companyId,
                warehouseId,
                movementId
        );

        if (!isOutbound(movement.getMovementType())) {
            throw new BusinessException(
                    "Il movimento non è di uscita."
            );
        }

        if (movement.getQuantity() == null
                || movement.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "La quantità del movimento deve essere positiva."
            );
        }

        // Verifica se il movimento è già stato valorizzato.
        List<InventoryLayerConsumption> existingConsumptions =
                inventoryLayerConsumptionRepository
                        .findByTenantIdAndCompanyIdAndWarehouseIdAndOutMovementIdOrderByIdAsc(
                                tenantId,
                                companyId,
                                warehouseId,
                                movementId
                        );

        if (!existingConsumptions.isEmpty()) {
            return new InventoryValuationResult(
                    nvlCost(movement.getTotalCost()),
                    nvlCost(movement.getUnitCost())
            );
        }

        // Recupera soltanto gli strati FIFO della società e del magazzino.
        List<InventoryLayer> candidateLayers = inventoryLayerRepository
                .findByTenantIdAndCompanyIdAndWarehouseIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
                        tenantId,
                        companyId,
                        warehouseId,
                        movement.getItemId()
                );

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

            BigDecimal consumedQty = available.min(remaining)
                    .setScale(3, RoundingMode.HALF_UP);

            BigDecimal unitCost = scaleCost(layer.getUnitCost());

            BigDecimal consumedTotalCost = scaleCost(
                    consumedQty.multiply(unitCost)
            );

            InventoryLayerConsumption consumption =
                    new InventoryLayerConsumption();

            consumption.setTenantId(tenantId);
            consumption.setCompanyId(companyId);
            consumption.setWarehouseId(warehouseId);

            consumption.setOutMovementId(movement.getId());
            consumption.setLayerId(layer.getId());

            consumption.setConsumedQty(consumedQty);
            consumption.setUnitCost(unitCost);
            consumption.setTotalCost(consumedTotalCost);

            inventoryLayerConsumptionRepository.save(consumption);

            BigDecimal newRemainingQty = scaleQty(
                    layer.getRemainingQty().subtract(consumedQty)
            );

            layer.setRemainingQty(newRemainingQty);
            layer.setClosed(
                    newRemainingQty.compareTo(BigDecimal.ZERO) == 0
            );

            inventoryLayerRepository.save(layer);

            remaining = scaleQty(
                    remaining.subtract(consumedQty)
            );

            totalCost = scaleCost(
                    totalCost.add(consumedTotalCost)
            );
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException(
                    "Strati FIFO insufficienti per valorizzare lo scarico nel magazzino selezionato."
            );
        }

        BigDecimal unitCost = scaleCost(
                totalCost.divide(
                        qtyToConsume,
                        4,
                        RoundingMode.HALF_UP
                )
        );

        movement.setTotalCost(totalCost);
        movement.setUnitCost(unitCost);

        inventoryMovementRepository.save(movement);

        return new InventoryValuationResult(
                totalCost,
                unitCost
        );
    }

    public InventoryValuationResult applyOutboundValuation(Long tenantId, Long movementId) {
        InventoryMovement movement = inventoryMovementRepository.findByTenantIdAndId(tenantId, movementId)
                .orElseThrow(() -> new BusinessException("Movimento di magazzino non trovato."));

        if (!isOutbound(movement.getMovementType())) {
            throw new BusinessException("Il movimento non è di uscita.");
        }

        InventoryValuationMethod method = resolveValuationMethod(tenantId);

        if (method == InventoryValuationMethod.AVERAGE) {
            return applyOutboundAverage(tenantId, movement);
        }

        List<InventoryLayerConsumption> existingConsumptions =
                inventoryLayerConsumptionRepository.findByTenantIdAndOutMovementIdOrderByIdAsc(tenantId, movement.getId());

        if (!existingConsumptions.isEmpty()) {
            return new InventoryValuationResult(
                    nvlCost(movement.getTotalCost()),
                    nvlCost(movement.getUnitCost())
            );
        }

        List<InventoryLayer> candidateLayers = switch (method) {
            case FIFO -> inventoryLayerRepository
                    .findByTenantIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(tenantId, movement.getItemId());
            case LIFO -> inventoryLayerRepository
                    .findByTenantIdAndItemIdAndClosedFalseOrderByLayerDateDescIdDesc(tenantId, movement.getItemId());
            case AVERAGE -> throw new BusinessException("Metodo AVERAGE non gestito in questo ramo.");
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

    private void applyInboundAverage(Long tenantId, InventoryMovement movement) {
        BigDecimal inQty = scaleQty(movement.getQuantity());
        BigDecimal inUnitCost = scaleCost(movement.getUnitCost());
        BigDecimal inTotalCost = scaleCost(inQty.multiply(inUnitCost));

        InventoryAverageBalance balance = inventoryAverageBalanceRepository
                .findByTenantIdAndItemId(tenantId, movement.getItemId())
                .orElseGet(() -> {
                    InventoryAverageBalance b = new InventoryAverageBalance();
                    b.setTenantId(tenantId);
                    b.setItemId(movement.getItemId());
                    b.setCurrentQty(zeroQty());
                    b.setCurrentTotalValue(zeroCost());
                    b.setCurrentAvgUnitCost(zeroCost());
                    return b;
                });

        BigDecimal oldQty = scaleQty(balance.getCurrentQty());
        BigDecimal oldTotalValue = scaleCost(balance.getCurrentTotalValue());

        BigDecimal newQty = scaleQty(oldQty.add(inQty));
        BigDecimal newTotalValue = scaleCost(oldTotalValue.add(inTotalCost));
        BigDecimal newAvgUnitCost = newQty.compareTo(BigDecimal.ZERO) > 0
                ? scaleCost(newTotalValue.divide(newQty, 4, RoundingMode.HALF_UP))
                : zeroCost();

        balance.setCurrentQty(newQty);
        balance.setCurrentTotalValue(newTotalValue);
        balance.setCurrentAvgUnitCost(newAvgUnitCost);
        inventoryAverageBalanceRepository.save(balance);

        movement.setTotalCost(inTotalCost);
        inventoryMovementRepository.save(movement);
    }

    private InventoryValuationResult applyOutboundAverage(Long tenantId, InventoryMovement movement) {
        if (movement.getTotalCost() != null && movement.getUnitCost() != null) {
            return new InventoryValuationResult(
                    nvlCost(movement.getTotalCost()),
                    nvlCost(movement.getUnitCost())
            );
        }

        InventoryAverageBalance balance = inventoryAverageBalanceRepository
                .findByTenantIdAndItemId(tenantId, movement.getItemId())
                .orElseThrow(() -> new BusinessException("Saldo medio non trovato per l'articolo."));

        BigDecimal oldQty = scaleQty(balance.getCurrentQty());
        BigDecimal oldTotalValue = scaleCost(balance.getCurrentTotalValue());
        BigDecimal oldAvgUnitCost = scaleCost(balance.getCurrentAvgUnitCost());

        BigDecimal outQty = scaleQty(movement.getQuantity());

        if (outQty.compareTo(oldQty) > 0) {
            throw new BusinessException("Giacenza insufficiente per valorizzare lo scarico di magazzino.");
        }

        BigDecimal outUnitCost = oldAvgUnitCost;
        BigDecimal outTotalCost = scaleCost(outQty.multiply(outUnitCost));

        BigDecimal newQty = scaleQty(oldQty.subtract(outQty));
        BigDecimal newTotalValue = scaleCost(oldTotalValue.subtract(outTotalCost));

        if (newTotalValue.compareTo(BigDecimal.ZERO) < 0) {
            newTotalValue = zeroCost();
        }

        BigDecimal newAvgUnitCost = newQty.compareTo(BigDecimal.ZERO) > 0
                ? scaleCost(newTotalValue.divide(newQty, 4, RoundingMode.HALF_UP))
                : zeroCost();

        balance.setCurrentQty(newQty);
        balance.setCurrentTotalValue(newTotalValue);
        balance.setCurrentAvgUnitCost(newAvgUnitCost);
        inventoryAverageBalanceRepository.save(balance);

        movement.setUnitCost(outUnitCost);
        movement.setTotalCost(outTotalCost);
        inventoryMovementRepository.save(movement);

        return new InventoryValuationResult(outTotalCost, outUnitCost);
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

        InventoryValuationMethod method = resolveValuationMethod(tenantId);

        if (method == InventoryValuationMethod.AVERAGE) {
            applyInboundAverage(tenantId, movement);
            return;
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
    private BigDecimal zeroQty() {
        return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
    }
}