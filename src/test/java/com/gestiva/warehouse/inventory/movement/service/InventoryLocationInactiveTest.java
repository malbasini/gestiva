package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.entity.WarehouseLocation;
import com.gestiva.warehouse.location.repository.WarehouseLocationRepository;
import com.gestiva.warehouse.location.repository.WarehouseRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryLocationInactiveTest {

    private static final Long TENANT_ID = 2L;
    private static final Long COMPANY_ID = 1L;
    private static final Long WAREHOUSE_ID = 2L;
    private static final Long LOCATION_ID = 999L;

    @Mock
    WarehouseRepository warehouseRepository;

    @Mock
    WarehouseLocationRepository locationRepository;

    @InjectMocks
    InventoryLocationValidationService service;

    private void activeWarehouse() {
        Warehouse warehouse = mock(Warehouse.class);

        when(warehouse.isActive()).thenReturn(true);

        when(warehouseRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID
        )).thenReturn(Optional.of(warehouse));
    }

    @Test
    void inactiveLocationMustBeRejected() {
        activeWarehouse();

        WarehouseLocation location = mock(WarehouseLocation.class);
        when(location.isActive()).thenReturn(false);

        when(locationRepository.findByTenantIdAndWarehouseIdAndId(
                TENANT_ID, WAREHOUSE_ID, LOCATION_ID
        )).thenReturn(Optional.of(location));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.validate(
                        TENANT_ID, COMPANY_ID,
                        WAREHOUSE_ID, LOCATION_ID
                )
        );

        assertTrue(ex.getMessage().contains(
                "ubicazione selezionata non è attiva"
        ));

        verify(locationRepository).findByTenantIdAndWarehouseIdAndId(
                TENANT_ID, WAREHOUSE_ID, LOCATION_ID
        );
    }

    @Test
    void activeLocationMustBeAccepted() {
        activeWarehouse();

        WarehouseLocation location = mock(WarehouseLocation.class);
        when(location.isActive()).thenReturn(true);

        when(locationRepository.findByTenantIdAndWarehouseIdAndId(
                TENANT_ID, WAREHOUSE_ID, LOCATION_ID
        )).thenReturn(Optional.of(location));

        assertDoesNotThrow(() -> service.validate(
                TENANT_ID, COMPANY_ID,
                WAREHOUSE_ID, LOCATION_ID
        ));
    }

    @Test
    void locationOutsideWarehouseMustBeRejected() {
        activeWarehouse();

        when(locationRepository.findByTenantIdAndWarehouseIdAndId(
                TENANT_ID, WAREHOUSE_ID, LOCATION_ID
        )).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.validate(
                        TENANT_ID, COMPANY_ID,
                        WAREHOUSE_ID, LOCATION_ID
                )
        );

        assertTrue(ex.getMessage().contains(
                "Ubicazione non trovata nel magazzino indicato"
        ));
    }
}
