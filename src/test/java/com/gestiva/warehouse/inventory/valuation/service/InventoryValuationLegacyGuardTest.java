package com.gestiva.warehouse.inventory.valuation.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerConsumptionRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryAverageBalanceRepository;
import com.gestiva.warehouse.location.repository.WarehouseRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryValuationLegacyGuardTest {

    private static final Long TENANT_ID = 999L;
    private static final Long MOVEMENT_ID = 77L;

    @Mock TenantRepository tenantRepository;
    @Mock InventoryMovementRepository inventoryMovementRepository;
    @Mock InventoryLayerRepository inventoryLayerRepository;
    @Mock InventoryLayerConsumptionRepository inventoryLayerConsumptionRepository;
    @Mock InventoryAverageBalanceRepository inventoryAverageBalanceRepository;
    @Mock WarehouseRepository warehouseRepository;

    @InjectMocks
    InventoryValuationService service;

    private void verifyRepositoriesUntouched() {
        verifyNoInteractions(
                tenantRepository,
                inventoryMovementRepository,
                inventoryLayerRepository,
                inventoryLayerConsumptionRepository,
                inventoryAverageBalanceRepository,
                warehouseRepository
        );
    }

    @Test
    void shouldRejectLegacyInboundBeforeRepositoryAccess() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.applyInboundValuation(
                        TENANT_ID, MOVEMENT_ID
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "Percorso legacy di valorizzazione disabilitato"
                )
        );

        verifyRepositoriesUntouched();
    }

    @Test
    void shouldRejectLegacyOutboundBeforeRepositoryAccess() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.applyOutboundValuation(
                        TENANT_ID, MOVEMENT_ID
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "Percorso legacy di valorizzazione disabilitato"
                )
        );

        verifyRepositoriesUntouched();
    }
}
