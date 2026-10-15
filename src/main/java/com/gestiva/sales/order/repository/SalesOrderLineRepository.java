package com.gestiva.sales.order.repository;

import com.gestiva.sales.order.entity.SalesOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderLineRepository
        extends JpaRepository<SalesOrderLine, Long> {

    // Metodi legacy: mantenuti temporaneamente per compatibilità

    List<SalesOrderLine> findByTenantIdAndSalesOrderIdOrderByLineNoAsc(
            Long tenantId,
            Long salesOrderId
    );

    void deleteByTenantIdAndSalesOrderId(
            Long tenantId,
            Long salesOrderId
    );

    // Metodi V2: gestione multi-società

    List<SalesOrderLine>
    findByTenantIdAndCompanyIdAndSalesOrderIdOrderByLineNoAsc(
            Long tenantId,
            Long companyId,
            Long salesOrderId
    );

    void deleteByTenantIdAndCompanyIdAndSalesOrderId(
            Long tenantId,
            Long companyId,
            Long salesOrderId
    );

    List<SalesOrderLine>
    findByTenantIdAndCompanyIdAndItemId(
            Long tenantId,
            Long companyId,
            Long itemId
    );
}