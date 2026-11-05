package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import com.gestiva.warehouse.location.entity.Warehouse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GoodsReceiptPartialCoordinatorTest {

    private GoodsReceiptPartialPersistenceGate gate;
    private GoodsReceiptPartialPlanningService planning;
    private WarehouseRepository warehouses;
    private GoodsReceiptRepository receipts;
    private DocumentSequenceService sequences;
    private GoodsReceiptPartialPersistenceWriter writer;

    private GoodsReceiptPartialCoordinator coordinator;

    private Map<Long, BigDecimal> requested() {
        return Map.of(101L, new BigDecimal("12.000"));
    }

    private GoodsReceipt header() {
        GoodsReceipt receipt = new GoodsReceipt();

        receipt.setTenantId(2L);
        receipt.setCompanyId(1L);
        receipt.setPurchaseOrderId(31L);
        receipt.setSupplierId(12L);
        receipt.setWarehouseId(7L);
        receipt.setReceiptDate(LocalDate.of(2026, 11, 4));

        return receipt;
    }

    @BeforeEach
    void setup() {
        gate = mock(GoodsReceiptPartialPersistenceGate.class);
        planning = mock(GoodsReceiptPartialPlanningService.class);
        warehouses = mock(WarehouseRepository.class);
        receipts = mock(GoodsReceiptRepository.class);
        sequences = mock(DocumentSequenceService.class);
        writer = mock(GoodsReceiptPartialPersistenceWriter.class);

        coordinator = new GoodsReceiptPartialCoordinator(
                gate, planning, warehouses,
                receipts, sequences, writer
        );
    }

    private GoodsReceiptPartialDocumentAssembler.DocumentPlan
    configureValidPlan(GoodsReceipt receipt) {

        var plan = mock(
                GoodsReceiptPartialDocumentAssembler.DocumentPlan.class
        );

        when(plan.header()).thenReturn(receipt);

        var input = mock(
                GoodsReceiptPartialDocumentAssembler.LineInput.class
        );

        when(plan.inputs()).thenReturn(List.of(input));

        when(planning.plan(same(receipt), eq(requested())))
                .thenReturn(plan);

        return plan;
    }

    private void configureActiveWarehouse() {
        Warehouse warehouse = mock(Warehouse.class);
        when(warehouse.isActive()).thenReturn(true);

        when(warehouses.findByTenantIdAndCompanyIdAndId(
                2L, 1L, 7L
        )).thenReturn(java.util.Optional.of(warehouse));
    }

    @Test
    void coordinatesPlanningNumberingAndWritingInOrder() {

        GoodsReceipt receipt = header();

        var plan = configureValidPlan(receipt);
        configureActiveWarehouse();

        when(sequences.nextNumber(
                2L, 1L, DocumentSequenceService.GOODS_RECEIPT
        )).thenReturn("GR-00004");

        when(writer.write(same(plan))).thenReturn(500L);

        Long result = coordinator.create(receipt, requested());

        assertEquals(500L, result);
        assertEquals("GR-00004", receipt.getReceiptNumber());

        var order = inOrder(
                gate, planning, warehouses,
                receipts, sequences, writer
        );

        order.verify(gate).requireEnabled();
        order.verify(planning).plan(same(receipt), eq(requested()));
        order.verify(warehouses)
                .findByTenantIdAndCompanyIdAndId(2L, 1L, 7L);
        order.verify(sequences).nextNumber(
                2L, 1L, DocumentSequenceService.GOODS_RECEIPT
        );
        order.verify(writer).write(same(plan));

        verifyNoMoreInteractions(
                gate, planning, warehouses,
                receipts, sequences, writer
        );

        System.out.println(
                "PARTIAL COORDINATOR: pianificazione e scrittura ordinate"
        );
    }

    @Test
    void gateRejectsBeforeAnyOtherOperation() {

        doThrow(new BusinessException(
                "Feature flag disabilitato."
        )).when(gate).requireEnabled();

        assertThrows(
                BusinessException.class,
                () -> coordinator.create(header(), requested())
        );

        verifyNoInteractions(
                planning, warehouses, receipts,
                sequences, writer
        );

        System.out.println(
                "PARTIAL COORDINATOR: gate precede ogni operazione"
        );
    }

    @Test
    void planningFailurePreventsWrites() {

        GoodsReceipt receipt = header();

        when(planning.plan(same(receipt), eq(requested())))
                .thenThrow(new BusinessException(
                        "Quantità richieste non valide."
                ));

        assertThrows(
                BusinessException.class,
                () -> coordinator.create(receipt, requested())
        );

        verifyNoInteractions(
                warehouses, receipts, sequences, writer
        );

        System.out.println(
                "PARTIAL COORDINATOR: piano non valido blocca scritture"
        );
    }

    @Test
    void missingWarehousePreventsNumberingAndWriting() {

        GoodsReceipt receipt = header();
        configureValidPlan(receipt);

        when(warehouses.findByTenantIdAndCompanyIdAndId(
                2L, 1L, 7L
        )).thenReturn(java.util.Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> coordinator.create(receipt, requested())
        );

        verifyNoInteractions(receipts, sequences, writer);

        System.out.println(
                "PARTIAL COORDINATOR: magazzino estraneo rifiutato"
        );
    }

    @Test
    void existingReceiptAllowsSecondReceiptWhenPlanIsValid() {

        GoodsReceipt receipt = header();
        var plan = configureValidPlan(receipt);
        configureActiveWarehouse();

        when(sequences.nextNumber(
                2L, 1L, DocumentSequenceService.GOODS_RECEIPT
        )).thenReturn("GR-00005");

        when(writer.write(same(plan))).thenReturn(501L);

        Long result = coordinator.create(receipt, requested());

        assertEquals(501L, result);
        assertEquals("GR-00005", receipt.getReceiptNumber());

        verify(planning).plan(same(receipt), eq(requested()));
        verify(writer).write(same(plan));
        verifyNoInteractions(receipts);

        System.out.println(
                "PARTIAL COORDINATOR: secondo GR consentito "
                + "con piano validato"
        );
    }

    @Test
    void sequenceFailurePreventsWriting() {

        GoodsReceipt receipt = header();
        configureValidPlan(receipt);
        configureActiveWarehouse();

        when(sequences.nextNumber(
                2L, 1L, DocumentSequenceService.GOODS_RECEIPT
        )).thenThrow(new BusinessException(
                "Numeratore non configurato."
        ));

        assertThrows(
                BusinessException.class,
                () -> coordinator.create(receipt, requested())
        );

        verifyNoInteractions(writer);

        System.out.println(
                "PARTIAL COORDINATOR: numeratore fallito blocca writer"
        );
    }

    @Test
    void declaresSingleReadCommittedTransaction() throws Exception {

        var method = GoodsReceiptPartialCoordinator.class
                .getDeclaredMethod(
                        "create", GoodsReceipt.class, Map.class
                );

        Transactional tx = method.getAnnotation(
                Transactional.class
        );

        assertNotNull(tx);
        assertEquals(Isolation.READ_COMMITTED, tx.isolation());
        assertEquals(Propagation.REQUIRED, tx.propagation());

        System.out.println(
                "PARTIAL COORDINATOR: transazione unica dichiarata"
        );
    }
}
