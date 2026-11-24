package com.gestiva.warehouse.inventory.movement.service;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferAtomicPostingCoordinatorContractTest {

    @Test
    void coordinatorMustExposeTransactionalPostingContract()
            throws Exception {

        Class<?> coordinator;

        try {
            coordinator = Class.forName(
                    "com.gestiva.warehouse.inventory.movement.service."
                    + "InventoryLocationTransferAtomicPostingCoordinator"
            );
        } catch (ClassNotFoundException exception) {
            fail(
                "STOP-AOS-ATOMIC-COORDINATOR-NON-IMPLEMENTATO"
            );
            return;
        }

        assertTrue(
                coordinator.isAnnotationPresent(Service.class),
                "STOP-AOS-SERVICE-ANNOTATION-MANCANTE"
        );

        Method posting = coordinator.getDeclaredMethod(
                "post",
                Long.class,       // tenantId
                Long.class,       // companyId
                Long.class,       // warehouseId
                Long.class,       // itemId
                Long.class,       // sourceLocationId
                Long.class,       // targetLocationId
                BigDecimal.class, // quantity
                LocalDate.class,  // movementDate
                String.class,     // notes
                String.class      // operationKey
        );

        assertTrue(
                Modifier.isPublic(posting.getModifiers()),
                "STOP-AOS-POSTING-NON-PUBBLICO"
        );

        assertEquals(
                Long.class,
                posting.getReturnType(),
                "STOP-AOS-RETURN-TYPE-NON-CONFORME"
        );

        Transactional transaction =
                posting.getAnnotation(Transactional.class);

        if (transaction == null) {
            transaction = coordinator.getAnnotation(
                    Transactional.class
            );
        }

        assertNotNull(
                transaction,
                "STOP-AOS-TRANSACTIONAL-MANCANTE"
        );

        assertNotEquals(
                Propagation.NOT_SUPPORTED,
                transaction.propagation(),
                "STOP-AOS-TRANSAZIONE-DISABILITATA"
        );

        assertNotEquals(
                Propagation.NEVER,
                transaction.propagation(),
                "STOP-AOS-TRANSAZIONE-VIETATA"
        );

        assertNotEquals(
                Propagation.SUPPORTS,
                transaction.propagation(),
                "STOP-AOS-TRANSAZIONE-NON-GARANTITA"
        );

        assertFalse(
                transaction.readOnly(),
                "STOP-AOS-TRANSAZIONE-READ-ONLY"
        );
    }
}
