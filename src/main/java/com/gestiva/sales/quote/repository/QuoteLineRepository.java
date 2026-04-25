package com.gestiva.sales.quote.repository;

import com.gestiva.sales.quote.entity.QuoteLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteLineRepository extends JpaRepository<QuoteLine, Long> {

    List<QuoteLine> findByTenantIdAndQuoteIdOrderByLineNoAsc(Long tenantId, Long quoteId);

    void deleteByTenantIdAndQuoteId(Long tenantId, Long quoteId);
}