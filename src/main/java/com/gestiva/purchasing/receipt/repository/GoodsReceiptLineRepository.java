package com.gestiva.purchasing.receipt.repository;

import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GoodsReceiptLineRepository extends JpaRepository<GoodsReceiptLine, Long> {

    @Query("""
       select j
       from GoodsReceiptLine j
       where j.tenantId = :tenantId
       and j.id = :id
       order by j.id asc
       """)
    List<GoodsReceiptLine> findByTenantIdAndGoodsReceiptIdOrderByLineNoAsc(Long tenantId, Long id);
}
