package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.entity.WarehouseLocation;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import com.gestiva.warehouse.location.repository.WarehouseLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InventoryLocationValidationService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseLocationRepository warehouseLocationRepository;

    public InventoryLocationValidationService(
            WarehouseRepository warehouseRepository,
            WarehouseLocationRepository warehouseLocationRepository) {

        this.warehouseRepository = warehouseRepository;
        this.warehouseLocationRepository = warehouseLocationRepository;
    }

    public Warehouse validateWarehouse(
            Long tenantId,
            Long companyId,
            Long warehouseId) {

        if (tenantId == null) {
            throw new BusinessException("Tenant non valido.");
        }

        if (warehouseId == null) {
            throw new BusinessException("Il magazzino è obbligatorio.");
        }

        Warehouse warehouse;

        if (companyId != null) {
            warehouse = warehouseRepository
                    .findByTenantIdAndCompanyIdAndId(
                            tenantId,
                            companyId,
                            warehouseId
                    )
                    .orElseThrow(() -> new BusinessException(
                            "Magazzino non trovato per l'azienda indicata."
                    ));
        } else {
            warehouse = warehouseRepository
                    .findByTenantIdAndId(tenantId, warehouseId)
                    .orElseThrow(() -> new BusinessException(
                            "Magazzino non trovato."
                    ));
        }

        if (!warehouse.isActive()) {
            throw new BusinessException(
                    "Il magazzino selezionato non è attivo."
            );
        }

        return warehouse;
    }

    public WarehouseLocation validateLocation(
            Long tenantId,
            Long warehouseId,
            Long locationId) {

        if (locationId == null) {
            return null;
        }

        if (tenantId == null || warehouseId == null) {
            throw new BusinessException(
                    "Tenant o magazzino non valido."
            );
        }

        WarehouseLocation location = warehouseLocationRepository
                .findByTenantIdAndWarehouseIdAndId(
                        tenantId,
                        warehouseId,
                        locationId
                )
                .orElseThrow(() -> new BusinessException(
                        "Ubicazione non trovata nel magazzino indicato."
                ));

        if (!location.isActive()) {
            throw new BusinessException(
                    "L'ubicazione selezionata non è attiva."
            );
        }

        return location;
    }

    public void validate(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long locationId) {

        validateWarehouse(
                tenantId,
                companyId,
                warehouseId
        );

        validateLocation(
                tenantId,
                warehouseId,
                locationId
        );
    }
}