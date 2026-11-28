package com.gestiva.warehouse.inventory.valuation.repository;

import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InventoryLayerRepository extends JpaRepository<InventoryLayer, Long> {

    Optional<InventoryLayer> findByTenantIdAndId(Long tenantId, Long id);

    List<InventoryLayer> findByTenantIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(Long tenantId, Long itemId);

    List<InventoryLayer> findByTenantIdAndItemIdAndClosedFalseOrderByLayerDateDescIdDesc(Long tenantId, Long itemId);

    List<InventoryLayer> findByTenantIdAndSourceMovementIdOrderByIdAsc(Long tenantId, Long sourceMovementId);

    List<InventoryLayer> findByTenantIdAndClosedFalseOrderByItemIdAscLayerDateAscIdAsc(Long tenantId);


    // Gestiva V2 - Tutti gli strati FIFO aperti della società
    List<InventoryLayer>
    findByTenantIdAndCompanyIdAndClosedFalseOrderByItemIdAscLayerDateAscIdAsc(
            Long tenantId,
            Long companyId
    );

    // Gestiva V2 - Strati FIFO aperti di un articolo nella società
    List<InventoryLayer>
    findByTenantIdAndCompanyIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
            Long tenantId,
            Long companyId,
            Long itemId
    );

    // Gestiva V2 - Strati FIFO disponibili per società e magazzino
    List<InventoryLayer>
    findByTenantIdAndCompanyIdAndWarehouseIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long itemId
    );

    // Gestiva V2 - Strati generati da un movimento di carico
    List<InventoryLayer>
    findByTenantIdAndCompanyIdAndSourceMovementIdOrderByIdAsc(
            Long tenantId,
            Long companyId,
            Long sourceMovementId
    );

    // Gestiva V2 - Recupero di uno strato appartenente alla società
    Optional<InventoryLayer>
    findByTenantIdAndCompanyIdAndId(
            Long tenantId,
            Long companyId,
            Long id
    );

    /**
     * Articoli con layer FIFO nella società selezionata.
     * Include anche i layer completamente consumati e chiusi.
     */
    @Query("""
            select distinct l.itemId
            from InventoryLayer l
            where l.tenantId = :tenantId
              and l.companyId = :companyId
            """)
    List<Long> findDistinctItemIdsByTenantIdAndCompanyId(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId
    );


    /**
     * Verifica se un'ubicazione contiene ancora quantità FIFO residue.
     * Non considera i layer completamente consumati.
     */
    @Query("""
            select count(l)
            from InventoryLayer l
            where l.tenantId = :tenantId
              and l.companyId = :companyId
              and l.warehouseId = :warehouseId
              and l.locationId = :locationId
              and l.remainingQty > 0
            """)
    long countRemainingLayersByLocation(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("locationId") Long locationId
    );


    /**
     * Layer FIFO aperti nell'ubicazione sorgente.
     *
     * Da chiamare esclusivamente dentro una transazione,
     * dopo il lock del magazzino tenant/company.
     *
     * L'ordine FIFO e' layerDate ASC, id ASC.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select l
            from InventoryLayer l
            where l.tenantId = :tenantId
              and l.companyId = :companyId
              and l.warehouseId = :warehouseId
              and l.itemId = :itemId
              and (
                  l.locationId = :sourceLocationId
                  or (
                      :sourceLocationId is null
                      and l.locationId is null
                  )
              )
              and l.closed = false
            order by l.layerDate asc, l.id asc
            """)
    List<InventoryLayer> findTransferSourceLayersForUpdate(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("itemId") Long itemId,
            @Param("sourceLocationId") Long sourceLocationId
    );


    // =====================================================
    // GESTIVA V2 - STRATI FIFO PER LOTTO V14
    // =====================================================

    /**
     * Strati FIFO aperti con residuo positivo appartenenti
     * esclusivamente al contesto operativo richiesto.
     *
     * NULL locationId e NULL lotId sono valori esatti:
     * non rimuovono i rispettivi filtri.
     *
     * Lettura senza lock, destinata alla consultazione.
     */
    @Query("""
            select l
            from InventoryLayer l
            where l.tenantId = :tenantId
              and l.companyId = :companyId
              and l.warehouseId = :warehouseId
              and l.itemId = :itemId
              and (
                  l.locationId = :locationId
                  or (
                      :locationId is null
                      and l.locationId is null
                  )
              )
              and (
                  l.lotId = :lotId
                  or (
                      :lotId is null
                      and l.lotId is null
                  )
              )
              and l.closed = false
              and l.remainingQty > 0
            order by l.layerDate asc, l.id asc
            """)
    List<InventoryLayer> findOpenLayersByExactLotContext(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("locationId") Long locationId,
            @Param("itemId") Long itemId,
            @Param("lotId") Long lotId
    );

    /**
     * Strati FIFO del contesto esatto con lock pessimistico.
     *
     * Da invocare esclusivamente dentro una transazione,
     * dopo il lock del magazzino e nel rispetto del
     * protocollo applicativo di acquisizione dei lock.
     *
     * Non modifica gli strati selezionati.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select l
            from InventoryLayer l
            where l.tenantId = :tenantId
              and l.companyId = :companyId
              and l.warehouseId = :warehouseId
              and l.itemId = :itemId
              and (
                  l.locationId = :locationId
                  or (
                      :locationId is null
                      and l.locationId is null
                  )
              )
              and (
                  l.lotId = :lotId
                  or (
                      :lotId is null
                      and l.lotId is null
                  )
              )
              and l.closed = false
              and l.remainingQty > 0
            order by l.layerDate asc, l.id asc
            """)
    List<InventoryLayer> findOpenLayersByExactLotContextForUpdate(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("locationId") Long locationId,
            @Param("itemId") Long itemId,
            @Param("lotId") Long lotId
    );

    /**
     * Somma delle quantita residue nel contesto esatto.
     *
     * Include anche eventuali residui anomali su strati
     * chiusi, per non nascondere incoerenze durante
     * la riconciliazione con i movimenti.
     *
     * Non costituisce autonomamente una verifica
     * della disponibilita operativa.
     */
    @Query("""
            select coalesce(sum(l.remainingQty), 0)
            from InventoryLayer l
            where l.tenantId = :tenantId
              and l.companyId = :companyId
              and l.warehouseId = :warehouseId
              and l.itemId = :itemId
              and (
                  l.locationId = :locationId
                  or (
                      :locationId is null
                      and l.locationId is null
                  )
              )
              and (
                  l.lotId = :lotId
                  or (
                      :lotId is null
                      and l.lotId is null
                  )
              )
            """)
    BigDecimal sumRemainingQuantityByExactLotContext(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId,
            @Param("locationId") Long locationId,
            @Param("itemId") Long itemId,
            @Param("lotId") Long lotId
    );

    /**
     * Verifica dello storico degli strati FIFO del lotto
     * in tutte le societa del tenant.
     *
     * Include anche gli strati completamente consumati.
     */
    boolean existsByTenantIdAndItemIdAndLotId(
            Long tenantId,
            Long itemId,
            Long lotId
    );

    /**
     * Verifica delle quantita residue del lotto
     * indipendentemente da societa, magazzino e ubicazione.
     *
     * Per la protezione anagrafica utilizzare
     * BigDecimal.ZERO come soglia.
     *
     * Non esclude gli strati chiusi con residui anomali.
     * Non sostituisce le verifiche di coerenza dei saldi.
     */
    boolean existsByTenantIdAndItemIdAndLotIdAndRemainingQtyGreaterThan(
            Long tenantId,
            Long itemId,
            Long lotId,
            BigDecimal remainingQty
    );
}