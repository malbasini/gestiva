package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryLocationTransferTenantIsolationTest {

    @Test
    void mustRejectDifferentTenantBeforeAccessingCompanyOrWarehouse() {

        TenantContext tenantContext =
                mock(TenantContext.class);

        CompanyContext companyContext =
                mock(CompanyContext.class);

        InventoryLocationValidationService locationValidator =
                mock(InventoryLocationValidationService.class);

        when(tenantContext.getCurrentTenantId())
                .thenReturn(2L);

        InventoryLocationTransferService service =
                new InventoryLocationTransferService(
                        org.mockito.Mockito.mock(
                                com.gestiva.warehouse.location.repository.WarehouseRepository.class),
                        tenantContext,
                        companyContext,
                        locationValidator,
                        org.mockito.Mockito.mock(
                                com.gestiva.warehouse.inventory.valuation.service.InventoryAvailabilityService.class)
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.transferBetweenLocations(
                        5L,
                        3L,
                        2L,
                        1L,
                        1L,
                        2L,
                        new BigDecimal("1.000"),
                        LocalDate.of(2026, 11, 22),
                        "COLLAUDO-ALX-TENANT-MISMATCH"
                )
        );

        assertEquals(
                "Il tenant indicato non corrisponde al tenant autenticato.",
                exception.getMessage()
        );

        verify(tenantContext, times(1))
                .getCurrentTenantId();

        verifyNoMoreInteractions(tenantContext);

        verifyNoInteractions(
                companyContext,
                locationValidator
        );
    }
}
