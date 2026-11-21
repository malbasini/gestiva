package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.inventory.movement.web.InventoryAdjustmentForm;
import com.gestiva.warehouse.inventory.valuation.service.InventoryAvailabilityService;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryAdjustmentLocationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long WAREHOUSE = 10L;
    private static final Long LOCATION = 101L;
    private static final Long ITEM = 20L;
    private static final Long MOVEMENT = 77L;

    @Mock InventoryMovementService movementService;
    @Mock InventoryValuationService valuationService;
    @Mock InventoryAvailabilityService availabilityService;
    @Mock CompanyContext companyContext;

    @InjectMocks InventoryAdjustmentService service;

    @Test
    void positiveAdjustmentMustPropagateLocationToFifoMovement() {

        InventoryAdjustmentForm form =
                new InventoryAdjustmentForm();

        form.setWarehouseId(WAREHOUSE);
        form.setLocationId(LOCATION);
        form.setItemId(ITEM);
        form.setAdjustmentType("IN");
        form.setMovementDate(LocalDate.of(2026, 11, 21));
        form.setQuantity("2");
        form.setUnitCost("10");
        form.setNotes("Rettifica ubicata");

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        when(movementService.registerMovement(
                eq(TENANT),
                eq(COMPANY),
                eq(WAREHOUSE),
                eq(LOCATION),
                eq(ITEM),
                any(LocalDate.class),
                eq("ADJUSTMENT_IN"),
                eq("MANUAL_ADJUSTMENT_IN"),
                any(BigDecimal.class),
                any(BigDecimal.class),
                eq("MANUAL_INVENTORY"),
                isNull(),
                anyString()
        )).thenReturn(MOVEMENT);

        Long result = service.registerAdjustment(
                TENANT, form
        );

        assertEquals(MOVEMENT, result);

        verify(movementService).registerMovement(
                eq(TENANT),
                eq(COMPANY),
                eq(WAREHOUSE),
                eq(LOCATION),
                eq(ITEM),
                any(LocalDate.class),
                eq("ADJUSTMENT_IN"),
                eq("MANUAL_ADJUSTMENT_IN"),
                any(BigDecimal.class),
                any(BigDecimal.class),
                eq("MANUAL_INVENTORY"),
                isNull(),
                anyString()
        );

        verify(valuationService).applyInboundValuation(
                TENANT, COMPANY, WAREHOUSE, MOVEMENT
        );

        verifyNoInteractions(availabilityService);
    }
}
