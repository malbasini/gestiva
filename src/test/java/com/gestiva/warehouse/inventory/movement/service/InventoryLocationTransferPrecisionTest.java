package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferPrecisionTest {

    private static final String EXPECTED_MESSAGE =
            "La quantità da trasferire non può superare tre decimali.";

    private final InventoryLocationTransferService service =
            new InventoryLocationTransferService(
                    org.mockito.Mockito.mock(
                            com.gestiva.security.usercontext.TenantContext.class),
                    org.mockito.Mockito.mock(
                            com.gestiva.security.company.context.CompanyContext.class),
                    org.mockito.Mockito.mock(
                            InventoryLocationValidationService.class)
            );

    private void assertInvalidPrecision(String quantity) {

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.transferBetweenLocations(
                        2L,
                        3L,
                        2L,
                        1L,
                        1L,
                        2L,
                        new BigDecimal(quantity),
                        LocalDate.of(2026, 11, 21),
                        "COLLAUDO-ALF-INVALID-PRECISION"
                )
        );

        assertEquals(
                EXPECTED_MESSAGE,
                exception.getMessage()
        );
    }

    @Test
    void mustRejectFourthDecimalDigit() {
        assertInvalidPrecision("1.0001");
    }

    @Test
    void mustRejectPositiveQuantityBelowMinimumUnit() {
        assertInvalidPrecision("0.0001");
    }

    @Test
    void mustRejectFiveDecimalDigits() {
        assertInvalidPrecision("1.23456");
    }
}
