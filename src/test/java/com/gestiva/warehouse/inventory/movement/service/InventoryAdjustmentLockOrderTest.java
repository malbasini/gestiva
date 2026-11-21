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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryAdjustmentLockOrderTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long WAREHOUSE = 10L;
    private static final Long ITEM = 20L;
    private static final Long MOVEMENT = 77L;

    @Mock InventoryMovementService movementService;
    @Mock InventoryValuationService valuationService;
    @Mock InventoryAvailabilityService availabilityService;
    @Mock WarehouseRepository warehouseRepository;
    @Mock CompanyContext companyContext;

    @InjectMocks InventoryAdjustmentService service;

    @Test
    void outboundMustAcquireWarehouseLockBeforeAvailabilityCheck() {

        InventoryAdjustmentForm form =
                new InventoryAdjustmentForm();

        form.setWarehouseId(WAREHOUSE);
        form.setLocationId(null);
        form.setItemId(ITEM);
        form.setMovementDate(LocalDate.of(2026, 11, 21));
        form.setAdjustmentType("OUT");
        form.setQuantity("2");
        form.setNotes("Test sequenza lock");

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        when(warehouseRepository
                .findByTenantAndCompanyAndIdForUpdate(
                        TENANT, COMPANY, WAREHOUSE
                ))
                .thenReturn(Optional.of(mock(Warehouse.class)));

        when(movementService.registerMovement(
                eq(TENANT),
                eq(COMPANY),
                eq(WAREHOUSE),
                isNull(),
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

        Long result = service.registerAdjustment(TENANT, form);

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
                        isNull(),
                        any(BigDecimal.class)
                );

        order.verify(movementService)
                .registerMovement(
                        eq(TENANT),
                        eq(COMPANY),
                        eq(WAREHOUSE),
                        isNull(),
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
