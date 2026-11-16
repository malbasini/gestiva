package com.gestiva.warehouse.location.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.location.entity.WarehouseLocation;
import com.gestiva.warehouse.location.repository.WarehouseLocationRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WarehouseLocationDeactivationService {

    private final WarehouseLocationService locationService;
    private final WarehouseLocationRepository locationRepository;
    private final InventoryMovementRepository movementRepository;
    private final InventoryLayerRepository layerRepository;

    public WarehouseLocationDeactivationService(
            WarehouseLocationService locationService,
            WarehouseLocationRepository locationRepository,
            InventoryMovementRepository movementRepository,
            InventoryLayerRepository layerRepository) {

        this.locationService = locationService;
        this.locationRepository = locationRepository;
        this.movementRepository = movementRepository;
        this.layerRepository = layerRepository;
    }

    @Transactional
    public void deactivate(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long locationId) {

        if (tenantId == null || companyId == null ||
                warehouseId == null || locationId == null) {
            throw new BusinessException(
                    "Tenant, società, magazzino e ubicazione sono obbligatori."
            );
        }

        WarehouseLocation location = locationService.getById(
                tenantId,
                companyId,
                warehouseId,
                locationId
        );

        if (!location.getWarehouse().isActive()) {
            throw new BusinessException(
                    "Non è possibile operare su un magazzino inattivo."
            );
        }

        if (!location.getWarehouse().getCompany().isActive()) {
            throw new BusinessException(
                    "Non è possibile operare su una società inattiva."
            );
        }

        if (!location.isActive()) {
            throw new BusinessException(
                    "L'ubicazione è già inattiva."
            );
        }

        boolean hasActiveChildren = locationRepository
                .findByTenantIdAndWarehouseIdAndParentLocationId(
                        tenantId,
                        warehouseId,
                        locationId
                )
                .stream()
                .anyMatch(WarehouseLocation::isActive);

        if (hasActiveChildren) {
            throw new BusinessException(
                    "Disattivazione impedita: esistono ubicazioni figlie attive."
            );
        }

        if (!movementRepository
                .findItemIdsWithNonZeroBalanceByLocation(
                        tenantId,
                        companyId,
                        warehouseId,
                        locationId
                ).isEmpty()) {

            throw new BusinessException(
                    "Disattivazione impedita: l'ubicazione presenta giacenze non nulle."
            );
        }

        if (layerRepository.countRemainingLayersByLocation(
                tenantId,
                companyId,
                warehouseId,
                locationId
        ) > 0) {

            throw new BusinessException(
                    "Disattivazione impedita: esistono layer FIFO con quantità residua."
            );
        }

        location.setActive(false);
        locationRepository.save(location);
    }
}
