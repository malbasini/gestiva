package com.gestiva.logistics.ddt.repository;

import com.gestiva.logistics.ddt.entity.DeliveryNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DeliveryNoteRepository extends JpaRepository<DeliveryNote, Long>, JpaSpecificationExecutor<DeliveryNote> {
    Optional<DeliveryNote> findByTenantIdAndSalesOrderId(Long tenantId, Long salesOrderId);
    Optional<DeliveryNote> findByTenantIdAndId(Long tenantId, Long id);
    boolean existsByTenantIdAndSalesOrderId(Long tenantId, Long salesOrderId);
    long countByTenantId(Long tenantId);
}