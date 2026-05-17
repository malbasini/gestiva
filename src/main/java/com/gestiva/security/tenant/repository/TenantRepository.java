package com.gestiva.security.tenant.repository;

import com.gestiva.security.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long>{
    Optional<Tenant> findBySlug(String slug);
    Optional<Tenant> findByEmail(String email);
    boolean existsBySlug(String slug);
    boolean existsByEmail(String email);
}