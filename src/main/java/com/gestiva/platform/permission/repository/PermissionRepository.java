package com.gestiva.platform.permission.repository;

import com.gestiva.platform.permission.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByCode(String code);
    List<Permission> findByModuleCode(String moduleCode);
    boolean existsByCode(String code);
}