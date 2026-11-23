package com.gestiva.warehouse.inventory.movement.service;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferPostingIdempotencyContractTest {

    @Test
    void postingMustAcceptExplicitOperationKey() {

        Class<InventoryLocationTransferPostingService> type =
                InventoryLocationTransferPostingService.class;

        // Il coordinatore deve mantenere il confine
        // transazionale per movimenti e layer FIFO.
        assertNotNull(
                type.getAnnotation(Transactional.class),
                "STOP-AOD-TRANSACTIONAL-MANCANTE"
        );

        Method posting;

        try {
            posting = type.getDeclaredMethod(
                    "postTransfer",
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
        } catch (NoSuchMethodException exception) {
            fail(
                "STOP-AOD-OPERATION-KEY-NON-IMPLEMENTATA"
            );
            return;
        }

        assertEquals(
                Long.class,
                posting.getReturnType(),
                "Il posting deve restituire l'ID del trasferimento."
        );

        // Preserviamo la vecchia firma per compatibilità.
        assertDoesNotThrow(
                () -> type.getDeclaredMethod(
                        "postTransfer",
                        Long.class,
                        Long.class,
                        Long.class,
                        Long.class,
                        Long.class,
                        Long.class,
                        BigDecimal.class,
                        LocalDate.class,
                        String.class
                )
        );
    }
}
