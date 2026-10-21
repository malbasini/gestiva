package com.gestiva.purchasing.receipt.repository;

import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long>, JpaSpecificationExecutor<GoodsReceipt> {
    Optional<GoodsReceipt> findByTenantIdAndId(Long tenantId, Long id);

    // Gestiva V2 - Ricerche isolate per tenant e società
    Optional<GoodsReceipt> findByTenantIdAndCompanyIdAndId(
            Long tenantId,
            Long companyId,
            Long id
    );

    boolean existsByTenantIdAndCompanyIdAndReceiptNumber(
            Long tenantId,
            Long companyId,
            String receiptNumber
    );

    boolean existsByTenantIdAndCompanyIdAndPurchaseOrderId(
            Long tenantId,
            Long companyId,
            Long purchaseOrderId
    );

    Optional<GoodsReceipt> findFirstByTenantIdAndCompanyIdAndPurchaseOrderId(
            Long tenantId,
            Long companyId,
            Long purchaseOrderId
    );
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

    // =====================================================
    // GESTIVA V2 - RICEVIMENTI DA FATTURARE PER SOCIETÀ
    // =====================================================

    @Query("""
        select count(gr)
        from GoodsReceipt gr
        where gr.tenantId = :tenantId
          and gr.companyId = :companyId
          and not exists (
              select 1
              from SupplierInvoice si
              where si.tenantId = gr.tenantId
                and si.companyId = gr.companyId
                and si.goodsReceiptId = gr.id
          )
        """)
    long countToInvoiceByTenantIdAndCompanyId(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId
    );


}
