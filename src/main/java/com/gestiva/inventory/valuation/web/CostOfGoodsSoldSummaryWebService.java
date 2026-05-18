package com.gestiva.inventory.valuation.web;

import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.inventory.item.entity.Item;
import com.gestiva.inventory.item.repository.ItemRepository;
import com.gestiva.inventory.valuation.repository.InventoryMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CostOfGoodsSoldSummaryWebService {

    private final InventoryMovementRepository inventoryMovementRepository;
    private final ItemRepository itemRepository;

    public CostOfGoodsSoldSummaryWebService(InventoryMovementRepository inventoryMovementRepository,
                                            ItemRepository itemRepository) {
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.itemRepository = itemRepository;
    }

    public List<CostOfGoodsSoldSummaryItemView> summarize(Long tenantId,
                                                          Long itemId,
                                                          LocalDate dateFrom,
                                                          LocalDate dateTo) {

        List<CostOfGoodsSoldSummaryRow> rows =
                inventoryMovementRepository.summarizeCostOfGoodsSoldByItem(tenantId, itemId, dateFrom, dateTo);

        Map<Long, Item> itemsById = itemRepository.findByTenantIdOrderByCodeAsc(tenantId)
                .stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));

        List<CostOfGoodsSoldSummaryItemView> result = new ArrayList<>();

        for (CostOfGoodsSoldSummaryRow row : rows) {
            Item item = itemsById.get(row.getItemId());

            BigDecimal qty = nvlQty(row.getTotalQuantity());
            BigDecimal cost = nvlCost(row.getTotalCost());
            BigDecimal average = qty.compareTo(BigDecimal.ZERO) > 0
                    ? cost.divide(qty, 4, RoundingMode.HALF_UP)
                    : zeroCost();

            CostOfGoodsSoldSummaryItemView view = new CostOfGoodsSoldSummaryItemView();
            view.setItemId(row.getItemId());
            view.setItemCode(item != null ? item.getCode() : "-");
            view.setItemName(item != null ? item.getName() : "-");
            view.setFormattedTotalQuantity(formatQty(qty));
            view.setFormattedTotalCost(formatCost(cost));
            view.setFormattedAverageUnitCost(formatCost(average));

            result.add(view);
        }

        result.sort(Comparator.comparing(CostOfGoodsSoldSummaryItemView::getItemCode, Comparator.nullsLast(String::compareTo)));
        return result;
    }

    private BigDecimal nvlQty(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP)
                : value.setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal nvlCost(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP)
                : value.setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroCost() {
        return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    }

    private String formatQty(BigDecimal value) {
        return value == null
                ? "0"
                : value.stripTrailingZeros().toPlainString();
    }

    private String formatCost(BigDecimal value) {
        return PdfFormatUtils.formatDecimal(value, 2);
    }
}