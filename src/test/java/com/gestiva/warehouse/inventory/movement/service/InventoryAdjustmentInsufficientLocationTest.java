package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.inventory.movement.web.InventoryAdjustmentForm;
import com.gestiva.warehouse.inventory.valuation.service.InventoryAvailabilityService;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;
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
class InventoryAdjustmentInsufficientLocationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long WAREHOUSE = 10L;
    private static final Long LOCATION = 101L;
    private static final Long ITEM = 20L;

    @Mock
    InventoryMovementService movementService;

    @Mock
    InventoryValuationService valuationService;

    @Mock
    InventoryAvailabilityService availabilityService;

    @Mock
    WarehouseRepository warehouseRepository;

    @Mock
    CompanyContext companyContext;

    @InjectMocks
    InventoryAdjustmentService service;

    @Test
    void insufficientLocationStockMustRejectBeforeMovement() {

        InventoryAdjustmentForm form =
                new InventoryAdjustmentForm();

        form.setWarehouseId(WAREHOUSE);
        form.setLocationId(LOCATION);
        form.setItemId(ITEM);
        form.setAdjustmentType("OUT");
        form.setMovementDate(LocalDate.of(2026, 11, 21));
        form.setQuantity("5");
        form.setNotes("Test giacenza insufficiente");

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        when(warehouseRepository
                .findByTenantAndCompanyAndIdForUpdate(
                        TENANT, COMPANY, WAREHOUSE
                ))
                .thenReturn(Optional.of(mock(Warehouse.class)));

        doThrow(new BusinessException(
                "Disponibilità insufficiente nell'ubicazione."
        )).when(availabilityService)
                .validateAvailabilityByCompanyAndWarehouse(
                        TENANT,
                        COMPANY,
                        WAREHOUSE,
                        ITEM,
                        LOCATION,
                        new BigDecimal("5")
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.registerAdjustment(TENANT, form)
        );

        assertTrue(
                exception.getMessage().contains(
                        "Disponibilità insufficiente"
                )
        );

        InOrder order = inOrder(
                warehouseRepository,
                availabilityService
        );

        order.verify(warehouseRepository)
                .findByTenantAndCompanyAndIdForUpdate(
                        TENANT, COMPANY, WAREHOUSE
                );

        order.verify(availabilityService)
                .validateAvailabilityByCompanyAndWarehouse(
                        TENANT,
                        COMPANY,
                        WAREHOUSE,
                        ITEM,
                        LOCATION,
                        new BigDecimal("5")
                );

        verifyNoInteractions(
                movementService,
                valuationService
        );
    }
}
