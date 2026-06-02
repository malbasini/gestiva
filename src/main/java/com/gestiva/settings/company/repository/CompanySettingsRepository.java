package com.gestiva.settings.company.repository;

import com.gestiva.settings.company.entity.CompanySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompanySettingsRepository extends JpaRepository<CompanySettings, Long> {

    Optional<CompanySettings> findByTenantId(Long tenantId);
}