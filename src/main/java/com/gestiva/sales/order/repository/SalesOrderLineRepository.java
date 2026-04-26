package com.gestiva.sales.order.repository;

import com.gestiva.sales.order.entity.SalesOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine, Long> {

    List<SalesOrderLine> findByTenantIdAndSalesOrderIdOrderByLineNoAsc(Long tenantId, Long salesOrderId);

    void deleteByTenantIdAndSalesOrderId(Long tenantId, Long salesOrderId);
}