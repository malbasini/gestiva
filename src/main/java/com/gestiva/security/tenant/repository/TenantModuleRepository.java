package com.gestiva.security.tenant.repository;

import com.gestiva.security.tenant.entity.TenantModule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TenantModuleRepository extends JpaRepository<TenantModule, Long> {
    List<TenantModule> findByTenantId(Long tenantId);
    Optional<TenantModule> findByTenantIdAndModuleCode(Long tenantId, String moduleCode);
}