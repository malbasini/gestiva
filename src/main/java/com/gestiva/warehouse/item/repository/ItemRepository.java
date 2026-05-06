package com.gestiva.warehouse.item.repository;

import com.gestiva.warehouse.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    Optional<Item> findByTenantIdAndId(Long tenantId, Long id);
    Optional<Item> findByTenantIdAndCode(Long tenantId, String code);
    boolean existsByTenantIdAndCode(Long tenantId, String code);
    List<Item> findByTenantIdAndActiveTrueOrderByNameAsc(Long tenantId);
}
