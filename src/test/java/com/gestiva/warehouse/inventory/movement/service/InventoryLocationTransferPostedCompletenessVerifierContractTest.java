package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransfer;
import com.gestiva.warehouse.inventory.movement.entity.InventoryMovement;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferPostedCompletenessVerifierContractTest {

    @Test
    void verifierMustExposePureCompletenessContract()
            throws Exception {

        final Class<?> verifier;

        try {
            verifier = Class.forName(
                    "com.gestiva.warehouse.inventory.movement.service."
                    + "InventoryLocationTransferPostedCompletenessVerifier"
            );
        } catch (ClassNotFoundException exception) {
            fail(
                    "STOP-APS-FIFO-COMPLETENESS-VERIFIER-NON-IMPLEMENTATO"
            );
            return;
        }

        var method = verifier.getDeclaredMethod(
                "verify",
                InventoryLocationTransfer.class,
                InventoryMovement.class,
                InventoryMovement.class,
                List.class,
                List.class,
                List.class
        );

        assertTrue(
                Modifier.isPublic(method.getModifiers()),
                "STOP-APS-METODO-NON-PUBBLICO"
        );

        assertTrue(
                Modifier.isStatic(method.getModifiers()),
                "STOP-APS-METODO-NON-STATICO"
        );

        assertEquals(
                void.class,
                method.getReturnType(),
                "STOP-APS-RETURN-TYPE-NON-CONFORME"
        );

        assertTrue(
                Modifier.isFinal(verifier.getModifiers()),
                "STOP-APS-CLASSE-NON-FINALE"
        );

        var parameterTypes = method.getGenericParameterTypes();

        String[] expectedElements = {
                "InventoryLocationTransferAllocation",
                "InventoryLayerConsumption",
                "InventoryLayer"
        };

        for (int i = 0; i < expectedElements.length; i++) {

            var listType = assertInstanceOf(
                    ParameterizedType.class,
                    parameterTypes[i + 3]
            );

            assertEquals(
                    List.class,
                    listType.getRawType()
            );

            assertEquals(
                    1,
                    listType.getActualTypeArguments().length
            );

            assertEquals(
                    expectedElements[i],
                    assertInstanceOf(
                            Class.class,
                            listType.getActualTypeArguments()[0]
                    ).getSimpleName()
            );
        }
    }
}
