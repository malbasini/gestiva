package com.gestiva.warehouse.item.repository;

import com.gestiva.warehouse.item.entity.ItemUom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ItemUomRepository
        extends JpaRepository<ItemUom, Long> {

    Optional<ItemUom> findByTenantIdAndId(
            Long tenantId,
            Long id
    );

    List<ItemUom> findByTenantIdAndItemIdOrderByIdAsc(
            Long tenantId,
            Long itemId
    );

    Optional<ItemUom> findByTenantIdAndItemIdAndUomId(
            Long tenantId,
            Long itemId,
            Long uomId
    );

    boolean existsByTenantIdAndItemIdAndUomId(
            Long tenantId,
            Long itemId,
            Long uomId
    );
}
