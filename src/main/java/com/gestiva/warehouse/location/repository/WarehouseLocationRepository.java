package com.gestiva.warehouse.location.repository;

import com.gestiva.warehouse.location.entity.WarehouseLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseLocationRepository extends JpaRepository<WarehouseLocation, Long> {

    Optional<WarehouseLocation> findByTenantIdAndWarehouseIdAndId(
            Long tenantId,
            Long warehouseId,
            Long id
    );

    Optional<WarehouseLocation> findByTenantIdAndWarehouseIdAndCode(
            Long tenantId,
            Long warehouseId,
            String code
    );

    List<WarehouseLocation> findByTenantIdAndWarehouseIdOrderByCodeAsc(
            Long tenantId,
            Long warehouseId
    );

    List<WarehouseLocation> findByTenantIdAndWarehouseIdAndActiveTrueOrderByCodeAsc(
            Long tenantId,
            Long warehouseId
    );

    List<WarehouseLocation> findByTenantIdAndWarehouseIdAndParentLocationId(
            Long tenantId,
            Long warehouseId,
            Long parentLocationId
    );

    List<WarehouseLocation> findByTenantIdAndWarehouseIdAndParentLocationIsNull(
            Long tenantId,
            Long warehouseId
    );

    boolean existsByTenantIdAndWarehouseIdAndCode(
            Long tenantId,
            Long warehouseId,
            String code
    );

    boolean existsByTenantIdAndWarehouseIdAndParentLocationId(
            Long tenantId,
            Long warehouseId,
            Long parentLocationId
    );
}