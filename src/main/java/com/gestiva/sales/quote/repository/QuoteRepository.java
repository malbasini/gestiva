package com.gestiva.sales.quote.repository;

import com.gestiva.sales.quote.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    List<Quote> findByTenantIdOrderByQuoteDateDescIdDesc(Long tenantId);

    Optional<Quote> findByTenantIdAndId(Long tenantId, Long id);
}