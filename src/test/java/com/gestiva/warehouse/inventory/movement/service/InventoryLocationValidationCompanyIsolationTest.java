package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.location.entity.Warehouse;
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
class InventoryLocationValidationCompanyIsolationTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;
    private static final Long WAREHOUSE_ID = 77L;

    @Mock
    WarehouseRepository warehouseRepository;

    @Mock
    WarehouseLocationRepository warehouseLocationRepository;

    @InjectMocks
    InventoryLocationValidationService service;

    @Test
    void shouldRejectMissingCompanyWithoutRepositoryAccess() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.validateWarehouse(
                        TENANT_ID,
                        null,
                        WAREHOUSE_ID
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "La società attiva è obbligatoria"
                )
        );

        verifyNoInteractions(
                warehouseRepository,
                warehouseLocationRepository
        );
    }

    @Test
    void shouldRejectWarehouseOutsideSelectedCompany() {
        when(warehouseRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID,
                COMPANY_ID,
                WAREHOUSE_ID
        )).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.validateWarehouse(
                        TENANT_ID,
                        COMPANY_ID,
                        WAREHOUSE_ID
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "Magazzino non trovato per l'azienda indicata"
                )
        );

        verify(warehouseRepository)
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID,
                        COMPANY_ID,
                        WAREHOUSE_ID
                );

        verifyNoInteractions(warehouseLocationRepository);
    }

    @Test
    void shouldRejectInactiveWarehouse() {
        Warehouse warehouse = mock(Warehouse.class);

        when(warehouse.isActive()).thenReturn(false);

        when(warehouseRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID,
                COMPANY_ID,
                WAREHOUSE_ID
        )).thenReturn(Optional.of(warehouse));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.validateWarehouse(
                        TENANT_ID,
                        COMPANY_ID,
                        WAREHOUSE_ID
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "Il magazzino selezionato non è attivo"
                )
        );

        verifyNoInteractions(warehouseLocationRepository);
    }

    @Test
    void shouldAcceptActiveWarehouseOfSelectedCompany() {
        Warehouse warehouse = mock(Warehouse.class);

        when(warehouse.isActive()).thenReturn(true);

        when(warehouseRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID,
                COMPANY_ID,
                WAREHOUSE_ID
        )).thenReturn(Optional.of(warehouse));

        Warehouse result = service.validateWarehouse(
                TENANT_ID,
                COMPANY_ID,
                WAREHOUSE_ID
        );

        assertSame(warehouse, result);

        verify(warehouseRepository)
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID,
                        COMPANY_ID,
                        WAREHOUSE_ID
                );

        verifyNoInteractions(warehouseLocationRepository);
    }

    @Test
    void shouldValidateCompanyWarehouseWithoutLocation() {
        Warehouse warehouse = mock(Warehouse.class);

        when(warehouse.isActive()).thenReturn(true);

        when(warehouseRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID,
                COMPANY_ID,
                WAREHOUSE_ID
        )).thenReturn(Optional.of(warehouse));

        assertDoesNotThrow(
                () -> service.validate(
                        TENANT_ID,
                        COMPANY_ID,
                        WAREHOUSE_ID,
                        null
                )
        );

        verify(warehouseRepository)
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID,
                        COMPANY_ID,
                        WAREHOUSE_ID
                );

        verifyNoInteractions(warehouseLocationRepository);
    }
}
