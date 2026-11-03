package com.gestiva.warehouse.inventory.valuation.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.inventory.movement.web.InventoryLedgerWebService;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.warehouse.item.entity.Item;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryAverageBalanceRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.security.tenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class InventoryConsistencyCheckWebService {

    private final CompanyContext companyContext;
    private final InventoryLedgerWebService inventoryLedgerWebService;
    private final ItemRepository itemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryLayerRepository inventoryLayerRepository;
    private final TenantRepository tenantRepository;
    private final InventoryAverageBalanceRepository inventoryAverageBalanceRepository;

    public InventoryConsistencyCheckWebService(CompanyContext companyContext,
                                               InventoryLedgerWebService inventoryLedgerWebService,
                                               ItemRepository itemRepository,
                                               InventoryMovementRepository inventoryMovementRepository,
                                               InventoryLayerRepository inventoryLayerRepository,
                                               TenantRepository tenantRepository,
                                               InventoryAverageBalanceRepository inventoryAverageBalanceRepository) {
        this.companyContext = companyContext;
        this.inventoryLedgerWebService = inventoryLedgerWebService;
        this.itemRepository = itemRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.inventoryLayerRepository = inventoryLayerRepository;
        this.tenantRepository = tenantRepository;
        this.inventoryAverageBalanceRepository = inventoryAverageBalanceRepository;
    }

    public List<InventoryConsistencyCheckItemView> findAll(Long tenantId, String q, Boolean onlyDifferences) {
        Long companyId = companyContext.requireCurrentCompanyId();
        String search = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);

        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException("Tenant non trovato."));

        String method = tenant.getInventoryValuationMethod() != null
                ? String.valueOf(tenant.getInventoryValuationMethod()).trim().toUpperCase(Locale.ROOT)
                : "FIFO";

        // Gestiva V2: il controllo di coerenza è disponibile solo per FIFO.
        // I saldi AVERAGE legacy non sono isolati per società.
        if (!"FIFO".equals(method)) {
            throw new BusinessException(
                    "Gestiva V2 supporta esclusivamente FIFO. "
                    + "Il controllo di coerenza legacy non è disponibile in modalità multi-società."
            );
        }


        Set<Long> companyVisibleItemIds = new HashSet<>(
                inventoryMovementRepository
                        .findDistinctItemIdsByTenantIdAndCompanyId(tenantId, companyId));
        companyVisibleItemIds.addAll(
                inventoryLayerRepository
                        .findDistinctItemIdsByTenantIdAndCompanyId(tenantId, companyId));

        List<Item> items = itemRepository.findByTenantIdOrderByCodeAsc(tenantId).stream()
                .filter(Item::isTrackStock)
                .filter(item -> companyVisibleItemIds.contains(item.getId()))
                .filter(item -> search.isBlank()
                        || containsIgnoreCase(item.getCode(), search)
                        || containsIgnoreCase(item.getName(), search))
                .toList();

        List<InventoryConsistencyCheckItemView> result = new ArrayList<>();

        for (Item item : items) {
            BigDecimal ledgerQty = qty(
                    inventoryLedgerWebService.calculateCurrentStock(tenantId, item.getId())
            );

            BigDecimal valuationQty;
            if ("AVERAGE".equals(method)) {
                valuationQty = inventoryAverageBalanceRepository.findByTenantIdAndItemId(tenantId, item.getId())
                        .map(balance -> qty(balance.getCurrentQty()))
                        .orElse(zeroQty());
            } else {
                valuationQty = inventoryLayerRepository
                        .findByTenantIdAndCompanyIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
                                tenantId, companyId, item.getId())
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