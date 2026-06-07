package com.gestiva.security.role.repository;

import com.gestiva.security.role.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByTenantIdAndUserId(Long tenantId, Long userId);
    boolean existsByTenantIdAndUserIdAndRoleId(Long tenantId, Long userId, Long roleId);
    Optional<UserRole> findByTenantIdAndId(Long tenantId, Long id);
    void deleteByTenantIdAndUserId(Long tenantId, Long userId);
    List<UserRole> findByTenantId(Long tenantId);

}