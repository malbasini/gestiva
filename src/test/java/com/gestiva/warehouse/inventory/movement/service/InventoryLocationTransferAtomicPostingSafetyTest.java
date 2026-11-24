package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryLocationTransferRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryLocationTransferAllocationRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerConsumptionRepository;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryLocationTransferAtomicPostingSafetyTest {

    @Test
    void validRequestMustRemainBlocked() {

        WarehouseRepository warehouseRepository =
                mock(WarehouseRepository.class);

        InventoryLocationTransferRepository transferRepository =
                mock(InventoryLocationTransferRepository.class);

        InventoryLocationTransferAllocationRepository allocationRepository =
                mock(InventoryLocationTransferAllocationRepository.class);

        InventoryLayerRepository layerRepository =
                mock(InventoryLayerRepository.class);

        InventoryLayerConsumptionRepository consumptionRepository =
                mock(InventoryLayerConsumptionRepository.class);

        InventoryMovementService movementService =
                mock(InventoryMovementService.class);

        InventoryValuationService valuationService =
                mock(InventoryValuationService.class);

        var coordinator =
                new InventoryLocationTransferAtomicPostingCoordinator(
                        warehouseRepository,
                        transferRepository,
                        allocationRepository,
                        layerRepository,
                        consumptionRepository,
                        movementService,
                        valuationService
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> coordinator.post(
                        2L,
                        1L,
                        5L,
                        10L,
                        100L,
                        200L,
                        new BigDecimal("7.000"),
                        LocalDate.of(2026, 11, 24),
                        "Collaudo senza registrazione",
                        "AOX-TRANSFER-001"
                )
        );

        assertEquals(
                "Coordinatore atomico dei trasferimenti FIFO "
                + "non ancora abilitato.",
                exception.getMessage()
        );

        verifyNoInteractions(
                warehouseRepository,
                transferRepository,
                allocationRepository,
                layerRepository,
                consumptionRepository,
                movementService,
                valuationService
        );
    }

    @Test
    void transactionMustRequireRollbackForExceptions()
            throws Exception {

        var method =
                InventoryLocationTransferAtomicPostingCoordinator.class
                        .getDeclaredMethod(
                                "post",
                                Long.class,
                                Long.class,
                                Long.class,
                                Long.class,
                                Long.class,
                                Long.class,
                                BigDecimal.class,
                                LocalDate.class,
                                String.class,
                                String.class
                        );

        Transactional transaction =
                method.getAnnotation(Transactional.class);

        assertNotNull(
                transaction,
                "STOP-AOU-TRANSACTIONAL-MANCANTE"
        );

        assertEquals(
                Propagation.REQUIRED,
                transaction.propagation(),
                "STOP-AOU-PROPAGATION-NON-REQUIRED"
        );

        assertFalse(
                transaction.readOnly(),
                "STOP-AOU-TRANSAZIONE-READ-ONLY"
        );

        assertTrue(
                Arrays.asList(transaction.rollbackFor())
                        .contains(Exception.class),
                "STOP-AOU-ROLLBACK-FOR-NON-CONFORME"
        );
    }

    @Test
    void disabledCoordinatorMustNotHoldPersistenceDependencies() {

        var fields =
                InventoryLocationTransferAtomicPostingCoordinator.class
                        .getDeclaredFields();

        assertEquals(
                7,
                fields.length,
                "STOP-AOU-NUMERO-DIPENDENZE-NON-CONFORME"
        );

        for (var field : fields) {
            assertTrue(
                    Modifier.isPrivate(field.getModifiers()),
                    "STOP-AOU-CAMPO-NON-PRIVATO"
            );
            assertTrue(
                    Modifier.isFinal(field.getModifiers()),
                    "STOP-AOU-CAMPO-NON-FINAL"
            );
        }
    }
}
