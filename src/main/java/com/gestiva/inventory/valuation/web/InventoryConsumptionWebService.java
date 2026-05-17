package com.gestiva.inventory.valuation.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.inventory.item.repository.ItemRepository;
import com.gestiva.inventory.valuation.repository.InventoryLayerConsumptionRepository;
import com.gestiva.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.inventory.valuation.repository.InventoryMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional(readOnly = true)
public class InventoryConsumptionWebService {

    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryLayerConsumptionRepository inventoryLayerConsumptionRepository;
    private final InventoryLayerRepository inventoryLayerRepository;
    private final ItemRepository itemRepository;

    public InventoryConsumptionWebService(InventoryMovementRepository inventoryMovementRepository,
                                          InventoryLayerConsumptionRepository inventoryLayerConsumptionRepository,
                                          InventoryLayerRepository inventoryLayerRepository,
                                          ItemRepository itemRepository) {
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.inventoryLayerConsumptionRepository = inventoryLayerConsumptionRepository;
        this.inventoryLayerRepository = inventoryLayerRepository;
        this.itemRepository = itemRepository;
    }

    public OutboundValuationDetailView getOutboundValuationDetail(Long tenantId, Long movementId) {
        var movement = inventoryMovementRepository.findByTenantIdAndId(tenantId, movementId)
                .orElseThrow(() -> new BusinessException("Movimento non trovato."));

        if (!"OUT".equalsIgnoreCase(movement.getMovementType())
                && !"ADJUSTMENT_OUT".equalsIgnoreCase(movement.getMovementType())) {
            throw new BusinessException("Il movimento non è uno scarico valorizzabile.");
        }

        var item = itemRepository.findByTenantIdAndId(tenantId, movement.getItemId())
                .orElseThrow(() -> new BusinessException("Articolo non trovato."));

        var consumptions = inventoryLayerConsumptionRepository
                .findByTenantIdAndOutMovementIdOrderByIdAsc(tenantId, movementId);

        OutboundValuationDetailView view = new OutboundValuationDetailView();
        view.setMovementId(movement.getId());
        view.setItemId(item.getId());
        view.setItemCode(item.getCode());
        view.setItemName(item.getName());
        view.setFormattedMovementDate(PdfFormatUtils.formatDate(movement.getMovementDate()));
        view.setCausalCode(movement.getCausalCode());
        view.setReferenceLabel(buildReferenceLabel(movement.getReferenceType(), movement.getReferenceId()));
        view.setFormattedQuantity(formatQty(movement.getQuantity()));
        view.setFormattedUnitCost(formatCost(movement.getUnitCost()));
        view.setFormattedTotalCost(formatCost(movement.getTotalCost()));

        for (var consumption : consumptions) {
            var layer = inventoryLayerRepository.findByTenantIdAndId(tenantId, consumption.getLayerId())
                    .orElseThrow(() -> new BusinessException("Layer non trovato: " + consumption.getLayerId()));

            InventoryConsumptionLineView line = new InventoryConsumptionLineView();
            line.setLayerId(layer.getId());
            line.setFormattedLayerDate(PdfFormatUtils.formatDate(layer.getLayerDate()));
            line.setFormattedConsumedQty(formatQty(consumption.getConsumedQty()));
            line.setFormattedUnitCost(formatCost(consumption.getUnitCost()));
            line.setFormattedTotalCost(formatCost(consumption.getTotalCost()));
            view.getConsumptions().add(line);
        }

        return view;
    }

    private String buildReferenceLabel(String referenceType, Long referenceId) {
        if (referenceType == null || referenceId == null) {
            return "-";
        }
        return referenceType + " #" + referenceId;
    }

    private String formatQty(BigDecimal value) {
        return value == null
                ? "0.000"
                : value.setScale(3, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatCost(BigDecimal value) {
        return value == null
                ? "0.0000"
                : value.setScale(4, RoundingMode.HALF_UP).toPlainString();
    }
}