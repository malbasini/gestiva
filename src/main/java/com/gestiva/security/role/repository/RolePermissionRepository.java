package com.gestiva.security.role.repository;

import com.gestiva.security.role.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByTenantIdAndRoleId(Long tenantId, Long roleId);
}
