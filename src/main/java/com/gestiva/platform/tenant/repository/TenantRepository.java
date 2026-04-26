package com.gestiva.platform.tenant.repository;

import com.gestiva.platform.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
}