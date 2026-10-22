package com.gestiva.purchasing.order.repository;

import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderLineRepository extends JpaRepository<PurchaseOrderLine, Long> {

    List<PurchaseOrderLine> findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(Long tenantId, Long purchaseOrderId);

    Optional<PurchaseOrderLine> findByTenantIdAndPurchaseOrderIdAndId(
            Long tenantId,
            Long purchaseOrderId,
            Long id
    );

    void deleteByTenantIdAndPurchaseOrderId(Long tenantId, Long purchaseOrderId);
}