package com.gestiva.purchasing.supplier.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.purchasing.supplier.entity.Supplier;
import com.gestiva.purchasing.supplier.repository.SupplierRepository;
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
public class SupplierWebService {

    private final SupplierRepository supplierRepository;

    public SupplierWebService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Transactional(readOnly = true)
    public List<SupplierListItemView> findAll(Long tenantId) {
        return supplierRepository.findAll(
                org.springframework.data.jpa.domain.Specification
                        .where((root, query, cb) -> cb.equal(root.get("tenantId"), tenantId)),
                Sort.by(Sort.Direction.ASC, "name")
        ).stream().map(this::toListItemView).toList();
    }

    @Transactional(readOnly = true)
    public SupplierDetailView getDetail(Long tenantId, Long id) {
        Supplier supplier = supplierRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Fornitore non trovato"));
        return toDetailView(supplier);
    }

    @Transactional(readOnly = true)
    public SupplierForm getForm(Long tenantId, Long id) {
        Supplier supplier = supplierRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Fornitore non trovato"));

        SupplierForm form = new SupplierForm();
        form.setCode(supplier.getCode());
        form.setName(supplier.getName());
        form.setVatNumber(supplier.getVatNumber());
        form.setTaxCode(supplier.getTaxCode());
        form.setEmail(supplier.getEmail());
        form.setPhone(supplier.getPhone());
        form.setAddressLine(supplier.getAddressLine());
        form.setCity(supplier.getCity());
        form.setPostalCode(supplier.getPostalCode());
        form.setProvince(supplier.getProvince());
        form.setCountryCode(supplier.getCountryCode());
        form.setNotes(supplier.getNotes());
        form.setActive(supplier.isActive());
        return form;
    }

    public Long create(Long tenantId, SupplierForm form) {
        String code = normalizeCode(form.getCode());

        if (supplierRepository.existsByTenantIdAndCode(tenantId, code)) {
            throw new BusinessException("Esiste già un fornitore con questo codice.");
        }

        Supplier supplier = new Supplier();
        supplier.setTenantId(tenantId);
        applyForm(supplier, form);
        supplier.setCode(code);

        return supplierRepository.save(supplier).getId();
    }

    public void update(Long tenantId, Long id, SupplierForm form) {
        Supplier supplier = supplierRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Fornitore non trovato"));

        String code = normalizeCode(form.getCode());
        supplierRepository.findByTenantIdAndCode(tenantId, code)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BusinessException("Esiste già un fornitore con questo codice.");
                });

        applyForm(supplier, form);
        supplier.setCode(code);

        supplierRepository.save(supplier);
    }

    private void applyForm(Supplier supplier, SupplierForm form) {
        supplier.setName(trim(form.getName()));
        supplier.setVatNumber(trim(form.getVatNumber()));
        supplier.setTaxCode(trim(form.getTaxCode()));
        supplier.setEmail(trim(form.getEmail()));
        supplier.setPhone(trim(form.getPhone()));
        supplier.setAddressLine(trim(form.getAddressLine()));
        supplier.setCity(trim(form.getCity()));
        supplier.setPostalCode(trim(form.getPostalCode()));
        supplier.setProvince(trim(form.getProvince()));
        supplier.setCountryCode(form.getCountryCode() != null ? form.getCountryCode().trim().toUpperCase(Locale.ROOT) : null);
        supplier.setNotes(trim(form.getNotes()));
        supplier.setActive(form.isActive());
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private SupplierListItemView toListItemView(Supplier supplier) {
        SupplierListItemView v = new SupplierListItemView();
        v.setId(supplier.getId());
        v.setCode(supplier.getCode());
        v.setName(supplier.getName());
        v.setVatNumber(supplier.getVatNumber());
        v.setEmail(supplier.getEmail());
        v.setPhone(supplier.getPhone());
        v.setActive(supplier.isActive());
        return v;
    }

    private SupplierDetailView toDetailView(Supplier supplier) {
        SupplierDetailView v = new SupplierDetailView();
        v.setId(supplier.getId());
        v.setCode(supplier.getCode());
        v.setName(supplier.getName());
        v.setVatNumber(supplier.getVatNumber());
        v.setTaxCode(supplier.getTaxCode());
        v.setEmail(supplier.getEmail());
        v.setPhone(supplier.getPhone());
        v.setAddressLine(supplier.getAddressLine());
        v.setCity(supplier.getCity());
        v.setPostalCode(supplier.getPostalCode());
        v.setProvince(supplier.getProvince());
        v.setCountryCode(supplier.getCountryCode());
        v.setNotes(supplier.getNotes());
        v.setActive(supplier.isActive());
        return v;
    }

    @Transactional(readOnly = true)
    public java.util.List<SupplierOptionView> findOptions(Long tenantId) {
        return supplierRepository.findByTenantIdAndActiveTrueOrderByNameAsc(tenantId)
                .stream()
                .map(s -> {
                    SupplierOptionView v = new SupplierOptionView();
                    v.setId(s.getId());
                    v.setCode(s.getCode());
                    v.setName(s.getName());
                    return v;
                })
                .toList();
    }

    public Page<SupplierListItemView> findPage(Long tenantId, int page, int size, String q, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("code")));
        Specification<Supplier> spec = Specification.where(byTenant(tenantId))
                .and(bySearch(q))
                .and(byStatus(status));
        return supplierRepository.findAll(spec, pageable).map(this::toListItemViewSupplier);
    }
    private Specification<Supplier> byTenant(Long tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
    }

    private Specification<Supplier> bySearch(String q) {
        if (q == null || q.trim().isEmpty()) {
            return null;
        }
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("code")), like),
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("vatNumber")), like),
                cb.like(cb.lower(root.get("email")), like)
        );

    }

    private Specification<Supplier> byStatus(String status) {
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
    private SupplierListItemView toListItemViewSupplier(Supplier supplier) {

        SupplierListItemView v = new SupplierListItemView();
        v.setId(supplier.getId());
        v.setCode(supplier.getCode());
        v.setName(supplier.getName());
        v.setVatNumber(supplier.getVatNumber());
        v.setEmail(supplier.getEmail());
        v.setActive(supplier.isActive());
        v.setPhone(supplier.getPhone());
        return v;

    }
}
