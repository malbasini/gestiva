package com.gestiva.logistics.ddt.repository;

import com.gestiva.logistics.ddt.entity.DeliveryNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface DeliveryNoteRepository extends JpaRepository<DeliveryNote, Long>, JpaSpecificationExecutor<DeliveryNote> {
    Optional<DeliveryNote> findByTenantIdAndSalesOrderId(Long tenantId, Long salesOrderId);
    Optional<DeliveryNote> findByTenantIdAndId(Long tenantId, Long id);
    boolean existsByTenantIdAndSalesOrderId(Long tenantId, Long salesOrderId);
    long countByTenantId(Long tenantId);
    long countByTenantIdAndStatusIn(Long tenantId, java.util.Collection<String> statuses);


    @Query("""
    select count(d)
    from DeliveryNote d
    where d.tenantId = :tenantId
      and d.status in :statuses
      and not exists (
          select 1
          from Invoice i
          where i.tenantId = d.tenantId
            and i.deliveryNoteId = d.id
      )
""")
   
    long countToInvoiceByTenantIdAndStatusIn(@Param("tenantId") Long tenantId,
                                             @Param("statuses") java.util.Collection<String> statuses);



    // =====================================================
    // METODI V2 - MULTI-SOCIETÀ
    // =====================================================

    Optional<DeliveryNote> findByTenantIdAndCompanyIdAndId(
            Long tenantId,
            Long companyId,
            Long id
    );

    Optional<DeliveryNote> findByTenantIdAndCompanyIdAndSalesOrderId(
            Long tenantId,
            Long companyId,
            Long salesOrderId
    );

    boolean existsByTenantIdAndCompanyIdAndSalesOrderId(
            Long tenantId,
            Long companyId,
            Long salesOrderId
    );

    long countByTenantIdAndCompanyId(
            Long tenantId,
            Long companyId
    );

    long countByTenantIdAndCompanyIdAndStatusIn(
            Long tenantId,
            Long companyId,
            java.util.Collection<String> statuses
    );

    @Query("""
    select count(d)
    from DeliveryNote d
    where d.tenantId = :tenantId
      and d.companyId = :companyId
      and d.status in :statuses
      and not exists (
          select 1
          from Invoice i
          where i.tenantId = d.tenantId
            and i.companyId = d.companyId
            and i.deliveryNoteId = d.id
      )
    """)
    long countToInvoiceByTenantIdAndCompanyIdAndStatusIn(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("statuses") java.util.Collection<String> statuses
    );
}