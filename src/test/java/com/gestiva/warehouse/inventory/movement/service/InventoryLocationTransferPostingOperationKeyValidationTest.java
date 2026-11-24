package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferPostingOperationKeyValidationTest {

    private final InventoryLocationTransferPostingService service =
            new InventoryLocationTransferPostingService();

    private BusinessException submit(
            String operationKey,
            BigDecimal quantity) {

        return assertThrows(
                BusinessException.class,
                () -> service.postTransfer(
                        2L,
                        1L,
                        1L,
                        10L,
                        100L,
                        200L,
                        quantity,
                        LocalDate.of(2026, 11, 24),
                        "Collaudo operationKey",
                        operationKey
                )
        );
    }

    @Test
    void mustRejectNullOperationKey() {
        var exception = submit(
                null, new BigDecimal("1.000")
        );
        assertTrue(
                exception.getMessage().contains("obbligatoria")
        );
    }

    @Test
    void mustRejectBlankOperationKey() {
        var exception = submit(
                "   ", new BigDecimal("1.000")
        );
        assertTrue(
                exception.getMessage().contains("obbligatoria")
        );
    }

    @Test
    void mustRejectOperationKeyLongerThan120Characters() {
        var exception = submit(
                "K".repeat(121),
                new BigDecimal("1.000")
        );
        assertTrue(
                exception.getMessage().contains("120")
        );
    }

    @Test
    void mustRejectLeadingOrTrailingSpaces() {
        var exception = submit(
                " transfer-001 ",
                new BigDecimal("1.000")
        );
        assertTrue(
                exception.getMessage().contains("spazi")
        );
    }

    @Test
    void validOperationKeyMustKeepPostingDisabled() {
        var exception = submit(
                "transfer-001",
                new BigDecimal("1.000")
        );
        assertEquals(
                "Registrazione dei trasferimenti FIFO "
                + "non ancora abilitata.",
                exception.getMessage()
        );
    }

    @Test
    void validOperationKeyMustPreserveQuantityValidation() {
        var exception = submit(
                "transfer-002",
                new BigDecimal("1.0001")
        );
        assertTrue(
                exception.getMessage().contains("tre decimali")
        );
    }
}
