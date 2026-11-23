package com.gestiva.warehouse.inventory.movement.repository;

import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
