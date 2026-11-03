package com.gestiva.warehouse.inventory.valuation.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryAverageBalanceRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.item.repository.ItemRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryAvailabilityLegacyGuardTest {

    private static final Long TENANT_ID = 999L;
    private static final Long WAREHOUSE_ID = 88L;
    private static final Long ITEM_ID = 77L;
    private static final Long LOCATION_ID = 66L;

    @Mock InventoryLayerRepository inventoryLayerRepository;
    @Mock ItemRepository itemRepository;
    @Mock TenantRepository tenantRepository;
    @Mock InventoryAverageBalanceRepository inventoryAverageBalanceRepository;
    @Mock InventoryMovementRepository inventoryMovementRepository;

    @InjectMocks
    InventoryAvailabilityService service;

    private void verifyRepositoriesUntouched() {
        verifyNoInteractions(
                inventoryLayerRepository,
                itemRepository,
                tenantRepository,
                inventoryAverageBalanceRepository,
                inventoryMovementRepository
        );
    }

    private void assertLegacyBlocked(
            org.junit.jupiter.api.function.Executable action
    ) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                action
        );

        assertTrue(
                exception.getMessage().contains(
                        "Percorso legacy di disponibilità disabilitato."
                )
        );

        verifyRepositoriesUntouched();
    }

    @Test
    void shouldRejectLegacyAvailableQty() {
        assertLegacyBlocked(
                () -> service.getAvailableQty(
                        TENANT_ID, ITEM_ID
                )
        );
    }

    @Test
    void shouldRejectLegacyValidateAvailability() {
        assertLegacyBlocked(
                () -> service.validateAvailability(
                        TENANT_ID,
                        ITEM_ID,
                        new BigDecimal("5.000")
                )
        );
    }

    @Test
    void shouldRejectLegacyAvailableQtyByWarehouse() {
        assertLegacyBlocked(
                () -> service.getAvailableQtyByWarehouse(
                        TENANT_ID,
                        WAREHOUSE_ID,
                        ITEM_ID,
                        LOCATION_ID
                )
        );
    }

    @Test
    void shouldRejectLegacyValidateAvailabilityByWarehouse() {
        assertLegacyBlocked(
                () -> service.validateAvailabilityByWarehouse(
                        TENANT_ID,
                        WAREHOUSE_ID,
                        ITEM_ID,
                        LOCATION_ID,
                        new BigDecimal("5.000")
                )
        );
    }
}
