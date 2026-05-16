package com.gestiva.inventory.movement.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.inventory.item.repository.ItemRepository;
import com.gestiva.inventory.movement.repository.InventoryMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional(readOnly = true)
public class InventoryLedgerWebService {

    private final ItemRepository itemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public InventoryLedgerWebService(ItemRepository itemRepository,
                                     InventoryMovementRepository inventoryMovementRepository) {
        this.itemRepository = itemRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    public ItemInventoryLedgerView getLedger(Long tenantId, Long itemId) {
        var item = itemRepository.findByTenantIdAndId(tenantId, itemId)
                .orElseThrow(() -> new BusinessException("Articolo non trovato"));

        if (!item.isTrackStock()) {
            throw new BusinessException("L'articolo non è gestito a magazzino.");
        }

        var movements = inventoryMovementRepository.findByTenantIdAndItemIdOrderByMovementDateAscIdAsc(tenantId, itemId);

        BigDecimal runningBalance = zeroQty();

        ItemInventoryLedgerView view = new ItemInventoryLedgerView();
        view.setItemId(item.getId());
        view.setItemCode(item.getCode());
        view.setItemName(item.getName());
        view.setItemType(item.getItemType());
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

            view.getLines().add(line);
        }

        view.setFormattedCurrentStock(formatQty(runningBalance));
        return view;
    }

    public BigDecimal calculateCurrentStock(Long tenantId, Long itemId) {
        var movements = inventoryMovementRepository.findByTenantIdAndItemIdOrderByMovementDateAscIdAsc(tenantId, itemId);

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
        return value == null ? "0,000" : value.setScale(3, RoundingMode.HALF_UP).toPlainString();
    }
}
