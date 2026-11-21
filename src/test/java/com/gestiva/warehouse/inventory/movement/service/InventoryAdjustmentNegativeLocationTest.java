package com.gestiva.warehouse.inventory.movement.service;

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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryAdjustmentNegativeLocationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long WAREHOUSE = 10L;
    private static final Long LOCATION = 101L;
    private static final Long ITEM = 20L;
    private static final Long MOVEMENT = 77L;

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
    void negativeAdjustmentMustUseSelectedLocationUnderWarehouseLock() {

        InventoryAdjustmentForm form =
                new InventoryAdjustmentForm();

        form.setWarehouseId(WAREHOUSE);
        form.setLocationId(LOCATION);
        form.setItemId(ITEM);
        form.setAdjustmentType("OUT");
        form.setMovementDate(LocalDate.of(2026, 11, 21));
        form.setQuantity("2");
        form.setNotes("Rettifica negativa ubicata");

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        // Lenient soltanto durante la fase RED:
        // il servizio attuale si ferma prima di queste chiamate.
        lenient().when(warehouseRepository
                .findByTenantAndCompanyAndIdForUpdate(
                        TENANT, COMPANY, WAREHOUSE
                ))
                .thenReturn(Optional.of(mock(Warehouse.class)));

        lenient().when(movementService.registerMovement(
                eq(TENANT),
                eq(COMPANY),
                eq(WAREHOUSE),
                eq(LOCATION),
                eq(ITEM),
                any(LocalDate.class),
                eq("ADJUSTMENT_OUT"),
                eq("MANUAL_ADJUSTMENT_OUT"),
                any(BigDecimal.class),
                isNull(),
                eq("MANUAL_INVENTORY"),
                isNull(),
                anyString()
        )).thenReturn(MOVEMENT);

        Long result = service.registerAdjustment(
                TENANT, form
        );

        assertEquals(MOVEMENT, result);

        InOrder order = inOrder(
                warehouseRepository,
                availabilityService,
                movementService,
                valuationService
        );

        order.verify(warehouseRepository)
                .findByTenantAndCompanyAndIdForUpdate(
                        TENANT, COMPANY, WAREHOUSE
                );

        order.verify(availabilityService)
                .validateAvailabilityByCompanyAndWarehouse(
                        eq(TENANT),
                        eq(COMPANY),
                        eq(WAREHOUSE),
                        eq(ITEM),
                        eq(LOCATION),
                        any(BigDecimal.class)
                );

        order.verify(movementService)
                .registerMovement(
                        eq(TENANT),
                        eq(COMPANY),
                        eq(WAREHOUSE),
                        eq(LOCATION),
                        eq(ITEM),
                        any(LocalDate.class),
                        eq("ADJUSTMENT_OUT"),
                        eq("MANUAL_ADJUSTMENT_OUT"),
                        any(BigDecimal.class),
                        isNull(),
                        eq("MANUAL_INVENTORY"),
                        isNull(),
                        anyString()
                );

        order.verify(valuationService)
                .applyOutboundValuation(
                        TENANT, COMPANY, WAREHOUSE, MOVEMENT
                );
    }
}
