package com.gestiva.purchasing.receipt.repository;

import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GoodsReceiptLineRepository extends JpaRepository<GoodsReceiptLine, Long> {

    // Gestiva V2 — Somma delle quantità ricevute
    // per una riga ordine, isolata per tenant e società.
    // Le quantità sono già espresse nell'UOM base.
    @Query("""
        select sum(line.quantityReceived)
        from GoodsReceiptLine line
        join line.goodsReceipt receipt
        where line.tenantId = :tenantId
          and receipt.tenantId = :tenantId
          and receipt.companyId = :companyId
          and receipt.purchaseOrderId = :purchaseOrderId
          and line.purchaseOrderLineId = :purchaseOrderLineId
        """)
    java.math.BigDecimal sumReceivedBaseQuantityByOrderLine(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("purchaseOrderId") Long purchaseOrderId,
            @Param("purchaseOrderLineId") Long purchaseOrderLineId
    );





    /**
     * Quantità ricevuta nei documenti precedenti.
     *
     * L'ID corrente è escluso dal totale.
     * La query è isolata per tenant, società, ordine e riga.
     *
     * Restituisce NULL quando non esistono righe precedenti.
     */
    @Query("""
        select sum(line.quantityReceived)
        from GoodsReceiptLine line
        join line.goodsReceipt receipt
        where line.tenantId = :tenantId
          and receipt.tenantId = :tenantId
          and receipt.companyId = :companyId
          and receipt.purchaseOrderId = :purchaseOrderId
          and line.purchaseOrderLineId = :purchaseOrderLineId
          and receipt.id < :currentGoodsReceiptId
        """)
    java.math.BigDecimal sumReceivedBeforeReceiptByOrderLine(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("purchaseOrderId") Long purchaseOrderId,
            @Param("purchaseOrderLineId") Long purchaseOrderLineId,
            @Param("currentGoodsReceiptId") Long currentGoodsReceiptId
    );

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
