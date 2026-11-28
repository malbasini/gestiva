package com.gestiva.logistics.ddt.repository;

import com.gestiva.logistics.ddt.entity.DeliveryNoteLineLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Gestiva V2 - Allocazioni per lotto delle righe DDT.
 *
 * Le interrogazioni societarie verificano il contesto
 * dell'allocazione, della riga commerciale e della
 * testata del documento di trasporto.
 *
 * Le quantita sono espresse nell'UOM base.
 * I costi non sono duplicati nelle allocazioni DDT.
 *
 * La persistenza delle allocazioni deve avvenire
 * esclusivamente nel flusso applicativo autorizzato.
 */
public interface DeliveryNoteLineLotRepository
        extends JpaRepository<DeliveryNoteLineLot, Long> {

    /**
     * Allocazioni di una riga DDT autorizzata.
     * Include le allocazioni con movementId ancora NULL.
     */
    @Query("""
            select a
            from DeliveryNoteLineLot a
            where a.tenantId = :tenantId
              and a.companyId = :companyId
              and a.deliveryNoteLineId = :deliveryNoteLineId
              and exists (
                  select line.id
                  from DeliveryNoteLine line, DeliveryNote document
                  where line.id = a.deliveryNoteLineId
                    and line.tenantId = a.tenantId
                    and line.companyId = a.companyId
                    and line.itemId = a.itemId
                    and document.id = line.deliveryNoteId
                    and document.tenantId = a.tenantId
                    and document.companyId = a.companyId
              )
            order by a.id asc
            """)
    List<DeliveryNoteLineLot> findAllocationsByDeliveryLine(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("deliveryNoteLineId") Long deliveryNoteLineId
    );

    /**
     * Storico societario delle allocazioni per articolo
     * e lotto, indipendentemente dallo stato del DDT
     * e dal movimento associato.
     */
    @Query("""
            select a
            from DeliveryNoteLineLot a
            where a.tenantId = :tenantId
              and a.companyId = :companyId
              and a.itemId = :itemId
              and a.lotId = :lotId
              and exists (
                  select line.id
                  from DeliveryNoteLine line, DeliveryNote document
                  where line.id = a.deliveryNoteLineId
                    and line.tenantId = a.tenantId
                    and line.companyId = a.companyId
                    and line.itemId = a.itemId
                    and document.id = line.deliveryNoteId
                    and document.tenantId = a.tenantId
                    and document.companyId = a.companyId
              )
            order by a.id asc
            """)
    List<DeliveryNoteLineLot> findAllocationsByItemAndLot(
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
            from DeliveryNoteLineLot a
            where a.tenantId = :tenantId
              and a.companyId = :companyId
              and a.movementId = :movementId
              and exists (
                  select line.id
                  from DeliveryNoteLine line, DeliveryNote document
                  where line.id = a.deliveryNoteLineId
                    and line.tenantId = a.tenantId
                    and line.companyId = a.companyId
                    and line.itemId = a.itemId
                    and document.id = line.deliveryNoteId
                    and document.tenantId = a.tenantId
                    and document.companyId = a.companyId
              )
            """)
    Optional<DeliveryNoteLineLot> findAllocationByMovementId(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("movementId") Long movementId
    );

    /**
     * Verifica conservativa dello storico documentale
     * del lotto in tutte le societa del tenant.
     *
     * Non esclude eventuali allocazioni incoerenti,
     * non associate a movimenti o relative a DDT annullati.
     */
    boolean existsByTenantIdAndItemIdAndLotId(
            Long tenantId,
            Long itemId,
            Long lotId
    );

    /**
     * Quantita complessiva allocata alla riga DDT.
     *
     * Restituisce zero in assenza di allocazioni
     * corrispondenti al contesto richiesto.
     *
     * Non applica arrotondamenti e non ricalcola costi.
     *
     * Non sostituisce la verifica di esistenza della
     * riga, della sua autorizzazione o di eventuali
     * allocazioni documentali incoerenti.
     *
     * Non costituisce una verifica di disponibilita.
     */
    @Query("""
            select coalesce(sum(a.quantity), 0)
            from DeliveryNoteLineLot a
            where a.tenantId = :tenantId
              and a.companyId = :companyId
              and a.deliveryNoteLineId = :deliveryNoteLineId
              and exists (
                  select line.id
                  from DeliveryNoteLine line, DeliveryNote document
                  where line.id = a.deliveryNoteLineId
                    and line.tenantId = a.tenantId
                    and line.companyId = a.companyId
                    and line.itemId = a.itemId
                    and document.id = line.deliveryNoteId
                    and document.tenantId = a.tenantId
                    and document.companyId = a.companyId
              )
            """)
    BigDecimal sumAllocatedQuantityByDeliveryLine(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("deliveryNoteLineId") Long deliveryNoteLineId
    );
}
