package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransfer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferIdempotencyVerifierContractTest {

    @Test
    void verifierMustExposePureSameRequestContract()
            throws Exception {

        Class<?> verifier;

        try {
            verifier = Class.forName(
                    "com.gestiva.warehouse.inventory.movement.service."
                    + "InventoryLocationTransferIdempotencyVerifier"
            );
        } catch (ClassNotFoundException exception) {
            fail(
                "STOP-API-IDEMPOTENCY-VERIFIER-NON-IMPLEMENTATO"
            );
            return;
        }

        var method = verifier.getDeclaredMethod(
                "verifySameRequest",
                InventoryLocationTransfer.class,
                Long.class,
                Long.class,
                Long.class,
                Long.class,
                Long.class,
                Long.class,
                BigDecimal.class,
                LocalDate.class
        );

        assertTrue(
                Modifier.isPublic(method.getModifiers()),
                "STOP-API-VERIFIER-NON-PUBBLICO"
        );

        assertTrue(
                Modifier.isStatic(method.getModifiers()),
                "STOP-API-VERIFIER-NON-STATICO"
        );

        assertEquals(
                void.class,
                method.getReturnType(),
                "STOP-API-RETURN-TYPE-NON-CONFORME"
        );
    }
}
