package com.gestiva.sales.quote.repository;

import com.gestiva.sales.quote.entity.QuoteLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteLineRepository extends JpaRepository<QuoteLine, Long> {

    // =====================================================
    // METODI LEGACY - DA MIGRARE
    // =====================================================

    List<QuoteLine> findByTenantIdAndQuoteIdOrderByLineNoAsc(
            Long tenantId,
            Long quoteId
    );

    void deleteByTenantIdAndQuoteId(
            Long tenantId,
            Long quoteId
    );

    boolean existsByTenantIdAndQuoteId(
            Long tenantId,
            Long quoteId
    );

    // =====================================================
    // METODI V2 - MULTI-SOCIETÀ
    // =====================================================

    List<QuoteLine> findByTenantIdAndCompanyIdAndQuoteIdOrderByLineNoAsc(
            Long tenantId,
            Long companyId,
            Long quoteId
    );

    void deleteByTenantIdAndCompanyIdAndQuoteId(
            Long tenantId,
            Long companyId,
            Long quoteId
    );

    boolean existsByTenantIdAndCompanyIdAndQuoteId(
            Long tenantId,
            Long companyId,
            Long quoteId
    );

    List<QuoteLine> findByTenantIdAndCompanyIdAndItemId(
            Long tenantId,
            Long companyId,
            Long itemId
    );
}