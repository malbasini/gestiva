package com.gestiva.warehouse.inventory.movement.controller;

import com.gestiva.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferSourceSelectionTest {

    @Test
    void explicitUnlocatedSelectionMustResolveToNull() {

        assertEquals(
                "UNLOCATED",
                InventoryLocationTransferSourceSelection.UNLOCATED
        );

        assertNull(
                InventoryLocationTransferSourceSelection
                        .parse("UNLOCATED")
        );
    }

    @Test
    void physicalLocationMustResolveToPositiveIdentifier() {

        assertEquals(
                Long.valueOf(1L),
                InventoryLocationTransferSourceSelection.parse("1")
        );

        assertEquals(
                Long.valueOf(2L),
                InventoryLocationTransferSourceSelection.parse("2")
        );

        assertEquals(
                Long.valueOf(5L),
                InventoryLocationTransferSourceSelection.parse("5")
        );
    }

    @Test
    void missingSelectionMustBeRejected() {

        assertMissing(null);
    }

    @Test
    void emptySelectionMustBeRejected() {

        assertMissing("");
    }

    @Test
    void blankSelectionMustBeRejected() {

        assertMissing("   ");
        assertMissing("\t");
        assertMissing("\n");
    }

    @Test
    void zeroIdentifierMustBeRejected() {

        assertInvalid("0");
    }

    @Test
    void negativeIdentifiersMustBeRejected() {

        assertInvalid("-1");
        assertInvalid("-25");
    }

    @Test
    void textualNullMustNotResolveToUnlocated() {

        assertInvalid("null");
        assertInvalid("NULL");
    }

    @Test
    void unlocatedTokenMustBeCaseSensitive() {

        assertInvalid("unlocated");
        assertInvalid("Unlocated");
    }

    @Test
    void unknownTextMustBeRejected() {

        assertInvalid("ABC");
        assertInvalid("NONE");
        assertInvalid("ALL");
    }

    @Test
    void fractionalIdentifiersMustBeRejected() {

        assertInvalid("1.5");
        assertInvalid("2.0");
        assertInvalid("1e3");
    }

    @Test
    void nonCanonicalIdentifiersMustBeRejected() {

        assertInvalid("+1");
        assertInvalid("01");
        assertInvalid("001");
        assertInvalid("1 ");
        assertInvalid(" 1");
        assertInvalid(" UNLOCATED");
        assertInvalid("UNLOCATED ");
    }

    @Test
    void maximumLongIdentifierMustBeAccepted() {

        assertEquals(
                Long.valueOf(Long.MAX_VALUE),
                InventoryLocationTransferSourceSelection.parse(
                        "9223372036854775807"
                )
        );
    }

    @Test
    void overflowingIdentifiersMustBeRejected() {

        assertInvalid("9223372036854775808");
        assertInvalid("99999999999999999999999999999");
    }

    private static void assertMissing(String selection) {

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> InventoryLocationTransferSourceSelection
                        .parse(selection)
        );

        assertEquals(
                "Seleziona l'ubicazione di origine.",
                exception.getMessage()
        );
    }

    private static void assertInvalid(String selection) {

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> InventoryLocationTransferSourceSelection
                        .parse(selection)
        );

        assertEquals(
                "Selezione dell'ubicazione di origine non valida.",
                exception.getMessage()
        );
    }
}
