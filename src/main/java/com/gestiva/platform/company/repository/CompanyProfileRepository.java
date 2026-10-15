package com.gestiva.platform.company.repository;

import com.gestiva.platform.company.entity.CompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {

    // Metodo legacy: da eliminare dopo la migrazione dei servizi
    Optional<CompanyProfile> findByTenantId(Long tenantId);

    // Metodo V2 multi-società
    Optional<CompanyProfile> findByTenantIdAndCompanyId(
            Long tenantId,
            Long companyId
    );
}