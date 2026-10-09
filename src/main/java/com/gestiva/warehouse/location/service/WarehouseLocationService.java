package com.gestiva.warehouse.location.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.platform.company.entity.Company;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.entity.WarehouseLocation;
import com.gestiva.warehouse.location.entity.WarehouseLocationType;
import com.gestiva.warehouse.location.repository.WarehouseLocationRepository;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class WarehouseLocationService {

    private final WarehouseLocationRepository locationRepository;
    private final WarehouseRepository warehouseRepository;

    public WarehouseLocationService(
            WarehouseLocationRepository locationRepository,
            WarehouseRepository warehouseRepository) {

        this.locationRepository = locationRepository;
        this.warehouseRepository = warehouseRepository;
    }

    /**
     * Crea un'ubicazione all'interno di un magazzino.
     */
    @Transactional
    public WarehouseLocation create(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long parentLocationId,
            String code,
            String name,
            WarehouseLocationType locationType,
            String barcode) {

        if (tenantId == null ||
                companyId == null ||
                warehouseId == null) {

            throw new BusinessException(
                    "Tenant, azienda e magazzino sono obbligatori."
            );
        }

        Warehouse warehouse = warehouseRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        warehouseId
                )
                .orElseThrow(() -> new BusinessException(
                        "Magazzino non trovato per l'azienda."
                ));

        if (!warehouse.isActive()) {
            throw new BusinessException(
                    "Non è possibile creare ubicazioni in un magazzino inattivo."
            );
        }

        Company company = warehouse.getCompany();

        if (!company.isActive()) {
            throw new BusinessException(
                    "Non è possibile creare ubicazioni per un'azienda inattiva."
            );
        }

        String normalizedCode = normalizeCode(code);
        String normalizedName = normalizeName(name);
        String normalizedBarcode = normalizeBarcode(barcode);

        if (normalizedCode.length() > 50) {
            throw new BusinessException(
                    "Il codice dell'ubicazione non può superare 50 caratteri."
            );
        }

        if (normalizedName.length() > 180) {
            throw new BusinessException(
                    "Il nome dell'ubicazione non può superare 180 caratteri."
            );
        }

        if (normalizedBarcode != null &&
                normalizedBarcode.length() > 100) {

            throw new BusinessException(
                    "Il barcode non può superare 100 caratteri."
            );
        }

        if (locationRepository.existsByTenantIdAndWarehouseIdAndCode(
                tenantId,
                warehouseId,
                normalizedCode)) {

            throw new BusinessException(
                    "Esiste già un'ubicazione con questo codice nel magazzino."
            );
        }

        WarehouseLocation parentLocation = null;

        if (parentLocationId != null) {

            parentLocation = locationRepository
                    .findByTenantIdAndWarehouseIdAndId(
                            tenantId,
                            warehouseId,
                            parentLocationId
                    )
                    .orElseThrow(() -> new BusinessException(
                            "Ubicazione padre non trovata nel magazzino."
                    ));

            if (!parentLocation.isActive()) {
                throw new BusinessException(
                        "L'ubicazione padre non è attiva."
                );
            }
        }

        WarehouseLocation location = new WarehouseLocation();

        location.setTenantId(tenantId);
        location.setWarehouse(warehouse);
        location.setParentLocation(parentLocation);
        location.setCode(normalizedCode);
        location.setName(normalizedName);

        location.setLocationType(
                locationType != null
                        ? locationType
                        : WarehouseLocationType.STORAGE
        );

        location.setBarcode(normalizedBarcode);
        location.setActive(true);

        return locationRepository.save(location);
    }

    /**
     * Recupera un'ubicazione verificando tenant,
     * azienda e magazzino.
     */
    public WarehouseLocation getById(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long locationId) {

        getWarehouse(tenantId, companyId, warehouseId);

        return locationRepository
                .findByTenantIdAndWarehouseIdAndId(
                        tenantId,
                        warehouseId,
                        locationId
                )
                .orElseThrow(() -> new BusinessException(
                        "Ubicazione non trovata."
                ));
    }

    /**
     * Elenca tutte le ubicazioni del magazzino.
     */
    public List<WarehouseLocation> findAll(
            Long tenantId,
            Long companyId,
            Long warehouseId) {

        getWarehouse(tenantId, companyId, warehouseId);

        return locationRepository
                .findByTenantIdAndWarehouseIdOrderByCodeAsc(
                        tenantId,
                        warehouseId
                );
    }

    /**
     * Recupera le ubicazioni di primo livello.
     */
    public List<WarehouseLocation> findRootLocations(
            Long tenantId,
            Long companyId,
            Long warehouseId) {

        getWarehouse(tenantId, companyId, warehouseId);

        return locationRepository
                .findByTenantIdAndWarehouseIdAndParentLocationIsNull(
                        tenantId,
                        warehouseId
                );
    }

    /**
     * Recupera le ubicazioni figlie di un elemento.
     */
    public List<WarehouseLocation> findChildren(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long parentLocationId) {

        getById(
                tenantId,
                companyId,
                warehouseId,
                parentLocationId
        );

        return locationRepository
                .findByTenantIdAndWarehouseIdAndParentLocationId(
                        tenantId,
                        warehouseId,
                        parentLocationId
                );
    }

    private Warehouse getWarehouse(
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

    private String normalizeCode(String code) {

        if (code == null || code.isBlank()) {
            throw new BusinessException(
                    "Il codice dell'ubicazione è obbligatorio."
            );
        }

        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeName(String name) {

        if (name == null || name.isBlank()) {
            throw new BusinessException(
                    "Il nome dell'ubicazione è obbligatorio."
            );
        }

        return name.trim();
    }

    private String normalizeBarcode(String barcode) {

        if (barcode == null || barcode.isBlank()) {
            return null;
        }

        return barcode.trim();
    }
}