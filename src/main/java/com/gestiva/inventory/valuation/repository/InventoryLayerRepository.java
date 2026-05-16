package com.gestiva.inventory.valuation.repository;

import com.gestiva.inventory.valuation.entity.InventoryLayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryLayerRepository extends JpaRepository<InventoryLayer, Long> {

    Optional<InventoryLayer> findByTenantIdAndId(Long tenantId, Long id);

    List<InventoryLayer> findByTenantIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(Long tenantId, Long itemId);

    List<InventoryLayer> findByTenantIdAndItemIdAndClosedFalseOrderByLayerDateDescIdDesc(Long tenantId, Long itemId);

    List<InventoryLayer> findByTenantIdAndSourceMovementIdOrderByIdAsc(Long tenantId, Long sourceMovementId);
}