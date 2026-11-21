package com.gestiva.warehouse.inventory.movement.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import com.gestiva.warehouse.location.repository.WarehouseLocationRepository;
import java.util.HashMap;
import java.util.Map;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional(readOnly = true)
public class InventoryLedgerWebService {

    private final CompanyContext companyContext;
    private final ItemRepository itemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseLocationRepository warehouseLocationRepository;

    public InventoryLedgerWebService(
            CompanyContext companyContext,
            ItemRepository itemRepository,
            InventoryMovementRepository inventoryMovementRepository,
            WarehouseRepository warehouseRepository,
            WarehouseLocationRepository warehouseLocationRepository) {
        this.companyContext = companyContext;
        this.itemRepository = itemRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.warehouseRepository = warehouseRepository;
        this.warehouseLocationRepository = warehouseLocationRepository;
    }

    public ItemInventoryLedgerView getLedger(Long tenantId, Long itemId) {
        Long companyId = companyContext.requireCurrentCompanyId();
        var item = itemRepository.findByTenantIdAndId(tenantId, itemId)
                .orElseThrow(() -> new BusinessException("Articolo non trovato"));

        if (!item.isTrackStock()) {
            throw new BusinessException("L'articolo non è gestito a magazzino.");
        }

        var movements = inventoryMovementRepository.findByTenantIdAndCompanyIdAndItemIdOrderByMovementDateAscIdAsc(tenantId, companyId, itemId);

        BigDecimal runningBalance = zeroQty();

        // Cache limitate alla singola consultazione.
        Map<Long, String> warehouseLabels = new HashMap<>();
        Map<String, String> locationLabels = new HashMap<>();

        ItemInventoryLedgerView view = new ItemInventoryLedgerView();
        view.setItemId(item.getId());
        view.setItemCode(item.getCode());
        view.setItemName(item.getName());
        view.setItemType(item.getItemType().toString());
        view.setStockManaged(item.isTrackStock());

        for (var movement : movements) {
            BigDecimal qtyIn = isInbound(movement.getMovementType()) ? qty(movement.getQuantity()) : zeroQty();
            BigDecimal qtyOut = isOutbound(movement.getMovementType()) ? qty(movement.getQuantity()) : zeroQty();

            runningBalance = runningBalance.add(qtyIn).subtract(qtyOut);

            ItemInventoryLedgerLineView line = new ItemInventoryLedgerLineView();
            line.setMovementId(movement.getId());
            line.setFormattedMovementDate(PdfFormatUtils.formatDate(movement.getMovementDate()));
            line.setMovementType(movement.getMovementType());
            line.setCausalCode(movement.getCausalCode());
            line.setReferenceLabel(buildReferenceLabel(movement.getReferenceType(), movement.getReferenceId()));
            line.setFormattedQtyIn(formatQty(qtyIn));
            line.setFormattedQtyOut(formatQty(qtyOut));
            line.setFormattedRunningBalance(formatQty(runningBalance));
            line.setNotes(movement.getNotes());

            Long warehouseId = movement.getWarehouseId();
            Long locationId = movement.getLocationId();

            if (warehouseId == null) {
                line.setWarehouseLabel("Magazzino non indicato");
                line.setLocationLabel(
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

                line.setWarehouseLabel(warehouseLabel);

                if (locationId == null) {
                    line.setLocationLabel("Merce senza ubicazione");
                } else if ("Magazzino non disponibile".equals(warehouseLabel)) {
                    line.setLocationLabel("Ubicazione non verificabile");
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

                    line.setLocationLabel(locationLabel);
                }
            }

            view.getLines().add(line);
        }

        view.setFormattedCurrentStock(formatQty(runningBalance));
        return view;
    }

    public BigDecimal calculateCurrentStock(Long tenantId, Long itemId) {
        Long companyId = companyContext.requireCurrentCompanyId();
        var movements = inventoryMovementRepository.findByTenantIdAndCompanyIdAndItemIdOrderByMovementDateAscIdAsc(tenantId, companyId, itemId);

        BigDecimal stock = zeroQty();
        for (var movement : movements) {
            if (isInbound(movement.getMovementType())) {
                stock = stock.add(qty(movement.getQuantity()));
            } else if (isOutbound(movement.getMovementType())) {
                stock = stock.subtract(qty(movement.getQuantity()));
            }
        }
        return stock;
    }

    private boolean isInbound(String movementType) {
        return "IN".equalsIgnoreCase(movementType) || "ADJUSTMENT_IN".equalsIgnoreCase(movementType);
    }

    private boolean isOutbound(String movementType) {
        return "OUT".equalsIgnoreCase(movementType) || "ADJUSTMENT_OUT".equalsIgnoreCase(movementType);
    }

    private String buildReferenceLabel(String referenceType, Long referenceId) {
        if (referenceType == null || referenceId == null) {
            return "-";
        }
        return referenceType + " #" + referenceId;
    }

    private BigDecimal qty(BigDecimal value) {
        return value == null
                ? zeroQty()
                : value.setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroQty() {
        return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
    }

    private String formatQty(BigDecimal value) {
        return value == null
                ? "0"
                : value.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }
}
