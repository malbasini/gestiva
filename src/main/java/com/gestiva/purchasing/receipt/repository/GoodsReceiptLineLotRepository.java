package com.gestiva.purchasing.receipt.repository;

import com.gestiva.purchasing.receipt.entity.GoodsReceiptLineLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Gestiva V2 - Allocazioni per lotto delle righe GR.
 *
 * Le interrogazioni societarie verificano il contesto
 * dell'allocazione, della riga commerciale e della
 * testata del ricevimento.
 *
 * Le quantita sono espresse nell'UOM base.
 * La persistenza delle allocazioni deve avvenire
 * esclusivamente nel flusso applicativo autorizzato.
 */
public interface GoodsReceiptLineLotRepository
        extends JpaRepository<GoodsReceiptLineLot, Long> {

    /**
     * Allocazioni di una riga GR autorizzata.
     * Include le allocazioni con movementId ancora NULL.
     */
    @Query("""
            select a
            from GoodsReceiptLineLot a
            where a.tenantId = :tenantId
              and a.companyId = :companyId
              and a.goodsReceiptLineId = :goodsReceiptLineId
              and exists (
                  select line.id
                  from GoodsReceiptLine line
                  join line.goodsReceipt receipt
                  where line.id = a.goodsReceiptLineId
                    and line.tenantId = a.tenantId
                    and line.itemId = a.itemId
                    and receipt.tenantId = a.tenantId
                    and receipt.companyId = a.companyId
              )
            order by a.id asc
            """)
    List<GoodsReceiptLineLot> findAllocationsByReceiptLine(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("goodsReceiptLineId") Long goodsReceiptLineId
    );

    /**
     * Storico societario delle allocazioni per articolo
     * e lotto, indipendentemente dal movimento associato.
     */
    @Query("""
            select a
            from GoodsReceiptLineLot a
            where a.tenantId = :tenantId
              and a.companyId = :companyId
              and a.itemId = :itemId
              and a.lotId = :lotId
              and exists (
                  select line.id
                  from GoodsReceiptLine line
                  join line.goodsReceipt receipt
                  where line.id = a.goodsReceiptLineId
                    and line.tenantId = a.tenantId
                    and line.itemId = a.itemId
                    and receipt.tenantId = a.tenantId
                    and receipt.companyId = a.companyId
              )
            order by a.id asc
            """)
    List<GoodsReceiptLineLot> findAllocationsByItemAndLot(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("itemId") Long itemId,
            @Param("lotId") Long lotId
    );

    /**
     * Allocazione associata a un movimento specifico.
     *
     * NULL movementId non restituisce alcuna allocazione.
     * L'unicita del movimento e' protetta dalla V14.
     *
     * Il controllo semantico del movimento associato
     * rimane responsabilita del servizio applicativo.
     */
    @Query("""
            select a
            from GoodsReceiptLineLot a
            where a.tenantId = :tenantId
              and a.companyId = :companyId
              and a.movementId = :movementId
              and exists (
                  select line.id
                  from GoodsReceiptLine line
                  join line.goodsReceipt receipt
                  where line.id = a.goodsReceiptLineId
                    and line.tenantId = a.tenantId
                    and line.itemId = a.itemId
                    and receipt.tenantId = a.tenantId
                    and receipt.companyId = a.companyId
              )
            """)
    Optional<GoodsReceiptLineLot> findAllocationByMovementId(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("movementId") Long movementId
    );

    /**
     * Verifica conservativa dello storico documentale
     * del lotto in tutte le societa del tenant.
     *
     * Non esclude eventuali allocazioni incoerenti
     * o non ancora associate a un movimento.
     */
    boolean existsByTenantIdAndItemIdAndLotId(
            Long tenantId,
            Long itemId,
            Long lotId
    );

    /**
     * Totali delle allocazioni documentali valide
     * rispetto al contesto richiesto.
     */
    interface ReceiptLineLotTotals {

        BigDecimal getTotalQuantity();

        BigDecimal getTotalCost();
    }

    /**
     * Quantita e costo complessivo allocati alla riga.
     *
     * Non ricalcola i costi unitari.
     * Non applica arrotondamenti.
     *
     * Restituisce zero per entrambi i totali in assenza
     * di allocazioni corrispondenti.
     *
     * Non sostituisce la verifica di esistenza della
     * riga, della sua autorizzazione o di eventuali
     * allocazioni documentali incoerenti.
     */
    @Query("""
            select
                coalesce(sum(a.quantity), 0) as totalQuantity,
                coalesce(sum(a.totalCost), 0) as totalCost
            from GoodsReceiptLineLot a
            where a.tenantId = :tenantId
              and a.companyId = :companyId
              and a.goodsReceiptLineId = :goodsReceiptLineId
              and exists (
                  select line.id
                  from GoodsReceiptLine line
                  join line.goodsReceipt receipt
                  where line.id = a.goodsReceiptLineId
                    and line.tenantId = a.tenantId
                    and line.itemId = a.itemId
                    and receipt.tenantId = a.tenantId
                    and receipt.companyId = a.companyId
              )
            """)
    ReceiptLineLotTotals summarizeAllocationsByReceiptLine(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("goodsReceiptLineId") Long goodsReceiptLineId
    );
}
