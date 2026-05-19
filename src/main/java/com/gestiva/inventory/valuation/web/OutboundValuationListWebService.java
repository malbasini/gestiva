package com.gestiva.inventory.valuation.web;

import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.inventory.item.entity.Item;
import com.gestiva.inventory.item.repository.ItemRepository;
import com.gestiva.inventory.movement.entity.InventoryMovement;
import com.gestiva.inventory.valuation.repository.InventoryMovementRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OutboundValuationListWebService {

    private final InventoryMovementRepository inventoryMovementRepository;
    private final ItemRepository itemRepository;

    public OutboundValuationListWebService(InventoryMovementRepository inventoryMovementRepository,
                                           ItemRepository itemRepository) {
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.itemRepository = itemRepository;
    }

    public Page<OutboundValuationListItemView> findPage(Long tenantId,
                                                        int page,
                                                        int size,
                                                        String q,
                                                        String causalCode,
                                                        LocalDate dateFrom,
                                                        LocalDate dateTo) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("movementDate"), Sort.Order.desc("id"))
        );

        Map<Long, Item> itemsById = itemRepository
                .findByTenantIdOrderByCodeAsc(tenantId)
                .stream()
                .collect(Collectors.toMap(
                        com.gestiva.inventory.item.entity.Item::getId,
                        Function.identity()
                ));
        Specification<InventoryMovement> spec = Specification.where(byTenant(tenantId))
                .and(byOutboundType())
                .and(bySearch(q, tenantId))
                .and(byCausalCode(causalCode))
                .and(byDateFrom(dateFrom))
                .and(byDateTo(dateTo));

        return inventoryMovementRepository.findAll(spec, pageable).map(this::toListItemViewInventory);
    }

    private OutboundValuationListItemView toListItemViewInventory(InventoryMovement movement) {

        OutboundValuationListItemView v = new OutboundValuationListItemView();
        v.setMovementId(movement.getId());
        v.setItemId(movement.getItemId());
        v.setFormattedMovementDate(formatDate(movement.getMovementDate()));
        v.setCausalCode(movement.getCausalCode());
        v.setReferenceLabel(buildReferenceLabel(movement.getReferenceType(), movement.getReferenceId()));
        v.setFormattedQuantity(formatQty(movement.getQuantity()));
        v.setFormattedUnitCost(formatCost(movement.getUnitCost()));
        v.setFormattedTotalCost(formatCost(movement.getTotalCost()));
        return v;






    }

    private Specification<InventoryMovement> byTenant(Long tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
    }

    private Specification<InventoryMovement> byOutboundType() {
        return (root, query, cb) -> cb.or(
                cb.equal(cb.upper(root.get("movementType")), "OUT"),
                cb.equal(cb.upper(root.get("movementType")), "ADJUSTMENT_OUT")
        );
    }

    private Specification<InventoryMovement> byCausalCode(String causalCode) {
        if (causalCode == null || causalCode.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("causalCode"), causalCode);
    }

    private Specification<InventoryMovement> byDateFrom(LocalDate dateFrom) {
        if (dateFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("movementDate"), dateFrom);
    }

    private Specification<InventoryMovement> byDateTo(LocalDate dateTo) {
        if (dateTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("movementDate"), dateTo);
    }

    private Specification<InventoryMovement> bySearch(String q, Long tenantId) {
        if (q == null || q.trim().isEmpty()) {
            return null;
        }

        String search = q.trim().toLowerCase();

        return (root, query, cb) -> {
            var itemIds = itemRepository.findByTenantIdOrderByCodeAsc(tenantId).stream()
                    .filter(item ->
                            (item.getCode() != null && item.getCode().toLowerCase().contains(search)) ||
                                    (item.getName() != null && item.getName().toLowerCase().contains(search)))
                    .map(item -> item.getId())
                    .toList();

            if (itemIds.isEmpty()) {
                return cb.or(
                        cb.like(cb.lower(root.get("causalCode")), "%" + search + "%"),
                        cb.like(cb.lower(root.get("referenceType")), "%" + search + "%")
                );
            }

            return cb.or(
                    root.get("itemId").in(itemIds),
                    cb.like(cb.lower(root.get("causalCode")), "%" + search + "%"),
                    cb.like(cb.lower(root.get("referenceType")), "%" + search + "%")
            );
        };
    }

    private OutboundValuationListItemView toListItemView(InventoryMovement movement,
                                                         Map<Long, Item> itemsById) {
        Item item = itemsById.get(movement.getItemId());

        OutboundValuationListItemView v = new OutboundValuationListItemView();
        v.setMovementId(movement.getId());
        v.setItemId(movement.getItemId());
        v.setItemCode(item != null ? item.getCode() : "-");
        v.setItemName(item != null ? item.getName() : "-");
        v.setFormattedMovementDate(formatDate(movement.getMovementDate()));
        v.setCausalCode(movement.getCausalCode());
        v.setReferenceLabel(buildReferenceLabel(movement.getReferenceType(), movement.getReferenceId()));
        v.setFormattedQuantity(formatQty(movement.getQuantity()));
        v.setFormattedUnitCost(formatCost(movement.getUnitCost()));
        v.setFormattedTotalCost(formatCost(movement.getTotalCost()));
        return v;
    }
    @Transactional(readOnly = true)
    public List<OutboundValuationListItemView> findAll(Long tenantId,
                                                       String q,
                                                       String causalCode,
                                                       LocalDate dateFrom,
                                                       LocalDate dateTo) {
        Specification<InventoryMovement> spec = Specification.where(byTenant(tenantId))
                .and(byOutboundType())
                .and(bySearch(q, tenantId))
                .and(byCausalCode(causalCode))
                .and(byDateFrom(dateFrom))
                .and(byDateTo(dateTo));

        List<InventoryMovement> movements = inventoryMovementRepository.findAll(
                spec,
                Sort.by(Sort.Order.desc("movementDate"), Sort.Order.desc("id"))
        );

        Map<Long, Item> itemsById = itemRepository
                .findByTenantIdOrderByCodeAsc(tenantId)
                .stream()
                .collect(Collectors.toMap(
                        com.gestiva.inventory.item.entity.Item::getId,
                        Function.identity()
                ));

        return movements.stream()
                .map(movement -> toListItemView(movement, itemsById))
                .toList();
    }













    private String buildReferenceLabel(String referenceType, Long referenceId) {
        if (referenceType == null || referenceId == null) {
            return "-";
        }
        return referenceType + " #" + referenceId;
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "-";
        return String.format("%02d/%02d/%04d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
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