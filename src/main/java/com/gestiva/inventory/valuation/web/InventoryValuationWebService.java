package com.gestiva.inventory.valuation.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.inventory.item.repository.ItemRepository;
import com.gestiva.inventory.valuation.repository.InventoryAverageBalanceRepository;
import com.gestiva.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.security.tenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional(readOnly = true)
public class InventoryValuationWebService {

    private final ItemRepository itemRepository;
    private final TenantRepository tenantRepository;
    private final InventoryLayerRepository inventoryLayerRepository;
    private final InventoryAverageBalanceRepository inventoryAverageBalanceRepository;

    public InventoryValuationWebService(ItemRepository itemRepository,
                                        TenantRepository tenantRepository,
                                        InventoryLayerRepository inventoryLayerRepository,
                                        InventoryAverageBalanceRepository inventoryAverageBalanceRepository) {

        this.itemRepository = itemRepository;
        this.tenantRepository = tenantRepository;
        this.inventoryLayerRepository = inventoryLayerRepository;
        this.inventoryAverageBalanceRepository = inventoryAverageBalanceRepository;
    }

    public ItemInventoryValuationView getItemValuation(Long tenantId, Long itemId) {
        var item = itemRepository.findByTenantIdAndId(tenantId, itemId)
                .orElseThrow(() -> new BusinessException("Articolo non trovato"));

        if (!item.isTrackStock()) {
            throw new BusinessException("L'articolo non è gestito a magazzino.");
        }

        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException("Tenant non trovato"));

        String method = tenant.getInventoryValuationMethod() != null
                ? String.valueOf(tenant.getInventoryValuationMethod()).trim().toUpperCase()
                : "FIFO";

        ItemInventoryValuationView view = new ItemInventoryValuationView();
        view.setItemId(item.getId());
        view.setItemCode(item.getCode());
        view.setItemName(item.getName());
        view.setValuationMethod(method);

        if ("AVERAGE".equals(method)) {
            var balance = inventoryAverageBalanceRepository.findByTenantIdAndItemId(tenantId, itemId).orElse(null);

            BigDecimal currentQty = balance != null ? qty(balance.getCurrentQty()) : zeroQty();
            BigDecimal currentTotalValue = balance != null ? cost(balance.getCurrentTotalValue()) : zeroCost();
            BigDecimal currentAvgUnitCost = balance != null ? cost(balance.getCurrentAvgUnitCost()) : zeroCost();

            view.setAverageMethod(true);
            view.setAverageMethodNote("Per il metodo AVERAGE la valorizzazione è basata sul saldo medio corrente dell'articolo.");
            view.setFormattedCurrentQty(formatQty(currentQty));
            view.setFormattedInventoryValue(formatCost(currentTotalValue));
            view.setFormattedAverageResidualCost(formatCost(currentAvgUnitCost));
            view.setLayers(java.util.List.of());

            return view;
        }

        var openLayers = inventoryLayerRepository
                .findByTenantIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(tenantId, itemId);

        BigDecimal totalQty = zeroQty();
        BigDecimal totalValue = zeroCost();

        for (var layer : openLayers) {
            BigDecimal remainingQty = qty(layer.getRemainingQty());
            BigDecimal unitCost = cost(layer.getUnitCost());
            BigDecimal residualValue = cost(remainingQty.multiply(unitCost));

            totalQty = qty(totalQty.add(remainingQty));
            totalValue = cost(totalValue.add(residualValue));

            InventoryLayerLineView line = new InventoryLayerLineView();
            line.setLayerId(layer.getId());
            line.setFormattedLayerDate(PdfFormatUtils.formatDate(layer.getLayerDate()));
            line.setSourceMovementId(layer.getSourceMovementId());
            line.setFormattedOriginalQty(formatQty(layer.getOriginalQty()));
            line.setFormattedRemainingQty(formatQty(remainingQty));
            line.setFormattedUnitCost(formatCost(unitCost));
            line.setFormattedResidualValue(formatCost(residualValue));

            view.getLayers().add(line);
        }

        BigDecimal averageResidualCost = totalQty.compareTo(BigDecimal.ZERO) > 0
                ? cost(totalValue.divide(totalQty, 4, RoundingMode.HALF_UP))
                : zeroCost();

        view.setAverageMethod(false);
        view.setFormattedCurrentQty(formatQty(totalQty));
        view.setFormattedInventoryValue(formatCost(totalValue));
        view.setFormattedAverageResidualCost(formatCost(averageResidualCost));

        return view;
    }
    private BigDecimal qty(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP)
                : value.setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal cost(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP)
                : value.setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroQty() {
        return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroCost() {
        return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    }

    private String formatQty(BigDecimal value) {
        return PdfFormatUtils.formatDecimal(value == null ? BigDecimal.ZERO : value, 0);
    }

    private String formatCost(BigDecimal value) {
        return PdfFormatUtils.formatDecimal(value == null ? BigDecimal.ZERO : value, 2);
    }
}