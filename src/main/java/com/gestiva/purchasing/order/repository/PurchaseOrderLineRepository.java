package com.gestiva.purchasing.order.repository;

import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PurchaseOrderLineRepository extends JpaRepository<PurchaseOrderLine, Long> {

    List<PurchaseOrderLine> findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(Long tenantId, Long purchaseOrderId);

    void deleteByTenantIdAndPurchaseOrderId(Long tenantId, Long purchaseOrderId);
}