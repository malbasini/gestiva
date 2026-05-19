package com.gestiva.inventory.valuation.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.inventory.item.repository.ItemRepository;
import com.gestiva.inventory.valuation.model.InventoryValuationMethod;
import com.gestiva.inventory.valuation.repository.InventoryAverageBalanceRepository;
import com.gestiva.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.security.tenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional(readOnly = true)
public class InventoryAvailabilityService {

    private final InventoryLayerRepository inventoryLayerRepository;
    private final ItemRepository itemRepository;
    private final TenantRepository tenantRepository;
    private final InventoryAverageBalanceRepository inventoryAverageBalanceRepository;

    public InventoryAvailabilityService(InventoryLayerRepository inventoryLayerRepository,
                                        ItemRepository itemRepository,
                                        TenantRepository tenantRepository,
                                        InventoryAverageBalanceRepository inventoryAverageBalanceRepository) {

        this.inventoryLayerRepository = inventoryLayerRepository;
        this.itemRepository = itemRepository;
        this.tenantRepository = tenantRepository;
        this.inventoryAverageBalanceRepository = inventoryAverageBalanceRepository;
    }

    public BigDecimal getAvailableQty(Long tenantId, Long itemId) {
        InventoryValuationMethod method = resolveValuationMethod(tenantId);

        if (method == InventoryValuationMethod.AVERAGE) {
            return inventoryAverageBalanceRepository.findByTenantIdAndItemId(tenantId, itemId).filter(balance -> balance.getCurrentQty() != null).map(balance -> balance.getCurrentQty().setScale(3, RoundingMode.HALF_UP)).orElse(BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));
        }

        return inventoryLayerRepository
                .findByTenantIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(tenantId, itemId)
                .stream()
                .map(layer -> layer.getRemainingQty() == null
                        ? BigDecimal.ZERO
                        : layer.getRemainingQty().setScale(3, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP), BigDecimal::add)
                .setScale(3, RoundingMode.HALF_UP);
    }
    private InventoryValuationMethod resolveValuationMethod(Long tenantId) {
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException("Tenant non trovato."));
        String value = String.valueOf(tenant.getInventoryValuationMethod());
        if (value == null || value.isBlank()) {
            return InventoryValuationMethod.FIFO;
        }
        return InventoryValuationMethod.valueOf(value.trim().toUpperCase());
    }
    public void validateAvailability(Long tenantId, Long itemId, BigDecimal requiredQty) {
        BigDecimal requested = requiredQty == null
                ? BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP)
                : requiredQty.setScale(3, RoundingMode.HALF_UP);

        if (requested.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("La quantità richiesta deve essere maggiore di zero.");
        }

        BigDecimal available = getAvailableQty(tenantId, itemId);

        if (requested.compareTo(available) > 0) {
            var item = itemRepository.findByTenantIdAndId(tenantId, itemId)
                    .orElseThrow(() -> new BusinessException("Articolo non trovato: " + itemId));

            throw new BusinessException(
                    "Disponibilità insufficiente per l'articolo "
                            + item.getCode()
                            + ". Disponibile: "
                            + PdfFormatUtils.formatDecimal(available, 0)
                            + ", richiesta: "
                            + PdfFormatUtils.formatDecimal(requested, 0)
                            + "."
            );
        }
    }
}