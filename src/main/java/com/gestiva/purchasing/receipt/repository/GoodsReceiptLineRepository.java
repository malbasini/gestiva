package com.gestiva.purchasing.receipt.repository;

import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GoodsReceiptLineRepository extends JpaRepository<GoodsReceiptLine, Long> {

    List<GoodsReceiptLine> findByTenantIdAndGoodsReceiptIdOrderByLineNoAsc(Long tenantId, Long goodsReceiptId);
}
