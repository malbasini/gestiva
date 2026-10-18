package com.gestiva.warehouse.inventory.valuation.repository;

import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InventoryLayerRepository extends JpaRepository<InventoryLayer, Long> {

    Optional<InventoryLayer> findByTenantIdAndId(Long tenantId, Long id);

    List<InventoryLayer> findByTenantIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(Long tenantId, Long itemId);

    List<InventoryLayer> findByTenantIdAndItemIdAndClosedFalseOrderByLayerDateDescIdDesc(Long tenantId, Long itemId);

    List<InventoryLayer> findByTenantIdAndSourceMovementIdOrderByIdAsc(Long tenantId, Long sourceMovementId);

    List<InventoryLayer> findByTenantIdAndClosedFalseOrderByItemIdAscLayerDateAscIdAsc(Long tenantId);

    // Gestiva V2 - Strati FIFO disponibili per società e magazzino
    List<InventoryLayer>
    findByTenantIdAndCompanyIdAndWarehouseIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long itemId
    );

    // Gestiva V2 - Strati generati da un movimento di carico
    List<InventoryLayer>
    findByTenantIdAndCompanyIdAndSourceMovementIdOrderByIdAsc(
            Long tenantId,
            Long companyId,
            Long sourceMovementId
    );

    // Gestiva V2 - Recupero di uno strato appartenente alla società
    Optional<InventoryLayer>
    findByTenantIdAndCompanyIdAndId(
            Long tenantId,
            Long companyId,
            Long id
    );
}