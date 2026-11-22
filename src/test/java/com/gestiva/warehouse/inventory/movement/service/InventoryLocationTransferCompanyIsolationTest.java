package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryLocationTransferCompanyIsolationTest {

    @Test
    void mustRejectCompanyDifferentFromActiveCompany() {

        com.gestiva.security.usercontext.TenantContext tenantContext =
                mock(com.gestiva.security.usercontext.TenantContext.class);

        CompanyContext companyContext =
                mock(CompanyContext.class);

        InventoryLocationValidationService locationValidator =
                mock(InventoryLocationValidationService.class);

        when(tenantContext.getCurrentTenantId())
                .thenReturn(2L);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(3L);

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
                        2L,
                        1L,
                        2L,
                        1L,
                        1L,
                        2L,
                        new BigDecimal("1.000"),
                        LocalDate.of(2026, 11, 22),
                        "COLLAUDO-ALN-COMPANY-MISMATCH"
                )
        );

        assertEquals(
                "La società indicata non corrisponde alla società attiva.",
                exception.getMessage()
        );

        verify(companyContext, times(1))
                .requireCurrentCompanyId();

        verifyNoInteractions(locationValidator);
    }
}
