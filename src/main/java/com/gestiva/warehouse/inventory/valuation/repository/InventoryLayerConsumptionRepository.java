package com.gestiva.warehouse.inventory.valuation.repository;

import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayerConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryLayerConsumptionRepository extends JpaRepository<InventoryLayerConsumption, Long> {

    List<InventoryLayerConsumption> findByTenantIdAndOutMovementIdOrderByIdAsc(Long tenantId, Long outMovementId);

    List<InventoryLayerConsumption> findByTenantIdAndLayerIdOrderByIdAsc(Long tenantId, Long layerId);

    // Gestiva V2 - Consumi FIFO associati a un movimento di scarico
    List<InventoryLayerConsumption>
    findByTenantIdAndCompanyIdAndWarehouseIdAndOutMovementIdOrderByIdAsc(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long outMovementId
    );

    // Gestiva V2 - Consumi FIFO associati a uno strato
    List<InventoryLayerConsumption>
    findByTenantIdAndCompanyIdAndWarehouseIdAndLayerIdOrderByIdAsc(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long layerId
    );
}