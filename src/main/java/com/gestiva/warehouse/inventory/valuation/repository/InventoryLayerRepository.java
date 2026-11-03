package com.gestiva.warehouse.inventory.valuation.repository;

import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface InventoryLayerRepository extends JpaRepository<InventoryLayer, Long> {

    Optional<InventoryLayer> findByTenantIdAndId(Long tenantId, Long id);

    List<InventoryLayer> findByTenantIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(Long tenantId, Long itemId);

    List<InventoryLayer> findByTenantIdAndItemIdAndClosedFalseOrderByLayerDateDescIdDesc(Long tenantId, Long itemId);

    List<InventoryLayer> findByTenantIdAndSourceMovementIdOrderByIdAsc(Long tenantId, Long sourceMovementId);

    List<InventoryLayer> findByTenantIdAndClosedFalseOrderByItemIdAscLayerDateAscIdAsc(Long tenantId);


    // Gestiva V2 - Tutti gli strati FIFO aperti della società
    List<InventoryLayer>
    findByTenantIdAndCompanyIdAndClosedFalseOrderByItemIdAscLayerDateAscIdAsc(
            Long tenantId,
            Long companyId
    );

    // Gestiva V2 - Strati FIFO aperti di un articolo nella società
    List<InventoryLayer>
    findByTenantIdAndCompanyIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
            Long tenantId,
            Long companyId,
            Long itemId
    );

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

    /**
     * Articoli con layer FIFO nella società selezionata.
     * Include anche i layer completamente consumati e chiusi.
     */
    @Query("""
            select distinct l.itemId
            from InventoryLayer l
            where l.tenantId = :tenantId
              and l.companyId = :companyId
            """)
    List<Long> findDistinctItemIdsByTenantIdAndCompanyId(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId
    );

}