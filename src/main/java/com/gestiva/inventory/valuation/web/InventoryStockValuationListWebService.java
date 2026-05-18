package com.gestiva.inventory.valuation.web;

import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.inventory.item.entity.Item;
import com.gestiva.inventory.item.repository.ItemRepository;
import com.gestiva.inventory.valuation.entity.InventoryLayer;
import com.gestiva.inventory.valuation.repository.InventoryLayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InventoryStockValuationListWebService {

    private final ItemRepository itemRepository;
    private final InventoryLayerRepository inventoryLayerRepository;

    public InventoryStockValuationListWebService(ItemRepository itemRepository,
                                                 InventoryLayerRepository inventoryLayerRepository) {
        this.itemRepository = itemRepository;
        this.inventoryLayerRepository = inventoryLayerRepository;
    }

    public List<InventoryStockValuationListItemView> findAll(Long tenantId, String q) {
        List<Item> items = itemRepository.findByTenantIdOrderByCodeAsc(tenantId).stream()
                .filter(Item::isTrackStock)
                .toList();

        Map<Long, List<InventoryLayer>> layersByItemId = inventoryLayerRepository
                .findByTenantIdAndClosedFalseOrderByItemIdAscLayerDateAscIdAsc(tenantId)
                .stream()
                .collect(Collectors.groupingBy(InventoryLayer::getItemId, LinkedHashMap::new, Collectors.toList()));

        String search = q == null ? null : q.trim().toLowerCase();

        List<InventoryStockValuationListItemView> result = new ArrayList<>();

        for (Item item : items) {
            if (search != null && !search.isBlank()) {
                boolean matches = (item.getCode() != null && item.getCode().toLowerCase().contains(search))
                        || (item.getName() != null && item.getName().toLowerCase().contains(search));
                if (!matches) {
                    continue;
                }
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

        result.sort(Comparator.comparing(InventoryStockValuationListItemView::getItemCode, Comparator.nullsLast(String::compareTo)));
        return result;
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
        return value == null
                ? "0"
                : value.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }
    private String formatCost(BigDecimal value) {
        return PdfFormatUtils.formatDecimal(value, 2);
    }
}