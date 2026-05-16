package com.gestiva.inventory.item.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.inventory.item.entity.Item;
import com.gestiva.inventory.item.repository.ItemRepository;
import com.gestiva.inventory.stock.repository.StockMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class ItemWebService {

    private final ItemRepository itemRepository;
    private final StockMovementRepository stockMovementRepository;

    public ItemWebService(ItemRepository itemRepository,
                          StockMovementRepository stockMovementRepository) {
        this.itemRepository = itemRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional(readOnly = true)
    public List<ItemListItemView> findAll(Long tenantId) {
        return itemRepository.findAll(
                org.springframework.data.jpa.domain.Specification
                        .where((root, query, cb) -> cb.equal(root.get("tenantId"), tenantId)),
                Sort.by(Sort.Direction.ASC, "name")
        ).stream().map(this::toListItemView).toList();
    }

    @Transactional(readOnly = true)
    public ItemDetailView getDetail(Long tenantId, Long id) {
        Item item = itemRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Articolo non trovato"));
        return toDetailView(item);
    }

    @Transactional(readOnly = true)
    public ItemForm getForm(Long tenantId, Long id) {
        Item item = itemRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Articolo non trovato"));

        ItemForm form = new ItemForm();
        form.setCode(item.getCode());
        form.setName(item.getName());
        form.setDescription(item.getDescription());
        form.setItemType(item.getItemType());
        form.setUnitOfMeasure(item.getUnitOfMeasure());
        form.setActive(item.isActive());
        form.setTrackStock(item.isTrackStock());
        form.setBasePrice(item.getBasePrice());
        form.setDefaultTaxPct(item.getDefaultTaxPct());
        return form;
    }

    public Long create(Long tenantId, ItemForm form) {
        String code = normalizeCode(form.getCode());

        if (itemRepository.existsByTenantIdAndCode(tenantId, code)) {
            throw new BusinessException("Esiste già un articolo con questo codice.");
        }

        Item item = new Item();
        item.setTenantId(tenantId);
        applyForm(item, form);
        item.setCode(code);

        return itemRepository.save(item).getId();
    }

    public void update(Long tenantId, Long id, ItemForm form) {
        Item item = itemRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Articolo non trovato"));

        String code = normalizeCode(form.getCode());
        itemRepository.findByTenantIdAndCode(tenantId, code)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BusinessException("Esiste già un articolo con questo codice.");
                });

        applyForm(item, form);
        item.setCode(code);

        itemRepository.save(item);
    }

    private void applyForm(Item item, ItemForm form) {
        String itemType = form.getItemType() == null ? "" : form.getItemType().trim().toUpperCase(Locale.ROOT);

        if (!"PRODUCT".equals(itemType) && !"SERVICE".equals(itemType)) {
            throw new BusinessException("Tipo articolo non valido.");
        }

        item.setName(form.getName().trim());
        item.setDescription(form.getDescription());
        item.setItemType(itemType);
        item.setUnitOfMeasure(form.getUnitOfMeasure().trim());
        item.setActive(form.isActive());

        if ("SERVICE".equals(itemType)) {
            item.setTrackStock(false);
        } else {
            item.setTrackStock(form.isTrackStock());
        }

        item.setBasePrice(form.getBasePrice());
        item.setDefaultTaxPct(form.getDefaultTaxPct());
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }

    private ItemListItemView toListItemView(Item item) {
        ItemListItemView v = new ItemListItemView();
        v.setId(item.getId());
        v.setCode(item.getCode());
        v.setName(item.getName());
        v.setItemType(item.getItemType());
        v.setUnitOfMeasure(item.getUnitOfMeasure());
        v.setActive(item.isActive());
        v.setTrackStock(item.isTrackStock());
        v.setFormattedBasePrice(item.getBasePrice() != null ? PdfFormatUtils.formatMoney(item.getBasePrice()) : "-");
        v.setFormattedDefaultTaxPct(item.getDefaultTaxPct() != null ? PdfFormatUtils.formatDecimal(item.getDefaultTaxPct()) + "%" : "-");
        return v;
    }

    private ItemDetailView toDetailView(Item item) {
        ItemDetailView v = new ItemDetailView();
        v.setId(item.getId());
        v.setCode(item.getCode());
        v.setName(item.getName());
        v.setDescription(item.getDescription());
        v.setItemType(item.getItemType());
        v.setUnitOfMeasure(item.getUnitOfMeasure());
        v.setActive(item.isActive());
        v.setTrackStock(item.isTrackStock());
        v.setStockManaged(item.isTrackStock());
        v.setFormattedBasePrice(item.getBasePrice() != null ? PdfFormatUtils.formatMoney(item.getBasePrice()) : "-");
        v.setFormattedDefaultTaxPct(item.getDefaultTaxPct() != null ? PdfFormatUtils.formatDecimal(item.getDefaultTaxPct()) + "%" : "-");
        if (item.isTrackStock()) {
            var balance = stockMovementRepository.calculateStockBalance(item.getTenantId(), item.getId());
            v.setFormattedStockBalance(PdfFormatUtils.formatDecimal(balance));
        } else {
            v.setFormattedStockBalance("-");
        }
        return v;
    }

    @Transactional(readOnly = true)
    public java.util.List<ItemOptionView> findOptions(Long tenantId) {
        return itemRepository.findByTenantIdAndActiveTrueOrderByNameAsc(tenantId)
                .stream()
                .map(item -> {
                    ItemOptionView v = new ItemOptionView();
                    v.setId(item.getId());
                    v.setCode(item.getCode());
                    v.setName(item.getName());
                    v.setItemType(item.getItemType());
                    v.setUnitOfMeasure(item.getUnitOfMeasure());
                    v.setLabel(item.getCode() + " - " + item.getName() + " (" + item.getItemType() + ")");
                    return v;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public ItemAutocompleteView getAutocompleteData(Long tenantId, Long itemId) {
        var item = itemRepository.findByTenantIdAndId(tenantId, itemId)
                .orElseThrow(() -> new com.gestiva.common.exception.NotFoundException("Articolo non trovato"));

        ItemAutocompleteView view = new ItemAutocompleteView();
        view.setId(item.getId());
        view.setCode(item.getCode());
        view.setName(item.getName());
        view.setDescription(item.getDescription());
        view.setUnitOfMeasure(item.getUnitOfMeasure());
        view.setItemType(item.getItemType());
        view.setBasePrice(item.getBasePrice());
        view.setDefaultTaxPct(item.getDefaultTaxPct());
        return view;
    }

    public Page<ItemListItemView> findPage(Long tenantId, int page, int size, String q, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("code")));
        Specification<Item> spec = Specification.where(byTenant(tenantId))
                .and(bySearch(q))
                .and(byStatus(status));
        return itemRepository.findAll(spec, pageable).map(this::toListItemViewItem);
    }

    private Specification<Item> byTenant(Long tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
    }
    private Specification<Item> bySearch(String q) {
        if (q == null || q.trim().isEmpty()) {
            return null;
        }
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("code")), like),
                cb.like(cb.lower(root.get("name")), like)
        );
    }
    private Specification<Item> byStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return (root, query, cb) -> {
            if ("ACTIVE".equalsIgnoreCase(status)) {
                return cb.isTrue(root.get("active"));
            }
            if ("INACTIVE".equalsIgnoreCase(status)) {
                return cb.isFalse(root.get("active"));
            }
            return null;
        };
    }

    private ItemListItemView toListItemViewItem(Item item) {
        ItemListItemView v = new ItemListItemView();
        v.setId(item.getId());
        v.setCode(item.getCode());
        v.setName(item.getName());
        v.setItemType(item.getItemType());
        v.setTrackStock(item.isTrackStock());
        v.setActive(item.isActive());
        v.setUnitOfMeasure(item.getUnitOfMeasure());
        v.setFormattedBasePrice(item.getBasePrice() != null ? PdfFormatUtils.formatMoney(item.getBasePrice()) : "-");
        v.setFormattedDefaultTaxPct(item.getDefaultTaxPct() != null ? PdfFormatUtils.formatDecimal(item.getDefaultTaxPct()) + "%" : "-");
        return v;
    }

    public List<ItemOptionView> findStockManagedOptions(Long tenantId) {
        return itemRepository.findByTenantIdAndActiveTrueOrderByCodeAsc(tenantId).stream()
                .filter(Item::isTrackStock)
                .map(item -> {
                    ItemOptionView v = new ItemOptionView();
                    v.setId(item.getId());
                    v.setLabel(item.getCode() + " - " + item.getName());
                    return v;
                })
                .toList();
    }
}