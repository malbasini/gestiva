package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.location.entity.Warehouse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryLocationTransferInactiveSourceTest {

    @Test
    void mustRejectInactiveSourceBeforeValidatingTarget() {

        com.gestiva.security.usercontext.TenantContext tenantContext =
                mock(com.gestiva.security.usercontext.TenantContext.class);

        CompanyContext companyContext =
                mock(CompanyContext.class);

        InventoryLocationValidationService locationValidator =
                mock(InventoryLocationValidationService.class);

        Warehouse warehouse = mock(Warehouse.class);

        when(tenantContext.getCurrentTenantId())
                .thenReturn(2L);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(3L);

        when(locationValidator.validateWarehouse(2L, 3L, 2L))
                .thenReturn(warehouse);

        when(locationValidator.validateLocation(2L, 2L, 1L))
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
                        "COLLAUDO-ALP-INACTIVE-SOURCE"
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

        order.verifyNoMoreInteractions();
    }
}
