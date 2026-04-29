package com.gestiva.sales.order.repository;

import com.gestiva.sales.order.entity.SalesOrder;
import org.springframework.data.jpa.domain.Specification;

public final class SalesOrderSpecifications {

    private SalesOrderSpecifications() {
    }

    public static Specification<SalesOrder> hasTenantId(Long tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
    }

    public static Specification<SalesOrder> hasStatus(String status) {
        return (root, query, cb) ->
                status == null || status.isBlank()
                        ? cb.conjunction()
                        : cb.equal(root.get("status"), status);
    }

    public static Specification<SalesOrder> hasCustomerId(Long customerId) {
        return (root, query, cb) ->
                customerId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("customerId"), customerId);
    }

    public static Specification<SalesOrder> hasQuoteId(Long quoteId) {
        return (root, query, cb) ->
                quoteId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("quoteId"), quoteId);
    }

    public static Specification<SalesOrder> matchesSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            String like = "%" + search.trim().toLowerCase() + "%";

            return cb.like(cb.lower(root.get("orderNumber")), like);
        };
    }
}