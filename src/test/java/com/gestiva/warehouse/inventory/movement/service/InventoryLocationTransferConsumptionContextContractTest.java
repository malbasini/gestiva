package com.gestiva.warehouse.inventory.movement.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferConsumptionContextContractTest {

    @Test
    void verifierMustRequireFullConsumptionContext() {

        Class<InventoryLocationTransferConsumptionVerifier> type =
                InventoryLocationTransferConsumptionVerifier.class;

        Method method;

        try {
            method = type.getDeclaredMethod(
                    "verify",
                    InventoryLocationTransferPostingPlan.class,
                    List.class,
                    Long.class, // tenantId
                    Long.class, // companyId
                    Long.class, // warehouseId
                    Long.class  // outMovementId
            );
        } catch (NoSuchMethodException exception) {
            fail(
                "STOP-AOO-CONSUMPTION-CONTEXT-NON-IMPLEMENTATO"
            );
            return;
        }

        assertTrue(
                Modifier.isStatic(method.getModifiers()),
                "STOP-AOO-VERIFIER-NON-STATICO"
        );

        assertEquals(
                void.class,
                method.getReturnType(),
                "STOP-AOO-RETURN-TYPE-NON-CONFORME"
        );

        // La precedente API economica rimane disponibile.
        assertDoesNotThrow(
                () -> type.getDeclaredMethod(
                        "verify",
                        InventoryLocationTransferPostingPlan.class,
                        List.class
                )
        );
    }
}
