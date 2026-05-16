package com.gestiva.inventory.valuation.repository;

import com.gestiva.inventory.valuation.entity.InventoryLayerConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryLayerConsumptionRepository extends JpaRepository<InventoryLayerConsumption, Long> {

    List<InventoryLayerConsumption> findByTenantIdAndOutMovementIdOrderByIdAsc(Long tenantId, Long outMovementId);

    List<InventoryLayerConsumption> findByTenantIdAndLayerIdOrderByIdAsc(Long tenantId, Long layerId);
}