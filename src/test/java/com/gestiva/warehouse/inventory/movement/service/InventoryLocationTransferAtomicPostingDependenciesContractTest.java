package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.warehouse.location.repository.WarehouseRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryLocationTransferRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryLocationTransferAllocationRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerConsumptionRepository;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class InventoryLocationTransferAtomicPostingDependenciesContractTest {

    @Test
    void coordinatorMustExposeExplicitDependencyConstructor() {

        Class<InventoryLocationTransferAtomicPostingCoordinator> type =
                InventoryLocationTransferAtomicPostingCoordinator.class;

        Constructor<InventoryLocationTransferAtomicPostingCoordinator> constructor;

        try {
            constructor = type.getDeclaredConstructor(
                    WarehouseRepository.class,
                    InventoryLocationTransferRepository.class,
                    InventoryLocationTransferAllocationRepository.class,
                    InventoryLayerRepository.class,
                    InventoryLayerConsumptionRepository.class,
                    InventoryMovementService.class,
                    InventoryValuationService.class
            );
        } catch (NoSuchMethodException exception) {
            fail(
                "STOP-AOW-COORDINATOR-DEPENDENCIES-NON-IMPLEMENTATE"
            );
            return;
        }

        assertTrue(
                Modifier.isPublic(constructor.getModifiers()),
                "STOP-AOW-COSTRUTTORE-NON-PUBBLICO"
        );

        assertEquals(
                7,
                constructor.getParameterCount(),
                "STOP-AOW-NUMERO-DIPENDENZE-NON-CONFORME"
        );
    }
}
