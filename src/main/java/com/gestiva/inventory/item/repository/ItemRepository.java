package com.gestiva.inventory.item.repository;

import com.gestiva.inventory.item.entity.Item;
import com.gestiva.inventory.movement.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    Optional<Item> findByTenantIdAndId(Long tenantId, Long id);
    Optional<Item> findByTenantIdAndCode(Long tenantId, String code);
    boolean existsByTenantIdAndCode(Long tenantId, String code);
    List<Item> findByTenantIdAndActiveTrueOrderByNameAsc(Long tenantId);
    List<Item> findByTenantIdAndActiveTrueOrderByCodeAsc(Long tenantId);
    List<Item> findByTenantIdOrderByCodeAsc(Long tenantId);
    long countByTenantIdAndTrackStockTrue(Long tenantId);

}
