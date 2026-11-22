package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferTenantDependencyTest {

    @Test
    void mustInjectTenantCompanyAndLocationContexts() {

        List<Constructor<?>> constructors = Arrays.asList(
                InventoryLocationTransferService.class
                        .getDeclaredConstructors()
        );

        boolean found = constructors.stream()
                .anyMatch(constructor -> {

                    if (!Modifier.isPublic(
                            constructor.getModifiers())) {
                        return false;
                    }

                    List<Class<?>> parameters = Arrays.asList(
                            constructor.getParameterTypes()
                    );

                    return parameters.size() >= 3
                            && parameters.contains(TenantContext.class)
                            && parameters.contains(CompanyContext.class)
                            && parameters.contains(
                                    InventoryLocationValidationService.class
                            );
                });

        assertTrue(
                found,
                "Il servizio deve ricevere TenantContext, "
                + "CompanyContext e "
                + "InventoryLocationValidationService "
                + "tramite costruttore pubblico."
        );
    }
}
