package com.gestiva.purchasing.order.repository;

import com.gestiva.purchasing.order.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long>, JpaSpecificationExecutor<PurchaseOrder> {

    Optional<PurchaseOrder> findByTenantIdAndId(Long tenantId, Long id);

    boolean existsByTenantIdAndOrderNumber(Long tenantId, String orderNumber);
}