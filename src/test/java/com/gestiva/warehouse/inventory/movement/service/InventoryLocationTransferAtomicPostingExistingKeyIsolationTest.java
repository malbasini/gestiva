package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransfer;
import com.gestiva.warehouse.inventory.movement.repository.InventoryLocationTransferRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryLocationTransferAllocationRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerConsumptionRepository;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryLocationTransferAtomicPostingExistingKeyIsolationTest {

    private static final class Fixture {

        final WarehouseRepository warehouse =
                mock(WarehouseRepository.class);

        final InventoryLocationTransferRepository transfers =
                mock(InventoryLocationTransferRepository.class);

        final InventoryLocationTransferAllocationRepository allocations =
                mock(InventoryLocationTransferAllocationRepository.class);

        final InventoryLayerRepository layers =
                mock(InventoryLayerRepository.class);

        final InventoryLayerConsumptionRepository consumptions =
                mock(InventoryLayerConsumptionRepository.class);

        final InventoryMovementService movements =
                mock(InventoryMovementService.class);

        final InventoryValuationService valuation =
                mock(InventoryValuationService.class);

        final InventoryLocationTransferAtomicPostingCoordinator coordinator =
                new InventoryLocationTransferAtomicPostingCoordinator(
                        warehouse,
                        transfers,
                        allocations,
                        layers,
                        consumptions,
                        movements,
                        valuation
                );

        void provideWarehouse(Long tenantId, Long companyId)
                throws Exception {

            var method = WarehouseRepository.class.getMethod(
                    "findByTenantAndCompanyAndIdForUpdate",
                    Long.class,
                    Long.class,
                    Long.class
            );

            var type = assertInstanceOf(
                    ParameterizedType.class,
                    method.getGenericReturnType()
            );

            Class<?> entityType = assertInstanceOf(
                    Class.class,
                    type.getActualTypeArguments()[0]
            );

            Object existingWarehouse = mock(entityType);

            doReturn(Optional.of(existingWarehouse))
                    .when(warehouse)
                    .findByTenantAndCompanyAndIdForUpdate(
                            tenantId, companyId, 5L
                    );
        }

        void post(
                Long tenantId,
                Long companyId,
                String operationKey,
                BigDecimal quantity) {

            coordinator.post(
                    tenantId,
                    companyId,
                    5L,
                    10L,
                    100L,
                    200L,
                    quantity,
                    LocalDate.of(2026, 11, 24),
                    "Collaudo APH",
                    operationKey
            );
        }

        void verifyOrderAndScope(
                Long tenantId,
                Long companyId,
                String operationKey) {

            InOrder sequence = inOrder(warehouse, transfers);

            sequence.verify(warehouse)
                    .findByTenantAndCompanyAndIdForUpdate(
                            tenantId, companyId, 5L
                    );

            sequence.verify(transfers)
                    .findByTenantIdAndCompanyIdAndOperationKey(
                            tenantId, companyId, operationKey
                    );

            sequence.verifyNoMoreInteractions();

            verifyNoInteractions(
                    allocations,
                    layers,
                    consumptions,
                    movements,
                    valuation
            );
        }
    }

    @Test
    void existingKeyMustBeRejectedWithoutPosting()
            throws Exception {

        var fixture = new Fixture();
        fixture.provideWarehouse(2L, 1L);

        when(fixture.transfers
                .findByTenantIdAndCompanyIdAndOperationKey(
                        2L, 1L, "APH-EXISTING-001"
                ))
                .thenReturn(Optional.of(
                        new InventoryLocationTransfer()
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.post(
                        2L,
                        1L,
                        "APH-EXISTING-001",
                        new BigDecimal("7.000")
                )
        );

        assertEquals(
                "La chiave di idempotenza è già associata "
                        + "a un trasferimento con parametri differenti.",
                exception.getMessage()
        );

        fixture.verifyOrderAndScope(
                2L, 1L, "APH-EXISTING-001"
        );
    }

    @Test
    void lookupMustUseTheRequestedTenantAndCompany()
            throws Exception {

        var fixture = new Fixture();
        fixture.provideWarehouse(3L, 4L);

        when(fixture.transfers
                .findByTenantIdAndCompanyIdAndOperationKey(
                        3L, 4L, "APH-SCOPE-001"
                ))
                .thenReturn(Optional.of(
                        new InventoryLocationTransfer()
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.post(
                        3L,
                        4L,
                        "APH-SCOPE-001",
                        new BigDecimal("7.000")
                )
        );

        assertEquals(
                "La chiave di idempotenza è già associata "
                        + "a un trasferimento con parametri differenti.",
                exception.getMessage()
        );

        fixture.verifyOrderAndScope(
                3L, 4L, "APH-SCOPE-001"
        );
    }

    @Test
    void missingKeyMustStillLeavePostingDisabled()
            throws Exception {

        var fixture = new Fixture();
        fixture.provideWarehouse(2L, 1L);

        when(fixture.transfers
                .findByTenantIdAndCompanyIdAndOperationKey(
                        2L, 1L, "APH-NEW-001"
                ))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.post(
                        2L,
                        1L,
                        "APH-NEW-001",
                        new BigDecimal("7.000")
                )
        );

        assertEquals(
                "Coordinatore atomico dei trasferimenti FIFO "
                        + "non ancora abilitato.",
                exception.getMessage()
        );

        fixture.verifyOrderAndScope(
                2L, 1L, "APH-NEW-001"
        );
    }

    @Test
    void invalidRequestMustNeverReachRepositories() {

        var fixture = new Fixture();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.post(
                        2L,
                        1L,
                        "APH-INVALID-001",
                        new BigDecimal("0.000")
                )
        );

        assertEquals(
                "La quantità da trasferire deve essere positiva.",
                exception.getMessage()
        );

        verifyNoInteractions(
                fixture.warehouse,
                fixture.transfers,
                fixture.allocations,
                fixture.layers,
                fixture.consumptions,
                fixture.movements,
                fixture.valuation
        );
    }
}
