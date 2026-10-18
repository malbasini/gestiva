package com.gestiva.warehouse.location.repository;

import com.gestiva.warehouse.location.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


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

    // Gestiva V2 - Blocco transazionale del magazzino
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT w
        FROM Warehouse w
        WHERE w.tenantId = :tenantId
          AND w.company.id = :companyId
          AND w.id = :warehouseId
        """)
    Optional<Warehouse> findByTenantAndCompanyAndIdForUpdate(
            @Param("tenantId") Long tenantId,
            @Param("companyId") Long companyId,
            @Param("warehouseId") Long warehouseId
    );
}