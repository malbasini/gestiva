package com.gestiva.inventory.item.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.warehouse.item.entity.ItemType;
import com.gestiva.common.util.NumberInputUtils;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.warehouse.item.entity.Item;
import com.gestiva.inventory.item.repository.ItemRepository;
import com.gestiva.inventory.movement.repository.InventoryMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gestiva.warehouse.uom.entity.UnitOfMeasure;
import com.gestiva.warehouse.uom.repository.UnitOfMeasureRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class ItemWebService {

    private final ItemRepository itemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;



    public ItemWebService(
            ItemRepository itemRepository,
            InventoryMovementRepository inventoryMovementRepository,
            UnitOfMeasureRepository unitOfMeasureRepository) {

        this.itemRepository = itemRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
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
        form.setItemType(item.getItemType().toString());
        form.setUnitOfMeasure(item.getBaseUom().getCode());
        form.setActive(item.isActive());
        form.setTrackStock(item.isTrackStock());
        form.setBasePrice(PdfFormatUtils.formatDecimal(item.getBasePrice(),2));
        form.setDefaultTaxPct(PdfFormatUtils.formatDecimal(item.getDefaultTaxPct(),0));
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

        String itemType = form.getItemType() == null
                ? ""
                : form.getItemType()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!"PRODUCT".equals(itemType)
                && !"SERVICE".equals(itemType)) {
            throw new BusinessException(
                    "Tipo articolo non valido."
            );
        }

        item.setName(form.getName().trim());
        item.setDescription(form.getDescription());
        item.setItemType(ItemType.valueOf(itemType));

        String uomCode = form.getUnitOfMeasure() == null
                ? ""
                : form.getUnitOfMeasure()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (uomCode.isEmpty()) {
            throw new BusinessException(
                    "Unità di misura obbligatoria."
            );
        }

        UnitOfMeasure baseUom = unitOfMeasureRepository
                .findByTenantIdAndCode(
                        item.getTenantId(),
                        uomCode
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "Unità di misura non valida: "
                                        + uomCode
                        )
                );

        item.setBaseUom(baseUom);

        item.setActive(form.isActive());

        item.setBasePrice(
                NumberInputUtils.parseDecimal(
                        form.getBasePrice(),
                        "base price"
                )
        );

        item.setDefaultTaxPct(
                NumberInputUtils.parseDecimal(
                        form.getDefaultTaxPct(),
                        "Tax Pct"
                )
        );
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }

    private ItemListItemView toListItemView(Item item) {
        ItemListItemView v = new ItemListItemView();
        v.setId(item.getId());
        v.setCode(item.getCode());
        v.setName(item.getName());
        v.setItemType(item.getItemType().toString());
        v.setUnitOfMeasure(item.getBaseUom().getCode());
        v.setActive(item.isActive());
        v.setTrackStock(item.isTrackStock());
        v.setFormattedBasePrice(item.getBasePrice() != null ? PdfFormatUtils.formatDecimal(item.getBasePrice(),2) : "-");
        v.setFormattedDefaultTaxPct(item.getDefaultTaxPct() != null ? PdfFormatUtils.formatDecimal(item.getDefaultTaxPct(), 0) + "%" : "-");
        return v;
    }

    private ItemDetailView toDetailView(Item item) {
        ItemDetailView v = new ItemDetailView();
        v.setId(item.getId());
        v.setCode(item.getCode());
        v.setName(item.getName());
        v.setDescription(item.getDescription());
        v.setItemType(item.getItemType().toString());
        v.setUnitOfMeasure(item.getBaseUom().getCode());
        v.setActive(item.isActive());
        v.setTrackStock(item.isTrackStock());
        v.setStockManaged(item.isTrackStock());
        v.setFormattedBasePrice(item.getBasePrice() != null ? PdfFormatUtils.formatDecimal(item.getBasePrice(),2) : "-");
        v.setFormattedDefaultTaxPct(item.getDefaultTaxPct() != null ? PdfFormatUtils.formatDecimal(item.getDefaultTaxPct(),0) + "%" : "-");
        if (item.isTrackStock()) {
            var currentStock = inventoryMovementRepository.calculateInventoryBalance(item.getTenantId(), item.getId());
            v.setFormattedStockBalance(
                    PdfFormatUtils.formatDecimal(currentStock == null ? BigDecimal.ZERO : currentStock, 0)
            );
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
                    v.setItemType(item.getItemType().toString());
                    v.setUnitOfMeasure(item.getBaseUom().getCode());
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
        view.setUnitOfMeasure(item.getBaseUom().getCode());
        view.setItemType(item.getItemType().toString());
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
        v.setItemType(item.getItemType().toString());
        v.setTrackStock(item.isTrackStock());
        v.setActive(item.isActive());
        v.setUnitOfMeasure(item.getBaseUom().getCode());
        v.setFormattedBasePrice(item.getBasePrice() != null ? PdfFormatUtils.formatDecimal(item.getBasePrice(),2) : "-");
        v.setFormattedDefaultTaxPct(item.getDefaultTaxPct() != null ? PdfFormatUtils.formatDecimalTrimmed(item.getDefaultTaxPct(),2) + "%" : "-");
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