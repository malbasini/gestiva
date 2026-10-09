package com.gestiva.warehouse.location.repository;

import com.gestiva.warehouse.location.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByTenantIdAndId(
            Long tenantId,
            Long id
    );

    Optional<Warehouse> findByTenantIdAndCompanyIdAndId(
            Long tenantId,
            Long companyId,
            Long id
    );

    Optional<Warehouse> findByTenantIdAndCompanyIdAndCode(
            Long tenantId,
            Long companyId,
            String code
    );

    List<Warehouse> findByTenantIdAndCompanyIdOrderByCodeAsc(
            Long tenantId,
            Long companyId
    );

    List<Warehouse> findByTenantIdAndCompanyIdAndActiveTrueOrderByCodeAsc(
            Long tenantId,
            Long companyId
    );

    boolean existsByTenantIdAndCompanyIdAndCode(
            Long tenantId,
            Long companyId,
            String code
    );
}