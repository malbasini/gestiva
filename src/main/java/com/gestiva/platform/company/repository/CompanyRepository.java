package com.gestiva.platform.company.repository;

import com.gestiva.platform.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByTenantIdAndCode(Long tenantId, String code);
}