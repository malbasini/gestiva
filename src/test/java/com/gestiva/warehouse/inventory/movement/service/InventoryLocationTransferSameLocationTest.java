package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferSameLocationTest {

    @Test
    void mustRejectTransferBetweenIdenticalLocations() {

        InventoryLocationTransferService service =
                new InventoryLocationTransferService(
                    org.mockito.Mockito.mock(
                            com.gestiva.security.usercontext.TenantContext.class),
                    org.mockito.Mockito.mock(
                            com.gestiva.security.company.context.CompanyContext.class),
                    org.mockito.Mockito.mock(
                            InventoryLocationValidationService.class)
            );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.transferBetweenLocations(
                        2L,
                        3L,
                        2L,
                        1L,
                        1L,
                        1L,
                        new BigDecimal("1.000"),
                        LocalDate.of(2026, 11, 21),
                        "COLLAUDO-AKZ-SAME-LOCATION"
                )
        );

        assertEquals(
                "Le ubicazioni di origine e destinazione devono essere diverse.",
                exception.getMessage()
        );
    }
}
