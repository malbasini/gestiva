package com.gestiva.warehouse.location.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.platform.company.entity.Company;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.entity.WarehouseLocation;
import com.gestiva.warehouse.location.repository.WarehouseLocationRepository;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseLocationDeactivationServiceTest {

    private static final Long TENANT_ID = 2L;
    private static final Long COMPANY_ID = 1L;
    private static final Long WAREHOUSE_ID = 10L;
    private static final Long LOCATION_ID = 30L;

    @Mock
    private WarehouseLocationService locationService;

    @Mock
    private WarehouseLocationRepository locationRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private InventoryMovementRepository movementRepository;

    @Mock
    private InventoryLayerRepository layerRepository;

    @InjectMocks
    private WarehouseLocationDeactivationService service;

    private WarehouseLocation location() {
        Company company = new Company();
        company.setActive(true);

        Warehouse warehouse = new Warehouse();
        warehouse.setCompany(company);
        warehouse.setActive(true);

        WarehouseLocation location = new WarehouseLocation();
        location.setWarehouse(warehouse);
        location.setActive(true);
        location.setCode("A-01");

        return location;
    }

    private void configureLocation(WarehouseLocation location) {
        when(warehouseRepository.findByTenantAndCompanyAndIdForUpdate(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID
        )).thenReturn(Optional.of(location.getWarehouse()));

        when(locationService.getById(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID, LOCATION_ID
        )).thenReturn(location);
    }

    @Test
    void emptyLocationCanBeDeactivated() {
        WarehouseLocation location = location();
        configureLocation(location);

        service.deactivate(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID, LOCATION_ID
        );

        assertFalse(location.isActive());
        verify(locationRepository).save(same(location));

        verify(movementRepository)
                .findItemIdsWithNonZeroBalanceByLocation(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID, LOCATION_ID
                );

        verify(layerRepository).countRemainingLayersByLocation(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID, LOCATION_ID
        );
    }

    @Test
    void missingIdentifiersAreRejected() {
        assertThrows(BusinessException.class, () ->
                service.deactivate(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID, null
                )
        );

        verifyNoInteractions(
                locationService,
                locationRepository,
                movementRepository,
                layerRepository
        );
    }

    @Test
    void inactiveLocationCannotBeDeactivatedAgain() {
        WarehouseLocation location = location();
        location.setActive(false);
        configureLocation(location);

        assertThrows(BusinessException.class, () ->
                service.deactivate(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID, LOCATION_ID
                )
        );

        verify(locationRepository, never()).save(any());
        verifyNoInteractions(movementRepository, layerRepository);
    }

    @Test
    void activeChildBlocksDeactivation() {
        WarehouseLocation location = location();
        WarehouseLocation child = location();
        configureLocation(location);

        when(locationRepository
                .findByTenantIdAndWarehouseIdAndParentLocationId(
                        TENANT_ID, WAREHOUSE_ID, LOCATION_ID
                )).thenReturn(List.of(child));

        assertThrows(BusinessException.class, () ->
                service.deactivate(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID, LOCATION_ID
                )
        );

        assertTrue(location.isActive());
        verify(locationRepository, never()).save(any());
        verifyNoInteractions(movementRepository, layerRepository);
    }

    @Test
    void nonZeroInventoryBalanceBlocksDeactivation() {
        WarehouseLocation location = location();
        configureLocation(location);

        when(movementRepository
                .findItemIdsWithNonZeroBalanceByLocation(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID, LOCATION_ID
                )).thenReturn(List.of(501L));

        assertThrows(BusinessException.class, () ->
                service.deactivate(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID, LOCATION_ID
                )
        );

        assertTrue(location.isActive());
        verify(locationRepository, never()).save(any());
        verifyNoInteractions(layerRepository);
    }

    @Test
    void remainingFifoLayersBlockDeactivation() {
        WarehouseLocation location = location();
        configureLocation(location);

        when(layerRepository.countRemainingLayersByLocation(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID, LOCATION_ID
        )).thenReturn(1L);

        assertThrows(BusinessException.class, () ->
                service.deactivate(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID, LOCATION_ID
                )
        );

        assertTrue(location.isActive());
        verify(locationRepository, never()).save(any());
    }

    @Test
    void foreignLocationIsRejectedWithoutSaving() {
        Warehouse warehouse = mock(Warehouse.class);

        when(warehouseRepository.findByTenantAndCompanyAndIdForUpdate(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID
        )).thenReturn(Optional.of(warehouse));

        when(locationService.getById(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID, LOCATION_ID
        )).thenThrow(new BusinessException(
                "Ubicazione non trovata."
        ));

        assertThrows(BusinessException.class, () ->
                service.deactivate(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID, LOCATION_ID
                )
        );

        verifyNoInteractions(
                locationRepository,
                movementRepository,
                layerRepository
        );
    }
}
