package com.gestiva.security.tenant.repository;

import com.gestiva.security.tenant.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByTenantIdAndEmail(Long tenantId, String email);
    List<AppUser> findByTenantId(Long tenantId);
    boolean existsByTenantIdAndEmail(Long tenantId, String email);
    List<AppUser> findByTenantIdOrderByLastNameAscFirstNameAsc(Long tenantId);
    Optional<AppUser> findByTenantIdAndId(Long tenantId, Long id);

}