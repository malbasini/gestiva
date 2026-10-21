package com.gestiva.purchasing.order.repository;

import com.gestiva.purchasing.order.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long>, JpaSpecificationExecutor<PurchaseOrder> {

    Optional<PurchaseOrder> findByTenantIdAndId(Long tenantId, Long id);
    boolean existsByTenantIdAndOrderNumber(Long tenantId, String orderNumber);
    long countByTenantIdAndStatusIn(Long tenantId, java.util.Collection<String> statuses);

    // =====================================================
    // METODI V2 - MULTI-SOCIETÀ
    // =====================================================

    Optional<PurchaseOrder> findByTenantIdAndCompanyIdAndId(
            Long tenantId,
            Long companyId,
            Long id
    );

    boolean existsByTenantIdAndCompanyIdAndOrderNumber(
            Long tenantId,
            Long companyId,
            String orderNumber
    );

    long countByTenantIdAndCompanyIdAndStatusIn(
            Long tenantId,
            Long companyId,
            java.util.Collection<String> statuses
    );
}