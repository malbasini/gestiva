package com.gestiva.sales.order.repository;

import com.gestiva.sales.order.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long>, JpaSpecificationExecutor<SalesOrder> {
    List<SalesOrder> findByTenantIdOrderByOrderDateDescIdDesc(Long tenantId);
    Optional<SalesOrder> findByTenantIdAndId(Long tenantId, Long id);
    boolean existsByTenantIdAndQuoteId(Long tenantId, Long quoteId);
}