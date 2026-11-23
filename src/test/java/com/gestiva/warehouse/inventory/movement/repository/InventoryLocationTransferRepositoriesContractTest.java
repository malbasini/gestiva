package com.gestiva.warehouse.inventory.movement.repository;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferRepositoriesContractTest {

    private static final String ENTITY_PACKAGE =
            "com.gestiva.warehouse.inventory.movement.entity.";

    private static final String REPOSITORY_PACKAGE =
            "com.gestiva.warehouse.inventory.movement.repository.";

    @Test
    void repositoriesMustSupportTenantCompanyScopedLookups()
            throws Exception {

        Class<?> transferEntity = Class.forName(
                ENTITY_PACKAGE + "InventoryLocationTransfer"
        );

        Class<?> allocationEntity = Class.forName(
                ENTITY_PACKAGE + "InventoryLocationTransferAllocation"
        );

        Class<?> transferRepository = requireRepository(
                "InventoryLocationTransferRepository"
        );

        Class<?> allocationRepository = requireRepository(
                "InventoryLocationTransferAllocationRepository"
        );

        verifyJpaRepository(
                transferRepository,
                transferEntity
        );

        verifyJpaRepository(
                allocationRepository,
                allocationEntity
        );

        Method byOperationKey = transferRepository.getMethod(
                "findByTenantIdAndCompanyIdAndOperationKey",
                Long.class,
                Long.class,
                String.class
        );

        assertEquals(
                Optional.class,
                byOperationKey.getReturnType()
        );

        Method byId = transferRepository.getMethod(
                "findByTenantIdAndCompanyIdAndId",
                Long.class,
                Long.class,
                Long.class
        );

        assertEquals(
                Optional.class,
                byId.getReturnType()
        );

        Method byTransfer = allocationRepository.getMethod(
                "findByTenantIdAndCompanyIdAndTransferIdOrderByIdAsc",
                Long.class,
                Long.class,
                Long.class
        );

        assertEquals(
                List.class,
                byTransfer.getReturnType()
        );
    }

    private static Class<?> requireRepository(String name)
            throws Exception {
        try {
            return Class.forName(REPOSITORY_PACKAGE + name);
        } catch (ClassNotFoundException exception) {
            fail("STOP-AOA-TRANSFER-REPOSITORIES-NON-IMPLEMENTATI: "
                    + name);
            throw exception;
        }
    }

    private static void verifyJpaRepository(
            Class<?> repository,
            Class<?> entity
    ) {
        assertTrue(
                repository.isInterface(),
                "Il repository deve essere un'interfaccia."
        );

        boolean correctJpaType = false;

        for (Type parent : repository.getGenericInterfaces()) {
            if (!(parent instanceof ParameterizedType type)) {
                continue;
            }

            if (type.getRawType() != JpaRepository.class) {
                continue;
            }

            Type[] arguments = type.getActualTypeArguments();

            if (arguments.length == 2
                    && arguments[0] == entity
                    && arguments[1] == Long.class) {
                correctJpaType = true;
            }
        }

        assertTrue(
                correctJpaType,
                "Repository JPA con entità o ID errato: "
                        + repository.getSimpleName()
        );
    }
}
