package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.warehouse.inventory.valuation.service.InventoryAvailabilityService;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.entity.WarehouseLocation;
import com.gestiva.warehouse.location.repository.WarehouseRepository;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryLocationTransferSourceAvailabilityTest {

    @Test
    void mustValidateSourceAvailabilityAfterWarehouseLock() {

        WarehouseRepository warehouseRepository =
                mock(WarehouseRepository.class);

        TenantContext tenantContext =
                mock(TenantContext.class);

        CompanyContext companyContext =
                mock(CompanyContext.class);

        InventoryLocationValidationService locationValidator =
                mock(InventoryLocationValidationService.class);

        InventoryAvailabilityService availabilityService =
                mock(InventoryAvailabilityService.class);

        Warehouse warehouse = mock(Warehouse.class);

        WarehouseLocation sourceLocation =
                mock(WarehouseLocation.class);

        WarehouseLocation targetLocation =
                mock(WarehouseLocation.class);

        BigDecimal quantity = new BigDecimal("12.000");

        when(tenantContext.getCurrentTenantId())
                .thenReturn(2L);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(3L);

        when(warehouseRepository
                .findByTenantAndCompanyAndIdForUpdate(
                        2L, 3L, 2L
                ))
                .thenReturn(Optional.of(warehouse));

        when(locationValidator.validateWarehouse(
                2L, 3L, 2L
        )).thenReturn(warehouse);

        when(locationValidator.validateLocation(
                2L, 2L, 1L
        )).thenReturn(sourceLocation);

        when(locationValidator.validateLocation(
                2L, 2L, 2L
        )).thenReturn(targetLocation);

        // Simula disponibilità insufficiente.
        // Nessun accesso reale al database.
        doThrow(new BusinessException(
                "STOP-AMM-DISPONIBILITA-ORIGINE"
        )).when(availabilityService)
                .validateAvailabilityByCompanyAndWarehouse(
                        2L,
                        3L,
                        2L,
                        1L,
                        1L,
                        quantity
                );

        InventoryLocationTransferService service =
                new InventoryLocationTransferService(
                        warehouseRepository,
                        tenantContext,
                        companyContext,
                        locationValidator,
                        availabilityService
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
                        quantity,
                        LocalDate.of(2026, 11, 22),
                        "COLLAUDO-AMM-SOURCE-AVAILABILITY"
                )
        );

        assertEquals(
                "STOP-AMM-DISPONIBILITA-ORIGINE",
                exception.getMessage()
        );

        InOrder order = inOrder(
                tenantContext,
                companyContext,
                warehouseRepository,
                locationValidator,
                availabilityService
        );

        order.verify(tenantContext)
                .getCurrentTenantId();

        order.verify(companyContext)
                .requireCurrentCompanyId();

        order.verify(warehouseRepository)
                .findByTenantAndCompanyAndIdForUpdate(
                        2L, 3L, 2L
                );

        order.verify(locationValidator)
                .validateWarehouse(2L, 3L, 2L);

        order.verify(locationValidator)
                .validateLocation(2L, 2L, 1L);

        order.verify(locationValidator)
                .validateLocation(2L, 2L, 2L);

        order.verify(availabilityService)
                .validateAvailabilityByCompanyAndWarehouse(
                        2L,
                        3L,
                        2L,
                        1L,
                        1L,
                        quantity
                );

        order.verifyNoMoreInteractions();
    }
}
