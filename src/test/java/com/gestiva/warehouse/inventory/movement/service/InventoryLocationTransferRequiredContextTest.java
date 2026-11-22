package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferRequiredContextTest {

    private static final String EXPECTED_MESSAGE =
            "Tenant, società, magazzino, articolo e data sono obbligatori.";

    private final InventoryLocationTransferService service =
            new InventoryLocationTransferService(
                    org.mockito.Mockito.mock(
                            com.gestiva.security.usercontext.TenantContext.class),
                    org.mockito.Mockito.mock(
                            com.gestiva.security.company.context.CompanyContext.class),
                    org.mockito.Mockito.mock(
                            InventoryLocationValidationService.class)
            );

    private void assertInvalidContext(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long itemId,
            LocalDate movementDate) {

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.transferBetweenLocations(
                        tenantId,
                        companyId,
                        warehouseId,
                        itemId,
                        1L,
                        2L,
                        new BigDecimal("1.000"),
                        movementDate,
                        "COLLAUDO-ALH-REQUIRED-CONTEXT"
                )
        );

        assertEquals(
                EXPECTED_MESSAGE,
                exception.getMessage()
        );
    }

    @Test
    void mustRejectMissingTenant() {
        assertInvalidContext(
                null, 3L, 2L, 1L,
                LocalDate.of(2026, 11, 22)
        );
    }

    @Test
    void mustRejectMissingCompany() {
        assertInvalidContext(
                2L, null, 2L, 1L,
                LocalDate.of(2026, 11, 22)
        );
    }

    @Test
    void mustRejectMissingWarehouse() {
        assertInvalidContext(
                2L, 3L, null, 1L,
                LocalDate.of(2026, 11, 22)
        );
    }

    @Test
    void mustRejectMissingItem() {
        assertInvalidContext(
                2L, 3L, 2L, null,
                LocalDate.of(2026, 11, 22)
        );
    }

    @Test
    void mustRejectMissingMovementDate() {
        assertInvalidContext(
                2L, 3L, 2L, 1L, null
        );
    }
}
