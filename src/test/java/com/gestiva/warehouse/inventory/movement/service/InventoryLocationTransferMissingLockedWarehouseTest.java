package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryLocationTransferMissingLockedWarehouseTest {

    @Test
    void mustRejectMissingWarehouseBeforeValidatingLocations() {

        WarehouseRepository warehouseRepository =
                mock(WarehouseRepository.class);

        TenantContext tenantContext =
                mock(TenantContext.class);

        CompanyContext companyContext =
                mock(CompanyContext.class);

        InventoryLocationValidationService locationValidator =
                mock(InventoryLocationValidationService.class);

        when(tenantContext.getCurrentTenantId())
                .thenReturn(2L);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(3L);

        when(warehouseRepository
                .findByTenantAndCompanyAndIdForUpdate(
                        2L, 3L, 2L
                ))
                .thenReturn(Optional.empty());

        InventoryLocationTransferService service =
                new InventoryLocationTransferService(
                        warehouseRepository,
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
                        3L,
                        2L,
                        1L,
                        1L,
                        2L,
                        new BigDecimal("1.000"),
                        LocalDate.of(2026, 11, 22),
                        "COLLAUDO-AMH-MISSING-WAREHOUSE"
                )
        );

        assertEquals(
                "Magazzino non trovato nella società attiva.",
                exception.getMessage()
        );

        InOrder order = inOrder(
                tenantContext,
                companyContext,
                warehouseRepository
        );

        order.verify(tenantContext)
                .getCurrentTenantId();

        order.verify(companyContext)
                .requireCurrentCompanyId();

        order.verify(warehouseRepository)
                .findByTenantAndCompanyAndIdForUpdate(
                        2L, 3L, 2L
                );

        order.verifyNoMoreInteractions();

        verifyNoInteractions(locationValidator);
    }
}
