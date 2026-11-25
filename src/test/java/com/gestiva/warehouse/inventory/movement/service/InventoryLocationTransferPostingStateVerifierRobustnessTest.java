package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransfer;

import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferPostingStateVerifierRobustnessTest {

    private static InventoryLocationTransfer postedTransfer() {
        var transfer = new InventoryLocationTransfer();

        transfer.setStatus("POSTED");
        transfer.setOutMovementId(101L);
        transfer.setInMovementId(102L);

        return transfer;
    }

    private static void accept(
            Consumer<InventoryLocationTransfer> changes) {

        var transfer = postedTransfer();
        changes.accept(transfer);

        assertDoesNotThrow(
                () -> InventoryLocationTransferPostingStateVerifier
                        .verifyPostedState(transfer)
        );
    }

    private static void reject(
            Consumer<InventoryLocationTransfer> changes,
            String expectedMessage) {

        var transfer = postedTransfer();
        changes.accept(transfer);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> InventoryLocationTransferPostingStateVerifier
                        .verifyPostedState(transfer)
        );

        assertEquals(expectedMessage, exception.getMessage());
    }

    // 1. Stato e riferimenti corretti.

    @Test
    void acceptsPostedWithDistinctPositiveMovementIds() {
        accept(transfer -> {});
    }

    // 2. Entità assente.

    @Test
    void rejectsNullTransfer() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> InventoryLocationTransferPostingStateVerifier
                        .verifyPostedState(null)
        );

        assertEquals(
                "Il trasferimento da verificare è obbligatorio.",
                exception.getMessage()
        );
    }

    // 3-7. Stati non validi.

    @Test
    void rejectsPendingStatus() {
        reject(
                transfer -> transfer.setStatus("PENDING"),
                "Il trasferimento è ancora in stato PENDING."
        );
    }

    @Test
    void rejectsLowercasePosted() {
        reject(
                transfer -> transfer.setStatus("posted"),
                "Lo stato del trasferimento non è valido per il riutilizzo."
        );
    }

    @Test
    void rejectsUnknownStatus() {
        reject(
                transfer -> transfer.setStatus("COMPLETED"),
                "Lo stato del trasferimento non è valido per il riutilizzo."
        );
    }

    @Test
    void rejectsNullStatus() {
        reject(
                transfer -> transfer.setStatus(null),
                "Lo stato del trasferimento non è valido per il riutilizzo."
        );
    }

    @Test
    void rejectsBlankStatus() {
        reject(
                transfer -> transfer.setStatus(" "),
                "Lo stato del trasferimento non è valido per il riutilizzo."
        );
    }

    // 8-10. Identificativo movimento OUT.

    @Test
    void rejectsNullOutMovementId() {
        reject(
                transfer -> transfer.setOutMovementId(null),
                "Il trasferimento POSTED non ha un movimento OUT valido."
        );
    }

    @Test
    void rejectsZeroOutMovementId() {
        reject(
                transfer -> transfer.setOutMovementId(0L),
                "Il trasferimento POSTED non ha un movimento OUT valido."
        );
    }

    @Test
    void rejectsNegativeOutMovementId() {
        reject(
                transfer -> transfer.setOutMovementId(-1L),
                "Il trasferimento POSTED non ha un movimento OUT valido."
        );
    }

    // 11-13. Identificativo movimento IN.

    @Test
    void rejectsNullInMovementId() {
        reject(
                transfer -> transfer.setInMovementId(null),
                "Il trasferimento POSTED non ha un movimento IN valido."
        );
    }

    @Test
    void rejectsZeroInMovementId() {
        reject(
                transfer -> transfer.setInMovementId(0L),
                "Il trasferimento POSTED non ha un movimento IN valido."
        );
    }

    @Test
    void rejectsNegativeInMovementId() {
        reject(
                transfer -> transfer.setInMovementId(-1L),
                "Il trasferimento POSTED non ha un movimento IN valido."
        );
    }

    // 14. OUT e IN non possono coincidere.

    @Test
    void rejectsSameOutAndInMovementId() {
        reject(
                transfer -> transfer.setInMovementId(101L),
                "I movimenti OUT e IN del trasferimento devono essere distinti."
        );
    }
}
