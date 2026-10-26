package com.gestiva.purchasing.order.repository;

import com.gestiva.purchasing.order.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Recupera l'ordine con blocco pessimista di scrittura.
     *
     * Da utilizzare all'interno di una transazione
     * per serializzare le modifiche allo stesso ordine.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select po
            from PurchaseOrder po
            where po.tenantId = :tenantId
              and po.companyId = :companyId
              and po.id = :id
            """)
    Optional<PurchaseOrder> findLockedByTenantIdAndCompanyIdAndId(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("id") Long id
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