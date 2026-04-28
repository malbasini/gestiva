package com.gestiva.sales.quote.repository;

import com.gestiva.sales.quote.entity.Quote;
import org.springframework.data.jpa.domain.Specification;

public final class QuoteSpecifications {

    private QuoteSpecifications() {
    }

    public static Specification<Quote> hasTenantId(Long tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
    }

    public static Specification<Quote> hasStatus(String status) {
        return (root, query, cb) ->
                status == null || status.isBlank()
                        ? cb.conjunction()
                        : cb.equal(root.get("status"), status);
    }

    public static Specification<Quote> hasCustomerId(Long customerId) {
        return (root, query, cb) ->
                customerId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("customerId"), customerId);
    }

    public static Specification<Quote> matchesSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            String like = "%" + search.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("quoteNumber")), like),
                    cb.like(cb.lower(root.get("notes")), like)
            );
        };
    }
}