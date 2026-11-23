package com.gestiva.warehouse.inventory.movement.repository;

import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransferAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository delle allocazioni FIFO collegate
 * a un trasferimento tra ubicazioni.
 *
 * La ricerca per transferId è sempre limitata
 * a tenant e company.
 */
public interface InventoryLocationTransferAllocationRepository
        extends JpaRepository<InventoryLocationTransferAllocation, Long> {

    List<InventoryLocationTransferAllocation>
    findByTenantIdAndCompanyIdAndTransferIdOrderByIdAsc(
            Long tenantId,
            Long companyId,
            Long transferId
    );
}
