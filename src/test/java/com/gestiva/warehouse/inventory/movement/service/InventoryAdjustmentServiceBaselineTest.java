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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryAdjustmentServiceBaselineTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long WAREHOUSE = 10L;
    private static final Long ITEM = 20L;
    private static final Long MOVEMENT = 77L;

    @Mock
    InventoryMovementService movementService;

    @Mock
    InventoryValuationService valuationService;

    @Mock
    InventoryAvailabilityService availabilityService;

    @Mock
    CompanyContext companyContext;

    @Mock
    WarehouseRepository warehouseRepository;

    @InjectMocks
    InventoryAdjustmentService service;

    private InventoryAdjustmentForm form(String type) {
        InventoryAdjustmentForm form = new InventoryAdjustmentForm();
        form.setWarehouseId(WAREHOUSE);
        form.setItemId(ITEM);
        form.setLocationId(null);
        form.setMovementDate(LocalDate.of(2026, 11, 21));
        form.setAdjustmentType(type);
        form.setQuantity("2");
        form.setUnitCost("10");
        form.setNotes("Baseline test");
        return form;
    }

    @Test
    void positiveAdjustmentWithoutLocationUsesInboundFifo() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        when(movementService.registerMovement(
                eq(TENANT), eq(COMPANY),
                eq(WAREHOUSE), isNull(),
                eq(ITEM), any(LocalDate.class),
                eq("ADJUSTMENT_IN"),
                eq("MANUAL_ADJUSTMENT_IN"),
                any(BigDecimal.class),
                any(BigDecimal.class),
                eq("MANUAL_INVENTORY"),
                isNull(), anyString()
        )).thenReturn(MOVEMENT);

        Long result = service.registerAdjustment(
                TENANT, form("IN")
        );

        assertEquals(MOVEMENT, result);

        verify(valuationService).applyInboundValuation(
                TENANT, COMPANY, WAREHOUSE, MOVEMENT
        );

        verify(valuationService, never())
                .applyOutboundValuation(
                        anyLong(), anyLong(),
                        anyLong(), anyLong()
                );

        verifyNoInteractions(availabilityService);
    }

    @Test
    void negativeAdjustmentWithoutLocationUsesOutboundFifo() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        when(warehouseRepository.findByTenantAndCompanyAndIdForUpdate(
                TENANT, COMPANY, WAREHOUSE
        )).thenReturn(Optional.of(mock(Warehouse.class)));

        when(movementService.registerMovement(
                eq(TENANT), eq(COMPANY),
                eq(WAREHOUSE), isNull(),
                eq(ITEM), any(LocalDate.class),
                eq("ADJUSTMENT_OUT"),
                eq("MANUAL_ADJUSTMENT_OUT"),
                any(BigDecimal.class),
                isNull(),
                eq("MANUAL_INVENTORY"),
                isNull(), anyString()
        )).thenReturn(MOVEMENT);

        Long result = service.registerAdjustment(
                TENANT, form("OUT")
        );

        assertEquals(MOVEMENT, result);

        verify(availabilityService)
                .validateAvailabilityByCompanyAndWarehouse(
                        eq(TENANT), eq(COMPANY),
                        eq(WAREHOUSE), eq(ITEM),
                        isNull(), any(BigDecimal.class)
                );

        verify(valuationService).applyOutboundValuation(
                TENANT, COMPANY, WAREHOUSE, MOVEMENT
        );

        verify(valuationService, never())
                .applyInboundValuation(
                        anyLong(), anyLong(),
                        anyLong(), anyLong()
                );
    }
}
