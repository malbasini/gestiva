package com.gestiva.platform.company.service;

import com.gestiva.platform.company.entity.Company;
import com.gestiva.platform.company.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CompanyManagementService {

    private final CompanyRepository companyRepository;
    private final CompanyBootstrapService companyBootstrapService;

    public CompanyManagementService(
            CompanyRepository companyRepository,
            CompanyBootstrapService companyBootstrapService) {

        this.companyRepository = companyRepository;
        this.companyBootstrapService = companyBootstrapService;
    }

    @Transactional(readOnly = true)
    public List<Company> listCompanies(Long tenantId) {
        requireTenantId(tenantId);

        return companyRepository.findByTenantIdOrderByLegalNameAsc(
                tenantId
        );
    }

    @Transactional(readOnly = true)
    public Company getCompany(Long tenantId, Long companyId) {
        requireTenantId(tenantId);

        if (companyId == null) {
            throw new IllegalArgumentException(
                    "Identificativo società obbligatorio."
            );
        }

        return companyRepository
                .findByTenantIdAndId(tenantId, companyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Società non trovata per il tenant indicato."
                ));
    }

    public Company createCompany(
            Long tenantId,
            String code,
            String legalName) {

        requireTenantId(tenantId);

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Codice società obbligatorio."
            );
        }

        if (legalName == null || legalName.isBlank()) {
            throw new IllegalArgumentException(
                    "Ragione sociale obbligatoria."
            );
        }

        String normalizedCode = code.trim().toUpperCase(
                java.util.Locale.ROOT
        );

        if (normalizedCode.length() > 30) {
            throw new IllegalArgumentException(
                    "Codice società troppo lungo."
            );
        }

        String normalizedName = legalName.trim();

        if (normalizedName.length() > 180) {
            throw new IllegalArgumentException(
                    "Ragione sociale troppo lunga."
            );
        }

        if (companyRepository.findByTenantIdAndCode(
                tenantId, normalizedCode
        ).isPresent()) {
            throw new IllegalArgumentException(
                    "Codice società già utilizzato nel tenant."
            );
        }

        Company company = new Company();
        company.setTenantId(tenantId);
        company.setCode(normalizedCode);
        company.setLegalName(normalizedName);
        company.setCountryCode("IT");
        company.setCurrencyCode("EUR");
        company.setLocaleCode("it");
        company.setActive(true);

        Company saved = companyRepository.saveAndFlush(company);

        companyBootstrapService.initializeCompany(
                tenantId, saved.getId()
        );

        return saved;
    }

    public Company updateCompany(
            Long tenantId,
            Long companyId,
            String legalName,
            boolean active) {

        requireTenantId(tenantId);

        if (companyId == null) {
            throw new IllegalArgumentException(
                    "Identificativo società obbligatorio."
            );
        }

        List<Company> tenantCompanies =
                companyRepository.findAllByTenantIdForUpdate(tenantId);

        Company company = tenantCompanies.stream()
                .filter(c -> companyId.equals(c.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Società non trovata per il tenant indicato."
                ));

        if (company.isActive() && !active) {
            long activeCount = tenantCompanies.stream()
                    .filter(Company::isActive)
                    .count();

            if (activeCount <= 1) {
                throw new IllegalArgumentException(
                        "Impossibile disattivare l'ultima società attiva del tenant."
                );
            }
        }

        if (legalName == null || legalName.isBlank()) {
            throw new IllegalArgumentException(
                    "Ragione sociale obbligatoria."
            );
        }

        String normalizedName = legalName.trim();

        if (normalizedName.length() > 180) {
            throw new IllegalArgumentException(
                    "Ragione sociale troppo lunga."
            );
        }

        company.setLegalName(normalizedName);
        company.setActive(active);

        return companyRepository.save(company);
    }

    private void requireTenantId(Long tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "Tenant obbligatorio."
            );
        }
    }
}
