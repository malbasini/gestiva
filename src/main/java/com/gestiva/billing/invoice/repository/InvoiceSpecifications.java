package com.gestiva.billing.invoice.repository;

import com.gestiva.billing.invoice.entity.Invoice;
import org.springframework.data.jpa.domain.Specification;

public final class InvoiceSpecifications {

    private InvoiceSpecifications() {
    }

    public static Specification<Invoice> hasTenantId(Long tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
    }

    public static Specification<Invoice> hasStatus(String status) {
        return (root, query, cb) ->
                status == null || status.isBlank()
                        ? cb.conjunction()
                        : cb.equal(root.get("status"), status);
    }

    public static Specification<Invoice> hasCustomerId(Long customerId) {
        return (root, query, cb) ->
                customerId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("customerId"), customerId);
    }

    public static Specification<Invoice> hasDeliveryNoteId(Long deliveryNoteId) {
        return (root, query, cb) ->
                deliveryNoteId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("deliveryNoteId"), deliveryNoteId);
    }

    public static Specification<Invoice> matchesSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            String like = "%" + search.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("invoiceNumber")), like);
        };
    }
}