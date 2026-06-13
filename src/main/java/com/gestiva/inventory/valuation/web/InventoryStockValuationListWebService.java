package com.gestiva.inventory.valuation.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.inventory.item.entity.Item;
import com.gestiva.inventory.item.repository.ItemRepository;
import com.gestiva.inventory.valuation.entity.InventoryLayer;
import com.gestiva.inventory.valuation.repository.InventoryAverageBalanceRepository;
import com.gestiva.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.security.tenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
public class InventoryStockValuationListWebService {

    private final ItemRepository itemRepository;
    private final InventoryLayerRepository inventoryLayerRepository;
    private final TenantRepository tenantRepository;
    private final InventoryAverageBalanceRepository inventoryAverageBalanceRepository;


    public InventoryStockValuationListWebService(ItemRepository itemRepository,
                                                 InventoryLayerRepository inventoryLayerRepository,
                                                 TenantRepository tenantRepository,
                                                 InventoryAverageBalanceRepository inventoryAverageBalanceRepository) {
        this.itemRepository = itemRepository;
        this.inventoryLayerRepository = inventoryLayerRepository;
        this.tenantRepository = tenantRepository;
        this.inventoryAverageBalanceRepository = inventoryAverageBalanceRepository;
    }

    public List<InventoryStockValuationListItemView> findAll(Long tenantId, String q) {
        List<Item> items = itemRepository.findByTenantIdOrderByCodeAsc(tenantId).stream()
                .filter(Item::isTrackStock)
                .toList();

        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException("Tenant non trovato."));

        String method = tenant.getInventoryValuationMethod() != null
                ? String.valueOf(tenant.getInventoryValuationMethod()).trim().toUpperCase()
                : "FIFO";

        String search = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);

        List<InventoryStockValuationListItemView> result = new ArrayList<>();

        if ("AVERAGE".equals(method)) {
            for (Item item : items) {
                boolean matches = search.isBlank()
                        || containsIgnoreCase(item.getCode(), search)
                        || containsIgnoreCase(item.getName(), search);

                if (!matches) {
                    continue;
                }

                var balance = inventoryAverageBalanceRepository.findByTenantIdAndItemId(tenantId, item.getId()).orElse(null);

                BigDecimal currentQty = balance != null ? qty(balance.getCurrentQty()) : zeroQty();
                BigDecimal currentTotalValue = balance != null ? cost(balance.getCurrentTotalValue()) : zeroCost();
                BigDecimal currentAvgUnitCost = balance != null ? cost(balance.getCurrentAvgUnitCost()) : zeroCost();

                InventoryStockValuationListItemView row = new InventoryStockValuationListItemView();
                row.setItemId(item.getId());
                row.setItemCode(item.getCode());
                row.setItemName(item.getName());
                row.setFormattedCurrentQty(formatQty(currentQty));
                row.setFormattedInventoryValue(formatCost(currentTotalValue));
                row.setFormattedAverageResidualCost(formatCost(currentAvgUnitCost));
                row.setOpenLayerCount(0);

                result.add(row);
            }

            result.sort(Comparator.comparing(
                    InventoryStockValuationListItemView::getItemCode,
                    Comparator.nullsLast(String::compareTo)
            ));
            return result;
        }

        Map<Long, List<InventoryLayer>> layersByItemId = inventoryLayerRepository
                .findByTenantIdAndClosedFalseOrderByItemIdAscLayerDateAscIdAsc(tenantId)
                .stream()
                .collect(Collectors.groupingBy(InventoryLayer::getItemId, LinkedHashMap::new, Collectors.toList()));

        for (Item item : items) {
            boolean matches = search.isBlank()
                    || containsIgnoreCase(item.getCode(), search)
                    || containsIgnoreCase(item.getName(), search);

            if (!matches) {
                continue;
            }

            List<InventoryLayer> layers = layersByItemId.getOrDefault(item.getId(), List.of());

            BigDecimal totalQty = zeroQty();
            BigDecimal totalValue = zeroCost();

            for (InventoryLayer layer : layers) {
                BigDecimal remainingQty = qty(layer.getRemainingQty());
                BigDecimal unitCost = cost(layer.getUnitCost());
                BigDecimal residualValue = cost(remainingQty.multiply(unitCost));

                totalQty = qty(totalQty.add(remainingQty));
                totalValue = cost(totalValue.add(residualValue));
            }

            BigDecimal averageResidualCost = totalQty.compareTo(BigDecimal.ZERO) > 0
                    ? cost(totalValue.divide(totalQty, 4, RoundingMode.HALF_UP))
                    : zeroCost();

            InventoryStockValuationListItemView row = new InventoryStockValuationListItemView();
            row.setItemId(item.getId());
            row.setItemCode(item.getCode());
            row.setItemName(item.getName());
            row.setFormattedCurrentQty(formatQty(totalQty));
            row.setFormattedInventoryValue(formatCost(totalValue));
            row.setFormattedAverageResidualCost(formatCost(averageResidualCost));
            row.setOpenLayerCount(layers.size());

            result.add(row);
        }

        result.sort(Comparator.comparing(
                InventoryStockValuationListItemView::getItemCode,
                Comparator.nullsLast(String::compareTo)
        ));
        return result;
    }
    private boolean containsIgnoreCase(String value, String search) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(search);
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