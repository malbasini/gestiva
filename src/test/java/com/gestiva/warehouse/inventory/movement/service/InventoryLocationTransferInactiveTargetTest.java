package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.entity.WarehouseLocation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryLocationTransferInactiveTargetTest {

    @Test
    void mustRejectInactiveTargetAfterValidatingSource() {

        com.gestiva.security.usercontext.TenantContext tenantContext =
                mock(com.gestiva.security.usercontext.TenantContext.class);

        CompanyContext companyContext =
                mock(CompanyContext.class);

        InventoryLocationValidationService locationValidator =
                mock(InventoryLocationValidationService.class);

        Warehouse warehouse = mock(Warehouse.class);

        WarehouseLocation sourceLocation =
                mock(WarehouseLocation.class);

        when(tenantContext.getCurrentTenantId())
                .thenReturn(2L);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(3L);

        when(locationValidator.validateWarehouse(2L, 3L, 2L))
                .thenReturn(warehouse);

        when(locationValidator.validateLocation(2L, 2L, 1L))
                .thenReturn(sourceLocation);

        when(locationValidator.validateLocation(2L, 2L, 2L))
                .thenThrow(new BusinessException(
                        "L'ubicazione selezionata non è attiva."
                ));

        InventoryLocationTransferService service =
                new InventoryLocationTransferService(
                    tenantContext,
                    companyContext,
                        locationValidator
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.transferBetweenLocations(
                        2L,
                        3L,
                        2L,
                        1L,
                        1L,
                        2L,
                        new BigDecimal("1.000"),
                        LocalDate.of(2026, 11, 22),
                        "COLLAUDO-ALR-INACTIVE-TARGET"
                )
        );

        assertEquals(
                "L'ubicazione selezionata non è attiva.",
                exception.getMessage()
        );

        var order = inOrder(companyContext, locationValidator);

        order.verify(companyContext)
                .requireCurrentCompanyId();

        order.verify(locationValidator)
                .validateWarehouse(2L, 3L, 2L);

        order.verify(locationValidator)
                .validateLocation(2L, 2L, 1L);

        order.verify(locationValidator)
                .validateLocation(2L, 2L, 2L);

        order.verifyNoMoreInteractions();
    }
}
