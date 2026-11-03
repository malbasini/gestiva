package com.gestiva.purchasing.receipt.repository;

import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GoodsReceiptLineRepository extends JpaRepository<GoodsReceiptLine, Long> {


    List<GoodsReceiptLine> findByTenantIdAndGoodsReceipt_Id(Long tenantId, Long id);

    // Gestiva V2 — righe isolate tramite la società del documento padre.
    @Query("""
        select line
        from GoodsReceiptLine line
        join line.goodsReceipt receipt
        where line.tenantId = :tenantId
          and receipt.tenantId = :tenantId
          and receipt.companyId = :companyId
          and receipt.id = :goodsReceiptId
        order by line.lineNo asc
        """)
    List<GoodsReceiptLine> findByTenantIdAndCompanyIdAndGoodsReceiptIdOrderByLineNoAsc(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("goodsReceiptId") Long goodsReceiptId
    );


    boolean existsByTenantIdAndItemId(Long tenantId, Long itemId);

    // Gestiva V2 - Protezione delle righe degli ordini fornitori
    // già collegate a documenti di ricevimento merci.
    boolean existsByTenantIdAndPurchaseOrderLineId(
            Long tenantId,
            Long purchaseOrderLineId
    );

}
