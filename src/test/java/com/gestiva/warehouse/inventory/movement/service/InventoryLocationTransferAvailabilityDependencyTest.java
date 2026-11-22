package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import com.gestiva.warehouse.inventory.valuation.service.InventoryAvailabilityService;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferAvailabilityDependencyTest {

    @Test
    void mustInjectInventoryAvailabilityService() {

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

                    return parameters.size() == 5
                            && parameters.contains(
                                    WarehouseRepository.class
                            )
                            && parameters.contains(
                                    TenantContext.class
                            )
                            && parameters.contains(
                                    CompanyContext.class
                            )
                            && parameters.contains(
                                    InventoryLocationValidationService.class
                            )
                            && parameters.contains(
                                    InventoryAvailabilityService.class
                            );
                });

        assertTrue(
                found,
                "Il servizio deve ricevere "
                + "InventoryAvailabilityService "
                + "insieme alle quattro dipendenze esistenti "
                + "tramite costruttore pubblico."
        );
    }
}
