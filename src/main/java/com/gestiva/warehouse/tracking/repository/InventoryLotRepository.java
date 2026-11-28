package com.gestiva.warehouse.tracking.repository;

import com.gestiva.warehouse.tracking.entity.InventoryLot;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InventoryLotRepository extends JpaRepository<InventoryLot, Long> {

    Optional<InventoryLot> findByTenantIdAndId(
            Long tenantId,
            Long id
    );

    Optional<InventoryLot> findByTenantIdAndItemIdAndId(
            Long tenantId,
            Long itemId,
            Long id
    );

    Optional<InventoryLot> findByTenantIdAndItemIdAndLotCode(
            Long tenantId,
            Long itemId,
            String lotCode
    );

    boolean existsByTenantIdAndItemIdAndLotCode(
            Long tenantId,
            Long itemId,
            String lotCode
    );

    List<InventoryLot> findByTenantIdAndItemIdOrderByLotCodeAsc(
            Long tenantId,
            Long itemId
    );

    List<InventoryLot> findByTenantIdAndItemIdAndActiveTrueOrderByLotCodeAsc(
            Long tenantId,
            Long itemId
    );

    List<InventoryLot> findByTenantIdAndExpirationDateBeforeAndActiveTrueOrderByExpirationDateAsc(
            Long tenantId,
            LocalDate expirationDate
    );


    // =====================================================
    // GESTIVA V2 - CONTRATTI ANAGRAFICI LOTTI V14
    // =====================================================

    /**
     * Recupera e blocca un lotto identificato dal contesto
     * anagrafico completo.
     *
     * Include anche i lotti inattivi, necessari per le
     * verifiche anagrafiche e le operazioni consentite.
     *
     * Da invocare esclusivamente dentro una transazione,
     * rispettando il protocollo di lock applicativo.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select l
            from InventoryLot l
            where l.tenantId = :tenantId
              and l.item.id = :itemId
              and l.id = :lotId
            """)
    Optional<InventoryLot> findByTenantIdAndItemIdAndIdForUpdate(
            @Param("tenantId") Long tenantId,
            @Param("itemId") Long itemId,
            @Param("lotId") Long lotId
    );

    /**
     * Recupera un lotto attivo appartenente all'articolo.
     * Non verifica disponibilita, produzione o scadenza.
     */
    Optional<InventoryLot> findByTenantIdAndItemIdAndIdAndActiveTrue(
            Long tenantId,
            Long itemId,
            Long id
    );

    /**
     * Candidati anagrafici attivi e non scaduti.
     *
     * La data di riferimento e' inclusiva e obbligatoria.
     * Le scadenze NULL sono ammesse in questa ricerca.
     *
     * Non costituisce una verifica di disponibilita fisica,
     * ne' sostituisce le validazioni applicative.
     */
    @Query("""
            select l
            from InventoryLot l
            where l.tenantId = :tenantId
              and l.item.id = :itemId
              and l.active = true
              and :referenceDate is not null
              and (
                  l.expirationDate is null
                  or l.expirationDate >= :referenceDate
              )
            order by l.lotCode asc, l.id asc
            """)
    List<InventoryLot> findActiveUnexpiredLotsByTenantAndItem(
            @Param("tenantId") Long tenantId,
            @Param("itemId") Long itemId,
            @Param("referenceDate") LocalDate referenceDate
    );
}