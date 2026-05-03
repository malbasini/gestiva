package com.gestiva.logistics.ddt.repository;

import com.gestiva.logistics.ddt.entity.DeliveryNote;
import org.springframework.data.jpa.domain.Specification;

public final class DeliveryNoteSpecifications {

    private DeliveryNoteSpecifications() {
    }

    public static Specification<DeliveryNote> hasTenantId(Long tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
    }

    public static Specification<DeliveryNote> hasStatus(String status) {
        return (root, query, cb) ->
                status == null || status.isBlank()
                        ? cb.conjunction()
                        : cb.equal(root.get("status"), status);
    }

    public static Specification<DeliveryNote> hasCustomerId(Long customerId) {
        return (root, query, cb) ->
                customerId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("customerId"), customerId);
    }

    public static Specification<DeliveryNote> hasSalesOrderId(Long salesOrderId) {
        return (root, query, cb) ->
                salesOrderId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("salesOrderId"), salesOrderId);
    }

    public static Specification<DeliveryNote> matchesSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            String like = "%" + search.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("ddtNumber")), like),
                    cb.like(cb.lower(root.get("transportReason")), like),
                    cb.like(cb.lower(root.get("carrierName")), like)
            );
        };
    }
}