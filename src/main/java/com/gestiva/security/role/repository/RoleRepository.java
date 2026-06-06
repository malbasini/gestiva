package com.gestiva.security.role.repository;

import com.gestiva.security.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByTenantIdAndCode(Long tenantId, String code);
    List<Role> findByTenantId(Long tenantId);
    boolean existsByTenantIdAndCode(Long tenantId, String code);
    List<Role> findByNameInOrderByNameAsc(java.util.Collection<String> names);
    Optional<Role> findByName(String name);
}
