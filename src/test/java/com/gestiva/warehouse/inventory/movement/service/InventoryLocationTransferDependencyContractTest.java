package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.security.company.context.CompanyContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferDependencyContractTest {

    @Test
    void mustProvideConstructorWithSecurityAndLocationValidation() {

        boolean found = Arrays.stream(
                InventoryLocationTransferService.class
                        .getDeclaredConstructors()
        ).anyMatch(constructor -> {

            Class<?>[] parameters = constructor.getParameterTypes();

            return Modifier.isPublic(constructor.getModifiers())
                    && Arrays.asList(parameters).contains(
                            CompanyContext.class
                    )
                    && Arrays.asList(parameters).contains(
                            InventoryLocationValidationService.class
                    );
        });

        assertTrue(
                found,
                "Il servizio deve ricevere CompanyContext e "
                + "InventoryLocationValidationService tramite costruttore."
        );
    }

    @Test
    void mustNotExposePublicNoArgumentConstructor() {

        boolean found = Arrays.stream(
                InventoryLocationTransferService.class
                        .getConstructors()
        ).map(Constructor::getParameterCount)
         .anyMatch(count -> count == 0);

        assertFalse(
                found,
                "Il servizio non deve esporre un costruttore "
                + "pubblico privo delle dipendenze di sicurezza."
        );
    }
}
