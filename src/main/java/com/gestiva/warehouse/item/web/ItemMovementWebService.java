package com.gestiva.warehouse.item.web;

import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import com.gestiva.warehouse.location.repository.WarehouseLocationRepository;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemMovementWebService {

    private final CompanyContext companyContext;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseLocationRepository warehouseLocationRepository;

    public ItemMovementWebService(
            CompanyContext companyContext,
            InventoryMovementRepository inventoryMovementRepository,
            WarehouseRepository warehouseRepository,
            WarehouseLocationRepository warehouseLocationRepository) {
        this.companyContext = companyContext;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.warehouseRepository = warehouseRepository;
        this.warehouseLocationRepository = warehouseLocationRepository;
    }

    public List<RecentInventoryMovementView> findRecentMovements(Long tenantId, Long itemId) {
        Long companyId = companyContext.requireCurrentCompanyId();

        // Cache locali alla consultazione degli ultimi movimenti.
        Map<Long, String> warehouseLabels = new HashMap<>();
        Map<String, String> locationLabels = new HashMap<>();

        return inventoryMovementRepository
                .findTop10ByTenantIdAndCompanyIdAndItemIdOrderByMovementDateDescIdDesc(tenantId, companyId, itemId)
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

                    Long warehouseId = m.getWarehouseId();
                    Long locationId = m.getLocationId();

                    if (warehouseId == null) {
                        v.setWarehouseLabel("Magazzino non indicato");
                        v.setLocationLabel(
                                locationId == null
                                        ? "Merce senza ubicazione"
                                        : "Ubicazione non verificabile"
                        );
                    } else {
                        String warehouseLabel = warehouseLabels.computeIfAbsent(
                                warehouseId,
                                key -> warehouseRepository
                                        .findByTenantIdAndCompanyIdAndId(
                                                tenantId, companyId, key
                                        )
                                        .map(w -> w.getCode() + " - " + w.getName())
                                        .orElse("Magazzino non disponibile")
                        );

                        v.setWarehouseLabel(warehouseLabel);

                        if (locationId == null) {
                            v.setLocationLabel("Merce senza ubicazione");
                        } else if ("Magazzino non disponibile".equals(warehouseLabel)) {
                            v.setLocationLabel("Ubicazione non verificabile");
                        } else {
                            String locationKey = warehouseId + ":" + locationId;

                            String locationLabel = locationLabels.computeIfAbsent(
                                    locationKey,
                                    key -> warehouseLocationRepository
                                            .findByTenantIdAndWarehouseIdAndId(
                                                    tenantId,
                                                    warehouseId,
                                                    locationId
                                            )
                                            .map(l -> l.getCode() + " - " + l.getName())
                                            .orElse("Ubicazione non disponibile")
                            );

                            v.setLocationLabel(locationLabel);
                        }
                    }

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