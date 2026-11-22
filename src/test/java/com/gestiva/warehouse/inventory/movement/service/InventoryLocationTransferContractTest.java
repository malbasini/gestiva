package com.gestiva.warehouse.inventory.movement.service;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferContractTest {

    @Test
    void transferServiceMustExposeTransactionalContract()
            throws Exception {

        Class<?> service = Class.forName(
                "com.gestiva.warehouse.inventory.movement.service."
                + "InventoryLocationTransferService"
        );

        Method transfer = service.getMethod(
                "transferBetweenLocations",
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
                transfer.getReturnType(),
                "Il trasferimento deve restituire il suo ID"
        );

        assertTrue(
                service.isAnnotationPresent(Transactional.class)
                || transfer.isAnnotationPresent(Transactional.class),
                "Il trasferimento deve essere transazionale"
        );
    }
}
