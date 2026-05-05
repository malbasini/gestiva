package com.gestiva.platform.role.repository;

import com.gestiva.platform.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByTenantIdAndCode(Long tenantId, String code);
    List<Role> findByTenantId(Long tenantId);
    boolean existsByTenantIdAndCode(Long tenantId, String code);
}
