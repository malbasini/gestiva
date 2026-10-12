package com.gestiva.security.company.repository;

import com.gestiva.security.company.entity.UserCompanyAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserCompanyAccessRepository
        extends JpaRepository<UserCompanyAccess, Long> {

    boolean existsByTenantIdAndUserIdAndCompanyId(
            Long tenantId,
            Long userId,
            Long companyId
    );

    List<UserCompanyAccess> findByTenantIdAndUserId(
            Long tenantId,
            Long userId
    );
}