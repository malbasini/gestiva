package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferQuantityTest {

    private static final String EXPECTED_MESSAGE =
            "La quantità da trasferire deve essere maggiore di zero.";

    private final InventoryLocationTransferService service =
            new InventoryLocationTransferService(
                    org.mockito.Mockito.mock(
                            com.gestiva.security.usercontext.TenantContext.class),
                    org.mockito.Mockito.mock(
                            com.gestiva.security.company.context.CompanyContext.class),
                    org.mockito.Mockito.mock(
                            InventoryLocationValidationService.class)
            );

    private void assertInvalidQuantity(BigDecimal quantity) {

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
                        LocalDate.of(2026, 11, 21),
                        "COLLAUDO-ALD-INVALID-QUANTITY"
                )
        );

        assertEquals(
                EXPECTED_MESSAGE,
                exception.getMessage()
        );
    }

    @Test
    void mustRejectNullQuantity() {
        assertInvalidQuantity(null);
    }

    @Test
    void mustRejectZeroQuantity() {
        assertInvalidQuantity(new BigDecimal("0.000"));
    }

    @Test
    void mustRejectNegativeQuantity() {
        assertInvalidQuantity(new BigDecimal("-1.000"));
    }
}
