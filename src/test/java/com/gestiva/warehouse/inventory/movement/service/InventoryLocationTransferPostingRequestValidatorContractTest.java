package com.gestiva.warehouse.inventory.movement.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferPostingRequestValidatorContractTest {

    @Test
    void validatorMustExposePureRequestValidationContract()
            throws Exception {

        Class<?> validator;

        try {
            validator = Class.forName(
                    "com.gestiva.warehouse.inventory.movement.service."
                    + "InventoryLocationTransferPostingRequestValidator"
            );
        } catch (ClassNotFoundException exception) {
            fail(
                    "STOP-AOY-POSTING-REQUEST-VALIDATOR-NON-IMPLEMENTATO"
            );
            return;
        }

        Method validate = validator.getDeclaredMethod(
                "validate",
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
                Modifier.isPublic(validate.getModifiers()),
                "STOP-AOY-VALIDATE-NON-PUBBLICO"
        );

        assertTrue(
                Modifier.isStatic(validate.getModifiers()),
                "STOP-AOY-VALIDATE-NON-STATICO"
        );

        assertEquals(
                void.class,
                validate.getReturnType(),
                "STOP-AOY-RETURN-TYPE-NON-CONFORME"
        );
    }
}
