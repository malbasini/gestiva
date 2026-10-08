package com.gestiva.warehouse.uom.repository;

import com.gestiva.warehouse.uom.entity.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UnitOfMeasureRepository
        extends JpaRepository<UnitOfMeasure, Long> {

    Optional<UnitOfMeasure> findByTenantIdAndId(
            Long tenantId,
            Long id
    );

    Optional<UnitOfMeasure> findByTenantIdAndCode(
            Long tenantId,
            String code
    );

    List<UnitOfMeasure> findByTenantIdAndActiveTrueOrderByCodeAsc(
            Long tenantId
    );

    boolean existsByTenantIdAndCode(
            Long tenantId,
            String code
    );

    List<UnitOfMeasure> findByTenantId(Long tenantId);
}