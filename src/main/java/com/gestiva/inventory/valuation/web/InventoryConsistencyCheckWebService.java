package com.gestiva.inventory.valuation.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.inventory.item.entity.Item;
import com.gestiva.inventory.item.repository.ItemRepository;
import com.gestiva.inventory.valuation.repository.InventoryMovementRepository;
import com.gestiva.inventory.valuation.entity.InventoryLayer;
import com.gestiva.inventory.valuation.repository.InventoryAverageBalanceRepository;
import com.gestiva.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.security.tenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class InventoryConsistencyCheckWebService {

    private final ItemRepository itemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryLayerRepository inventoryLayerRepository;
    private final TenantRepository tenantRepository;
    private final InventoryAverageBalanceRepository inventoryAverageBalanceRepository;

    public InventoryConsistencyCheckWebService(ItemRepository itemRepository,
                                               InventoryMovementRepository inventoryMovementRepository,
                                               InventoryLayerRepository inventoryLayerRepository,
                                               TenantRepository tenantRepository,
                                               InventoryAverageBalanceRepository inventoryAverageBalanceRepository) {
        this.itemRepository = itemRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.inventoryLayerRepository = inventoryLayerRepository;
        this.tenantRepository = tenantRepository;
        this.inventoryAverageBalanceRepository = inventoryAverageBalanceRepository;
    }

    public List<InventoryConsistencyCheckItemView> findAll(Long tenantId, String q, Boolean onlyDifferences) {
        String search = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);

        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException("Tenant non trovato."));

        String method = tenant.getInventoryValuationMethod() != null
                ? String.valueOf(tenant.getInventoryValuationMethod()).trim().toUpperCase(Locale.ROOT)
                : "FIFO";

        List<Item> items = itemRepository.findByTenantIdOrderByCodeAsc(tenantId).stream()
                .filter(Item::isTrackStock)
                .filter(item -> search.isBlank()
                        || containsIgnoreCase(item.getCode(), search)
                        || containsIgnoreCase(item.getName(), search))
                .toList();

        List<InventoryConsistencyCheckItemView> result = new ArrayList<>();

        for (Item item : items) {
            BigDecimal ledgerQty = qty(
                    inventoryMovementRepository.calculateInventoryBalance(tenantId, item.getId())
            );

            BigDecimal valuationQty;
            if ("AVERAGE".equals(method)) {
                valuationQty = inventoryAverageBalanceRepository.findByTenantIdAndItemId(tenantId, item.getId())
                        .map(balance -> qty(balance.getCurrentQty()))
                        .orElse(zeroQty());
            } else {
                valuationQty = inventoryLayerRepository
                        .findByTenantIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(tenantId, item.getId())
                        .stream()
                        .map(InventoryLayer::getRemainingQty)
                        .map(this::qty)
                        .reduce(zeroQty(), BigDecimal::add);
            }

            BigDecimal difference = qty(ledgerQty.subtract(valuationQty));
            boolean aligned = difference.compareTo(zeroQty()) == 0;

            if (Boolean.TRUE.equals(onlyDifferences) && aligned) {
                continue;
            }

            InventoryConsistencyCheckItemView row = new InventoryConsistencyCheckItemView();
            row.setItemId(item.getId());
            row.setItemCode(item.getCode());
            row.setItemName(item.getName());
            row.setFormattedLedgerQty(formatQty(ledgerQty));
            row.setFormattedLayerQty(formatQty(valuationQty));
            row.setFormattedDifferenceQty(formatQty(difference));
            row.setAligned(aligned);
            row.setStatusLabel(aligned ? "OK" : "DA VERIFICARE");

            result.add(row);
        }

        result.sort(Comparator.comparing(
                InventoryConsistencyCheckItemView::getItemCode,
                Comparator.nullsLast(String::compareTo)
        ));

        return result;
    }

    private boolean containsIgnoreCase(String value, String search) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(search);
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
        return PdfFormatUtils.formatDecimal(value == null ? BigDecimal.ZERO : value, 0);
    }
}