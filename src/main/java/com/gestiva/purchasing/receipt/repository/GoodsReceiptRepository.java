package com.gestiva.purchasing.receipt.repository;

import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long>, JpaSpecificationExecutor<GoodsReceipt> {
    Optional<GoodsReceipt> findByTenantIdAndId(Long tenantId, Long id);
    boolean existsByTenantIdAndReceiptNumber(Long tenantId, String receiptNumber);
    boolean existsByTenantIdAndPurchaseOrderId(Long tenantId, Long purchaseOrderId);
    java.util.Optional<com.gestiva.purchasing.receipt.entity.GoodsReceipt> findFirstByTenantIdAndPurchaseOrderId(Long tenantId, Long purchaseOrderId);

    @Query("""
    select count(gr)
    from GoodsReceipt gr
    where gr.tenantId = :tenantId
      and not exists (
          select 1
          from SupplierInvoice si
          where si.tenantId = gr.tenantId
            and si.goodsReceiptId = gr.id
      )
""")
    long countToInvoiceByTenantId(@Param("tenantId") Long tenantId);


}
