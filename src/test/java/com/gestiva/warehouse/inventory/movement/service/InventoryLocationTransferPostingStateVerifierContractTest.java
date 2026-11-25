package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransfer;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferPostingStateVerifierContractTest {

    @Test
    void verifierMustExposePurePostedStateContract()
            throws Exception {

        final Class<?> verifier;

        try {
            verifier = Class.forName(
                    "com.gestiva.warehouse.inventory.movement.service."
                    + "InventoryLocationTransferPostingStateVerifier"
            );
        } catch (ClassNotFoundException exception) {
            fail(
                    "STOP-APM-POSTING-STATE-VERIFIER-NON-IMPLEMENTATO"
            );
            return;
        }

        var method = verifier.getDeclaredMethod(
                "verifyPostedState",
                InventoryLocationTransfer.class
        );

        assertTrue(
                Modifier.isPublic(method.getModifiers()),
                "STOP-APM-VERIFIER-NON-PUBBLICO"
        );

        assertTrue(
                Modifier.isStatic(method.getModifiers()),
                "STOP-APM-VERIFIER-NON-STATICO"
        );

        assertEquals(
                void.class,
                method.getReturnType(),
                "STOP-APM-RETURN-TYPE-NON-CONFORME"
        );

        assertTrue(
                Modifier.isFinal(verifier.getModifiers()),
                "STOP-APM-CLASSE-NON-FINALE"
        );
    }
}
