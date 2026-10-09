package com.gestiva.warehouse.location.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.platform.company.entity.Company;
import com.gestiva.platform.company.repository.CompanyRepository;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.entity.WarehouseType;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final CompanyRepository companyRepository;

    public WarehouseService(
            WarehouseRepository warehouseRepository,
            CompanyRepository companyRepository) {

        this.warehouseRepository = warehouseRepository;
        this.companyRepository = companyRepository;
    }

    /**
     * Crea un nuovo magazzino associato a una Company.
     */
    @Transactional
    public Warehouse create(
            Long tenantId,
            Long companyId,
            String code,
            String name,
            String description,
            WarehouseType warehouseType) {

        if (tenantId == null || companyId == null) {
            throw new BusinessException(
                    "Tenant e azienda sono obbligatori."
            );
        }

        Company company = companyRepository
                .findByTenantIdAndId(tenantId, companyId)
                .orElseThrow(() -> new BusinessException(
                        "Azienda non trovata per il tenant."
                ));

        if (!company.isActive()) {
            throw new BusinessException(
                    "Non è possibile creare magazzini per un'azienda inattiva."
            );
        }

        String normalizedCode = normalizeCode(code);
        String normalizedName = normalizeName(name);

        if (normalizedCode.length() > 30) {
            throw new BusinessException(
                    "Il codice del magazzino non può superare 30 caratteri."
            );
        }

        if (normalizedName.length() > 180) {
            throw new BusinessException(
                    "Il nome del magazzino non può superare 180 caratteri."
            );
        }

        if (description != null && description.length() > 500) {
            throw new BusinessException(
                    "La descrizione non può superare 500 caratteri."
            );
        }

        if (warehouseRepository.existsByTenantIdAndCompanyIdAndCode(
                tenantId,
                companyId,
                normalizedCode)) {

            throw new BusinessException(
                    "Esiste già un magazzino con questo codice per l'azienda."
            );
        }

        Warehouse warehouse = new Warehouse();

        warehouse.setTenantId(tenantId);
        warehouse.setCompany(company);
        warehouse.setCode(normalizedCode);
        warehouse.setName(normalizedName);
        warehouse.setDescription(
                description == null ? null : description.trim()
        );

        warehouse.setWarehouseType(
                warehouseType != null
                        ? warehouseType
                        : WarehouseType.PHYSICAL
        );

        warehouse.setAllowNegativeStock(false);
        warehouse.setActive(true);

        return warehouseRepository.save(warehouse);
    }

    /**
     * Recupera un magazzino verificando tenant e Company.
     */
    public Warehouse getById(
            Long tenantId,
            Long companyId,
            Long warehouseId) {

        return warehouseRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        warehouseId
                )
                .orElseThrow(() -> new BusinessException(
                        "Magazzino non trovato."
                ));
    }

    /**
     * Elenca tutti i magazzini dell'azienda.
     */
    public List<Warehouse> findAll(
            Long tenantId,
            Long companyId) {

        return warehouseRepository
                .findByTenantIdAndCompanyIdOrderByCodeAsc(
                        tenantId,
                        companyId
                );
    }

    /**
     * Elenca soltanto i magazzini attivi.
     */
    public List<Warehouse> findActive(
            Long tenantId,
            Long companyId) {

        return warehouseRepository
                .findByTenantIdAndCompanyIdAndActiveTrueOrderByCodeAsc(
                        tenantId,
                        companyId
                );
    }

    private String normalizeCode(String code) {

        if (code == null || code.isBlank()) {
            throw new BusinessException(
                    "Il codice del magazzino è obbligatorio."
            );
        }

        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeName(String name) {

        if (name == null || name.isBlank()) {
            throw new BusinessException(
                    "Il nome del magazzino è obbligatorio."
            );
        }

        return name.trim();
    }
}