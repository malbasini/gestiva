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
    long countByTenantId(Long tenantId);
    java.util.List<com.gestiva.sales.order.entity.SalesOrder> findTop5ByTenantIdOrderByOrderDateDescIdDesc(Long tenantId);
    long countByTenantIdAndStatusIn(Long tenantId, java.util.Collection<String> statuses);

    // =====================================================
    // METODI V2 - MULTI-SOCIETÀ
    // =====================================================

    List<SalesOrder> findByTenantIdAndCompanyIdOrderByOrderDateDescIdDesc(
            Long tenantId,
            Long companyId
    );

    Optional<SalesOrder> findByTenantIdAndCompanyIdAndId(
            Long tenantId,
            Long companyId,
            Long id
    );

    boolean existsByTenantIdAndCompanyIdAndQuoteId(
            Long tenantId,
            Long companyId,
            Long quoteId
    );

    long countByTenantIdAndCompanyId(
            Long tenantId,
            Long companyId
    );

    List<SalesOrder> findTop5ByTenantIdAndCompanyIdOrderByOrderDateDescIdDesc(
            Long tenantId,
            Long companyId
    );

    long countByTenantIdAndCompanyIdAndStatusIn(
            Long tenantId,
            Long companyId,
            java.util.Collection<String> statuses
    );

    boolean existsByTenantIdAndCompanyIdAndOrderNumber(
            Long tenantId,
            Long companyId,
            String orderNumber
    );

    List<SalesOrder> findByTenantIdAndCompanyIdAndCustomerIdOrderByOrderDateDescIdDesc(
            Long tenantId,
            Long companyId,
            Long customerId
    );
}