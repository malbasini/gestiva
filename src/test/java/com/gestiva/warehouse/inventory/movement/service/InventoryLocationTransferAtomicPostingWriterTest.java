package com.gestiva.warehouse.inventory.movement.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransfer;
import com.gestiva.warehouse.inventory.movement.entity.InventoryLocationTransferAllocation;
import com.gestiva.warehouse.inventory.movement.entity.InventoryMovement;
import com.gestiva.warehouse.inventory.movement.repository.InventoryLocationTransferRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryLocationTransferAllocationRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayerConsumption;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerConsumptionRepository;

import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InventoryLocationTransferAtomicPostingWriterTest {

    private static final LocalDate DATE =
            LocalDate.of(2026, 11, 24);

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private static final class Fixture {

        final WarehouseRepository warehouses =
                mock(WarehouseRepository.class);

        final InventoryLocationTransferRepository transfers =
                mock(InventoryLocationTransferRepository.class);

        final InventoryLocationTransferAllocationRepository allocations =
                mock(InventoryLocationTransferAllocationRepository.class);

        final InventoryLayerRepository layers =
                mock(InventoryLayerRepository.class);

        final InventoryLayerConsumptionRepository consumptions =
                mock(InventoryLayerConsumptionRepository.class);

        final InventoryMovementRepository movements =
                mock(InventoryMovementRepository.class);

        final InventoryMovementService movementService =
                mock(InventoryMovementService.class);

        final InventoryLocationTransferAtomicPostingWriter writer =
                new InventoryLocationTransferAtomicPostingWriter(
                        warehouses,
                        transfers,
                        allocations,
                        layers,
                        consumptions,
                        movements,
                        movementService
                );

        final InventoryLayer source = spy(new InventoryLayer());
        final InventoryMovement out = spy(new InventoryMovement());
        final InventoryMovement in = spy(new InventoryMovement());

        void warehouseExists() throws Exception {
            var method = WarehouseRepository.class.getMethod(
                    "findByTenantAndCompanyAndIdForUpdate",
                    Long.class, Long.class, Long.class
            );

            var generic = (ParameterizedType)
                    method.getGenericReturnType();

            var entityType = (Class<?>)
                    generic.getActualTypeArguments()[0];

            Object warehouse = mock(entityType);

            doReturn(Optional.of(warehouse))
                    .when(warehouses)
                    .findByTenantAndCompanyAndIdForUpdate(
                            2L, 1L, 5L
                    );
        }

        void prepareSource(BigDecimal available) {
            doReturn(800L).when(source).getId();

            source.setTenantId(2L);
            source.setCompanyId(1L);
            source.setWarehouseId(5L);
            source.setLocationId(100L);
            source.setItemId(10L);
            source.setLayerDate(DATE.minusDays(20));
            source.setRemainingQty(available);
            source.setOriginalQty(available);
            source.setUnitCost(bd("2.5000"));
            source.setClosed(false);

            when(layers.findTransferSourceLayersForUpdate(
                    2L, 1L, 5L, 10L, 100L
            )).thenReturn(List.of(source));
        }

        void completePosting() throws Exception {
            warehouseExists();
            prepareSource(bd("9.000"));

            when(transfers
                    .findByTenantIdAndCompanyIdAndOperationKey(
                            2L, 1L, "APY-KEY-001"
                    )).thenReturn(Optional.empty());

            when(transfers.saveAndFlush(
                    any(InventoryLocationTransfer.class)
            )).thenAnswer(inv -> {
                InventoryLocationTransfer entity = inv.getArgument(0);

                // Il trasferimento viene salvato due volte:
                // prima PENDING, poi POSTED.
                // Non creare uno spy sopra uno spy esistente.
                if (mockingDetails(entity).isSpy()) {
                    return entity;
                }

                var saved = spy(entity);
                doReturn(700L).when(saved).getId();
                return saved;
            });

            when(movementService.registerMovement(
                    any(), any(), any(), any(), any(),
                    any(), any(), any(), any(),
                    nullable(BigDecimal.class),
                    any(), any(), nullable(String.class)
            )).thenReturn(701L, 702L);

            movement(out, 701L, 100L, "OUT");
            movement(in, 702L, 200L, "IN");

            when(movements.findByTenantIdAndCompanyIdAndId(
                    2L, 1L, 701L
            )).thenReturn(Optional.of(out));

            when(movements.findByTenantIdAndCompanyIdAndId(
                    2L, 1L, 702L
            )).thenReturn(Optional.of(in));

            when(consumptions.save(
                    any(InventoryLayerConsumption.class)
            )).thenAnswer(inv -> inv.getArgument(0));

            when(layers.saveAndFlush(
                    any(InventoryLayer.class)
            )).thenAnswer(inv -> {
                var target = spy(
                        (InventoryLayer) inv.getArgument(0)
                );
                doReturn(900L).when(target).getId();
                return target;
            });

            when(allocations.save(
                    any(InventoryLocationTransferAllocation.class)
            )).thenAnswer(inv -> inv.getArgument(0));
        }

        private void movement(
                InventoryMovement m,
                Long id,
                Long location,
                String type) {

            doReturn(id).when(m).getId();

            m.setTenantId(2L);
            m.setCompanyId(1L);
            m.setWarehouseId(5L);
            m.setLocationId(location);
            m.setItemId(10L);
            m.setMovementDate(DATE);
            m.setMovementType(type);
            m.setCausalCode("LOCATION_TRANSFER_" + type);
            m.setQuantity(bd("7.000"));
            m.setReferenceType("LOCATION_TRANSFER");
            m.setReferenceId(700L);
            m.setReversed(false);
            m.setReversalOfMovementId(null);
        }

        Long write() {
            return writer.write(
                    2L, 1L, 5L, 10L,
                    100L, 200L,
                    bd("7.000"),
                    DATE, "Collaudo",
                    "APY-KEY-001"
            );
        }
    }

    @Test
    void validPostingCreatesMatchingFifoEvidence() throws Exception {
        var f = new Fixture();
        f.completePosting();

        assertEquals(700L, f.write());
        assertEquals(0, bd("2.000")
                .compareTo(f.source.getRemainingQty()));
        assertEquals(0, bd("17.5000")
                .compareTo(f.out.getTotalCost()));
        assertEquals(0, bd("17.5000")
                .compareTo(f.in.getTotalCost()));

        verify(f.consumptions).save(
                any(InventoryLayerConsumption.class)
        );
        verify(f.allocations).save(
                any(InventoryLocationTransferAllocation.class)
        );
        verify(f.layers).saveAndFlush(
                any(InventoryLayer.class)
        );
    }

    @Test
    void invalidRequestStopsBeforeDatabaseAccess() {
        var f = new Fixture();

        assertThrows(
                BusinessException.class,
                () -> f.writer.write(
                        2L, 1L, 5L, 10L,
                        100L, 100L,
                        bd("7.000"),
                        DATE, "Collaudo",
                        "APY-KEY-001"
                )
        );

        verifyNoInteractions(
                f.warehouses,
                f.transfers,
                f.layers,
                f.movements
        );
    }

    @Test
    void missingWarehousePreventsAllWrites() {
        var f = new Fixture();

        assertThrows(BusinessException.class, f::write);

        verifyNoInteractions(
                f.allocations,
                f.consumptions,
                f.movementService,
                f.movements
        );
    }

    @Test
    void duplicateOperationKeyStopsBeforeFifoReads()
            throws Exception {

        var f = new Fixture();
        f.warehouseExists();

        when(f.transfers
                .findByTenantIdAndCompanyIdAndOperationKey(
                        2L, 1L, "APY-KEY-001"
                )).thenReturn(
                        Optional.of(
                                mock(InventoryLocationTransfer.class)
                        )
                );

        assertThrows(BusinessException.class, f::write);

        verifyNoInteractions(
                f.layers,
                f.allocations,
                f.consumptions,
                f.movementService,
                f.movements
        );
    }

    @Test
    void insufficientFifoStopsBeforeAnyPersistence()
            throws Exception {

        var f = new Fixture();
        f.warehouseExists();
        f.prepareSource(bd("3.000"));

        assertThrows(BusinessException.class, f::write);

        verify(f.transfers, never()).saveAndFlush(any());
        verifyNoInteractions(
                f.allocations,
                f.consumptions,
                f.movementService,
                f.movements
        );
    }

    @Test
    void identicalOutAndInIdsAreRejectedBeforeFifoWrites()
            throws Exception {

        var f = new Fixture();
        f.completePosting();

        when(f.movementService.registerMovement(
                any(), any(), any(), any(), any(),
                any(), any(), any(), any(),
                nullable(BigDecimal.class),
                any(), any(), nullable(String.class)
        )).thenReturn(701L, 701L);

        assertThrows(BusinessException.class, f::write);

        verifyNoInteractions(f.consumptions, f.allocations);
        verify(f.layers, never()).saveAndFlush(any());
    }

    @Test
    void missingOutMovementPreventsFifoWrites()
            throws Exception {

        var f = new Fixture();
        f.completePosting();

        when(f.movements.findByTenantIdAndCompanyIdAndId(
                2L, 1L, 701L
        )).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, f::write);

        verifyNoInteractions(f.consumptions, f.allocations);
        verify(f.layers, never()).saveAndFlush(any());
    }

    @Test
    void consumptionWriteFailurePropagates()
            throws Exception {

        var f = new Fixture();
        f.completePosting();

        when(f.consumptions.save(
                any(InventoryLayerConsumption.class)
        )).thenThrow(new IllegalStateException(
                "Errore simulato sul consumo FIFO."
        ));

        assertThrows(IllegalStateException.class, f::write);

        verify(f.consumptions).save(
                any(InventoryLayerConsumption.class)
        );

        verifyNoInteractions(f.allocations);
        verify(f.layers, never()).saveAndFlush(any());
    }

    @Test
    void destinationLayerWriteFailurePropagates()
            throws Exception {

        var f = new Fixture();
        f.completePosting();

        when(f.layers.saveAndFlush(
                any(InventoryLayer.class)
        )).thenThrow(new IllegalStateException(
                "Errore simulato sul layer destinazione."
        ));

        assertThrows(IllegalStateException.class, f::write);

        verify(f.consumptions).save(
                any(InventoryLayerConsumption.class)
        );

        verify(f.layers).saveAndFlush(
                any(InventoryLayer.class)
        );

        verifyNoInteractions(f.allocations);
    }

    @Test
    void allocationWriteFailurePreventsFinalTransferSave()
            throws Exception {

        var f = new Fixture();
        f.completePosting();

        when(f.allocations.save(
                any(InventoryLocationTransferAllocation.class)
        )).thenThrow(new IllegalStateException(
                "Errore simulato sull'allocazione."
        ));

        assertThrows(IllegalStateException.class, f::write);

        verify(f.allocations).save(
                any(InventoryLocationTransferAllocation.class)
        );

        // Il salvataggio iniziale PENDING avviene.
        // Il salvataggio finale POSTED non deve avvenire.
        verify(f.transfers, times(1)).saveAndFlush(
                any(InventoryLocationTransfer.class)
        );
    }

}
