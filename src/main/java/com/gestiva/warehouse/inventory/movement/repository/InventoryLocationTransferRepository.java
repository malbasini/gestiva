package com.gestiva.warehouse.inventory.movement.repository;

import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository dei trasferimenti tra ubicazioni.
 *
 * Le ricerche applicative devono essere sempre
 * limitate a tenant e company.
 *
 * La chiave operationKey è univoca nello stesso
 * tenant/company tramite vincolo SQL della V12.
 */
public interface InventoryLocationTransferRepository
        extends JpaRepository<InventoryLocationTransfer, Long> {

    Optional<InventoryLocationTransfer>
    findByTenantIdAndCompanyIdAndOperationKey(
            Long tenantId,
            Long companyId,
            String operationKey
    );

    Optional<InventoryLocationTransfer>
    findByTenantIdAndCompanyIdAndId(
            Long tenantId,
            Long companyId,
            Long id
    );


    // =====================================================
    // GESTIVA V2 - STORICO TRASFERIMENTI PER LOTTO V14
    // =====================================================

    /**
     * Storico dei trasferimenti dell'articolo e del lotto
     * nella societa selezionata.
     *
     * NULL lotId identifica esclusivamente i trasferimenti
     * senza lotto.
     *
     * Include tutti gli stati del trasferimento.
     * Non costituisce una verifica di disponibilita.
     *
     * L'ordinamento e' cronologico per data e ID.
     * Il contratto operationKey rimane invariato.
     */
    List<InventoryLocationTransfer>
    findByTenantIdAndCompanyIdAndItemIdAndLotIdOrderByMovementDateAscIdAsc(
            Long tenantId,
            Long companyId,
            Long itemId,
            Long lotId
    );
}
