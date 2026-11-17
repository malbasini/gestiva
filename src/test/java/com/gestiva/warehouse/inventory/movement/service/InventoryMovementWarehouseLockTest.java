package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.repository.WarehouseRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryMovementWarehouseLockTest {

    private static final Long TENANT_ID = 2L;
    private static final Long COMPANY_ID = 1L;
    private static final Long WAREHOUSE_ID = 10L;
    private static final Long LOCATION_ID = 30L;
    private static final Long ITEM_ID = 50L;

    @Mock
    private InventoryMovementRepository movementRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InventoryLocationValidationService locationValidationService;

    @Mock
    private InventoryValuationService valuationService;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private InventoryMovementService service;

    private Long register(Long tenantId, Long companyId) {
        return service.registerMovement(
                tenantId,
                companyId,
                WAREHOUSE_ID,
                LOCATION_ID,
                ITEM_ID,
                LocalDate.of(2026, 11, 17),
                "IN",
                "TEST_LOCK",
                new BigDecimal("1.000"),
                new BigDecimal("10.0000"),
                "TEST",
                1L,
                "Test isolamento lock"
        );
    }

    @Test
    void missingTenantCannotAcquireLockOrRegisterMovement() {
        assertThrows(BusinessException.class, () ->
                register(null, COMPANY_ID)
        );

        verifyNoInteractions(
                warehouseRepository,
                locationValidationService,
                itemRepository,
                movementRepository,
                valuationService
        );
    }

    @Test
    void missingCompanyCannotAcquireLockOrRegisterMovement() {
        assertThrows(BusinessException.class, () ->
                register(TENANT_ID, null)
        );

        verifyNoInteractions(
                warehouseRepository,
                locationValidationService,
                itemRepository,
                movementRepository,
                valuationService
        );
    }

    @Test
    void unauthorizedWarehouseBlocksMovementBeforeValidation() {
        when(warehouseRepository.findByTenantAndCompanyAndIdForUpdate(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID
        )).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                register(TENANT_ID, COMPANY_ID)
        );

        verify(warehouseRepository).findByTenantAndCompanyAndIdForUpdate(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID
        );

        verifyNoInteractions(
                locationValidationService,
                itemRepository,
                movementRepository,
                valuationService
        );
    }

    @Test
    void warehouseLockPrecedesLocationValidationAndNoSaveOnFailure() {
        Warehouse warehouse = mock(Warehouse.class);

        when(warehouseRepository.findByTenantAndCompanyAndIdForUpdate(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID
        )).thenReturn(Optional.of(warehouse));

        doThrow(new BusinessException("Ubicazione inattiva."))
                .when(locationValidationService)
                .validate(
                        TENANT_ID,
                        COMPANY_ID,
                        WAREHOUSE_ID,
                        LOCATION_ID
                );

        assertThrows(BusinessException.class, () ->
                register(TENANT_ID, COMPANY_ID)
        );

        InOrder order = inOrder(
                warehouseRepository,
                locationValidationService
        );

        order.verify(warehouseRepository)
                .findByTenantAndCompanyAndIdForUpdate(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID
                );

        order.verify(locationValidationService)
                .validate(
                        TENANT_ID,
                        COMPANY_ID,
                        WAREHOUSE_ID,
                        LOCATION_ID
                );

        verifyNoInteractions(
                itemRepository,
                movementRepository,
                valuationService
        );
    }
}
