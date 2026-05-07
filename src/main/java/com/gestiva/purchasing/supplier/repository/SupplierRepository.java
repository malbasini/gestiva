package com.gestiva.purchasing.supplier.repository;

import com.gestiva.purchasing.supplier.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long>, JpaSpecificationExecutor<Supplier> {

    Optional<Supplier> findByTenantIdAndId(Long tenantId, Long id);

    Optional<Supplier> findByTenantIdAndCode(Long tenantId, String code);

    boolean existsByTenantIdAndCode(Long tenantId, String code);

    List<Supplier> findByTenantIdAndActiveTrueOrderByNameAsc(Long tenantId);
}
