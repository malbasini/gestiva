package com.gestiva.warehouse.inventory.valuation.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import java.util.Objects;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerConsumptionRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional(readOnly = true)
public class InventoryConsumptionWebService {

    private final CompanyContext companyContext;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryLayerConsumptionRepository inventoryLayerConsumptionRepository;
    private final InventoryLayerRepository inventoryLayerRepository;
    private final ItemRepository itemRepository;

    public InventoryConsumptionWebService(CompanyContext companyContext, InventoryMovementRepository inventoryMovementRepository,
                                          InventoryLayerConsumptionRepository inventoryLayerConsumptionRepository,
                                          InventoryLayerRepository inventoryLayerRepository,
                                          ItemRepository itemRepository) {
        this.companyContext = companyContext;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.inventoryLayerConsumptionRepository = inventoryLayerConsumptionRepository;
        this.inventoryLayerRepository = inventoryLayerRepository;
        this.itemRepository = itemRepository;
    }

    public OutboundValuationDetailView getOutboundValuationDetail(Long tenantId, Long movementId) {
        Long companyId = companyContext.requireCurrentCompanyId();
        var movement = inventoryMovementRepository.findByTenantIdAndCompanyIdAndId(tenantId, companyId, movementId)
                .orElseThrow(() -> new BusinessException("Movimento non trovato."));

        if (!"OUT".equalsIgnoreCase(movement.getMovementType())
                && !"ADJUSTMENT_OUT".equalsIgnoreCase(movement.getMovementType())) {
            throw new BusinessException("Il movimento non è uno scarico valorizzabile.");
        }

        var item = itemRepository.findByTenantIdAndId(tenantId, movement.getItemId())
                .orElseThrow(() -> new BusinessException("Articolo non trovato."));

        if (movement.getWarehouseId() == null) {
            throw new BusinessException("Magazzino del movimento non valorizzato.");
        }

        var consumptions = inventoryLayerConsumptionRepository
                .findByTenantIdAndCompanyIdAndWarehouseIdAndOutMovementIdOrderByIdAsc(
                        tenantId, companyId, movement.getWarehouseId(), movementId);

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
            var layer = inventoryLayerRepository.findByTenantIdAndCompanyIdAndId(tenantId, companyId, consumption.getLayerId())
                    .orElseThrow(() -> new BusinessException("Layer non trovato: " + consumption.getLayerId()));

            if (!Objects.equals(layer.getWarehouseId(), movement.getWarehouseId())) {
                throw new BusinessException("Layer FIFO appartenente a un magazzino differente.");
            }

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
                ? "0"
                : value.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatCost(BigDecimal value) {
        return PdfFormatUtils.formatDecimal(value, 2);
    }
}