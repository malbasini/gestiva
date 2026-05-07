package com.gestiva.purchasing.receipt.repository;

import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long>, JpaSpecificationExecutor<GoodsReceipt> {

    Optional<GoodsReceipt> findByTenantIdAndId(Long tenantId, Long id);

    boolean existsByTenantIdAndReceiptNumber(Long tenantId, String receiptNumber);

    boolean existsByTenantIdAndPurchaseOrderId(Long tenantId, Long purchaseOrderId);
}
