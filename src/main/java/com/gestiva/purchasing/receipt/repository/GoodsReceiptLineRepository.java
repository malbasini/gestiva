package com.gestiva.purchasing.receipt.repository;

import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GoodsReceiptLineRepository extends JpaRepository<GoodsReceiptLine, Long> {

    @Query("""
       select j
       from GoodsReceiptLine j
       where j.tenantId = :tenantId
       and j.goodsReceiptId = :id
       order by j.lineNo asc
       """)
    List<GoodsReceiptLine> findByTenantIdAndGoodsReceiptIdOrderByLineNoAsc(@Param("tenantId") Long tenantId,
                                                                           @Param("id") Long id);

    List<GoodsReceiptLine> findByTenantIdAndGoodsReceipt_Id(Long tenantId, Long id);
}
