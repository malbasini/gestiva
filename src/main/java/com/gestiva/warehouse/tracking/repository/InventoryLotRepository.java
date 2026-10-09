package com.gestiva.warehouse.tracking.repository;

import com.gestiva.warehouse.tracking.entity.InventoryLot;
import org.springframework.data.jpa.repository.JpaRepository;
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
}