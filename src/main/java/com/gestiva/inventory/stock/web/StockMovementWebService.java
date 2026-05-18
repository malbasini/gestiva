package com.gestiva.inventory.stock.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.inventory.item.repository.ItemRepository;
import com.gestiva.inventory.movement.service.InventoryMovementService;
import com.gestiva.inventory.stock.entity.StockMovement;
import com.gestiva.inventory.stock.repository.StockMovementRepository;
import com.gestiva.inventory.valuation.service.InventoryAvailabilityService;
import com.gestiva.inventory.valuation.service.InventoryValuationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class StockMovementWebService {

    private final ItemRepository itemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InventoryAvailabilityService inventoryAvailabilityService;
    private final InventoryMovementService inventoryMovementService;
    private final InventoryValuationService inventoryValuationService;

    public StockMovementWebService(ItemRepository itemRepository,
                                   StockMovementRepository stockMovementRepository,
                                   InventoryAvailabilityService inventoryAvailabilityService,
                                   InventoryMovementService inventoryMovementService,
                                   InventoryValuationService inventoryValuationService
                                   ) {

        this.itemRepository = itemRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.inventoryAvailabilityService = inventoryAvailabilityService;
        this.inventoryMovementService = inventoryMovementService;
        this.inventoryValuationService = inventoryValuationService;
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long tenantId, Long itemId) {
        ensureTrackableProduct(tenantId, itemId);
        return stockMovementRepository.calculateStockBalance(tenantId, itemId);
    }

    @Transactional(readOnly = true)
    public String getFormattedBalance(Long tenantId, Long itemId) {
        return PdfFormatUtils.formatDecimal(getBalance(tenantId, itemId));
    }

    @Transactional(readOnly = true)
    public List<StockMovementListItemView> getRecentMovements(Long tenantId, Long itemId) {
        ensureTrackableProduct(tenantId, itemId);

        return stockMovementRepository
                .findTop20ByTenantIdAndItemIdOrderByMovementDateDescIdDesc(tenantId, itemId)
                .stream()
                .map(this::toView)
                .toList();
    }

    public void createManualMovement(Long tenantId, Long itemId, StockMovementForm form) {
        var item = itemRepository.findByTenantIdAndId(tenantId, itemId)
                .orElseThrow(() -> new NotFoundException("Articolo non trovato"));

        if (!item.isTrackStock()) {
            throw new BusinessException("Questo articolo non gestisce magazzino.");
        }

        String direction = normalize(form.getDirection());
        String reasonCode = normalize(form.getReasonCode());

        if (!"IN".equals(direction) && !"OUT".equals(direction)) {
            throw new BusinessException("Direzione movimento non valida.");
        }

        if (!"MANUAL_LOAD".equals(reasonCode) && !"MANUAL_UNLOAD".equals(reasonCode)) {
            throw new BusinessException("Causale movimento non valida.");
        }

        if ("IN".equals(direction) && !"MANUAL_LOAD".equals(reasonCode)) {
            throw new BusinessException("Per i carichi manuali usare causale MANUAL_LOAD.");
        }

        if ("OUT".equals(direction) && !"MANUAL_UNLOAD".equals(reasonCode)) {
            throw new BusinessException("Per gli scarichi manuali usare causale MANUAL_UNLOAD.");
        }

        BigDecimal quantity = form.getQuantity();
        if (quantity == null || quantity.signum() <= 0) {
            throw new BusinessException("La quantità deve essere maggiore di zero.");
        }

        if ("OUT".equals(direction)) {
            BigDecimal currentBalance = stockMovementRepository.calculateStockBalance(tenantId, itemId);
            if (currentBalance.compareTo(quantity) < 0) {
                throw new BusinessException("Giacenza insufficiente per effettuare lo scarico.");
            }

            inventoryAvailabilityService.validateAvailability(tenantId, itemId, quantity);
        }

        if ("IN".equals(direction) && (form.getUnitCost() == null || form.getUnitCost().signum() <= 0)) {
            throw new BusinessException("Per i carichi manuali il costo unitario è obbligatorio.");
        }

        StockMovement movement = new StockMovement();
        movement.setTenantId(tenantId);
        movement.setItemId(itemId);
        movement.setMovementDate(form.getMovementDate());
        movement.setDirection(direction);
        movement.setReasonCode(reasonCode);
        movement.setQuantity(quantity);
        movement.setNotes(form.getNotes());
        movement.setReferenceType("MANUAL");
        movement.setReferenceId(null);

        StockMovement saved = stockMovementRepository.save(movement);

        Long inventoryMovementId = inventoryMovementService.registerMovement(
                tenantId,
                itemId,
                form.getMovementDate(),
                "IN".equals(direction) ? "IN" : "OUT",
                "IN".equals(direction) ? "MANUAL_IN" : "MANUAL_OUT",
                quantity,
                "IN".equals(direction) ? form.getUnitCost() : null,
                "MANUAL_MOVEMENT",
                saved.getId(),
                form.getNotes()
        );

        if ("IN".equals(direction)) {
            inventoryValuationService.applyInboundValuation(tenantId, inventoryMovementId);
        } else {
            inventoryValuationService.applyOutboundValuation(tenantId, inventoryMovementId);
        }
    }
    private void ensureTrackableProduct(Long tenantId, Long itemId) {
        var item = itemRepository.findByTenantIdAndId(tenantId, itemId)
                .orElseThrow(() -> new NotFoundException("Articolo non trovato"));

        if (!item.isTrackStock()) {
            throw new BusinessException("Questo articolo non gestisce magazzino.");
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private StockMovementListItemView toView(StockMovement movement) {
        StockMovementListItemView v = new StockMovementListItemView();
        v.setId(movement.getId());
        v.setFormattedMovementDate(PdfFormatUtils.formatDate(movement.getMovementDate()));
        v.setDirection(movement.getDirection());
        v.setReasonCode(movement.getReasonCode());
        v.setFormattedQuantity(PdfFormatUtils.formatDecimalTrimmed(movement.getQuantity(),2));
        v.setNotes(movement.getNotes());
        v.setReferenceType(movement.getReferenceType());
        v.setReferenceId(movement.getReferenceId());
        return v;
    }
}