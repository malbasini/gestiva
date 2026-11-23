package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferPostingValidationTest {

    private final InventoryLocationTransferPostingService service =
            new InventoryLocationTransferPostingService();

    private void assertRejected(
            Long tenantId,
            Long companyId,
            Long warehouseId,
            Long itemId,
            Long sourceLocationId,
            Long targetLocationId,
            BigDecimal quantity,
            LocalDate movementDate,
            String expectedMessage) {

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.postTransfer(
                        tenantId,
                        companyId,
                        warehouseId,
                        itemId,
                        sourceLocationId,
                        targetLocationId,
                        quantity,
                        movementDate,
                        "TEST-ANO"
                )
        );

        assertEquals(
                expectedMessage,
                exception.getMessage()
        );
    }

    @Test
    void missingRequiredContextMustBeRejected() {

        assertRejected(
                null, 3L, 2L, 7L, 101L, 102L,
                new BigDecimal("3.000"),
                LocalDate.of(2026, 11, 23),
                "Tenant, società, magazzino, articolo e data sono obbligatori."
        );
    }

    @Test
    void sameSourceAndTargetMustBeRejected() {

        assertRejected(
                2L, 3L, 2L, 7L, 101L, 101L,
                new BigDecimal("3.000"),
                LocalDate.of(2026, 11, 23),
                "Le ubicazioni di origine e destinazione devono essere diverse."
        );
    }

    @Test
    void nonPositiveQuantityMustBeRejected() {

        assertRejected(
                2L, 3L, 2L, 7L, 101L, 102L,
                BigDecimal.ZERO,
                LocalDate.of(2026, 11, 23),
                "La quantità da trasferire deve essere maggiore di zero."
        );
    }

    @Test
    void quantityWithMoreThanThreeDecimalsMustBeRejected() {

        assertRejected(
                2L, 3L, 2L, 7L, 101L, 102L,
                new BigDecimal("1.0001"),
                LocalDate.of(2026, 11, 23),
                "La quantità da trasferire non può superare tre decimali."
        );
    }
}
