package com.gestiva.warehouse.inventory.movement.service;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferPostingContractTest {

    @Test
    void postingServiceMustExposeTransactionalTransferContract()
            throws Exception {

        Class<?> postingClass;

        try {
            postingClass = Class.forName(
                    "com.gestiva.warehouse.inventory.movement.service."
                    + "InventoryLocationTransferPostingService"
            );
        } catch (ClassNotFoundException exception) {
            fail("STOP-ANK-POSTING-SERVICE-NON-IMPLEMENTATO");
            return;
        }

        Method postingMethod = postingClass.getMethod(
                "postTransfer",
                Long.class,       // tenantId
                Long.class,       // companyId
                Long.class,       // warehouseId
                Long.class,       // itemId
                Long.class,       // sourceLocationId
                Long.class,       // targetLocationId
                BigDecimal.class, // quantity
                LocalDate.class,  // movementDate
                String.class      // notes
        );

        assertEquals(
                Long.class,
                postingMethod.getReturnType(),
                "Il risultato deve identificare il trasferimento."
        );

        Transactional transaction =
                postingMethod.getAnnotation(Transactional.class);

        if (transaction == null) {
            transaction = postingClass.getAnnotation(
                    Transactional.class
            );
        }

        assertNotNull(
                transaction,
                "La registrazione deve dichiarare una transazione."
        );

        assertFalse(
                transaction.readOnly(),
                "La registrazione non può essere read-only."
        );
    }
}
