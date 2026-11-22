package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferRequiredLocationsTest {

    private static final String EXPECTED_MESSAGE =
            "Le ubicazioni di origine e destinazione sono obbligatorie.";

    private final InventoryLocationTransferService service =
            new InventoryLocationTransferService(
                        org.mockito.Mockito.mock(
                                com.gestiva.warehouse.location.repository.WarehouseRepository.class),
                    org.mockito.Mockito.mock(
                            com.gestiva.security.usercontext.TenantContext.class),
                    org.mockito.Mockito.mock(
                            com.gestiva.security.company.context.CompanyContext.class),
                    org.mockito.Mockito.mock(
                            InventoryLocationValidationService.class),
                        org.mockito.Mockito.mock(
                                com.gestiva.warehouse.inventory.valuation.service.InventoryAvailabilityService.class)
            );

    @Test
    void mustRejectMissingSourceLocation() {

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.transferBetweenLocations(
                        2L,
                        3L,
                        2L,
                        1L,
                        null,
                        2L,
                        new BigDecimal("1.000"),
                        LocalDate.of(2026, 11, 21),
                        "COLLAUDO-ALB-SOURCE-NULL"
                )
        );

        assertEquals(EXPECTED_MESSAGE, exception.getMessage());
    }

    @Test
    void mustRejectMissingTargetLocation() {

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.transferBetweenLocations(
                        2L,
                        3L,
                        2L,
                        1L,
                        1L,
                        null,
                        new BigDecimal("1.000"),
                        LocalDate.of(2026, 11, 21),
                        "COLLAUDO-ALB-TARGET-NULL"
                )
        );

        assertEquals(EXPECTED_MESSAGE, exception.getMessage());
    }
}
