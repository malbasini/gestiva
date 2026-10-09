package com.gestiva.warehouse.tracking.repository;

import com.gestiva.warehouse.tracking.entity.InventorySerial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InventorySerialRepository extends JpaRepository<InventorySerial, Long> {

    Optional<InventorySerial> findByTenantIdAndId(
            Long tenantId,
            Long id
    );

    Optional<InventorySerial> findByTenantIdAndItemIdAndId(
            Long tenantId,
            Long itemId,
            Long id
    );

    Optional<InventorySerial> findByTenantIdAndItemIdAndSerialNumber(
            Long tenantId,
            Long itemId,
            String serialNumber
    );

    boolean existsByTenantIdAndItemIdAndSerialNumber(
            Long tenantId,
            Long itemId,
            String serialNumber
    );

    List<InventorySerial> findByTenantIdAndItemIdOrderBySerialNumberAsc(
            Long tenantId,
            Long itemId
    );

    List<InventorySerial> findByTenantIdAndItemIdAndActiveTrueOrderBySerialNumberAsc(
            Long tenantId,
            Long itemId
    );
}