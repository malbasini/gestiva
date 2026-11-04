package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoodsReceiptPartialPlanningServiceTest {

    private GoodsReceiptPartialSnapshotService snapshots;
    private GoodsReceiptIsolationGuard guard;
    private GoodsReceiptPartialPlanningService service;

    private static BigDecimal bd(String text) {
        return new BigDecimal(text);
    }

    @BeforeEach
    void setup() {
        snapshots = mock(GoodsReceiptPartialSnapshotService.class);
        guard = mock(GoodsReceiptIsolationGuard.class);
        service = new GoodsReceiptPartialPlanningService(
                snapshots, guard
        );
    }

    private GoodsReceipt header() {
        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setTenantId(2L);
        receipt.setCompanyId(1L);
        receipt.setPurchaseOrderId(31L);
        receipt.setSupplierId(12L);
        receipt.setWarehouseId(7L);
        receipt.setReceiptNumber("GR-TEST");
        receipt.setReceiptDate(LocalDate.of(2026, 11, 4));
        return receipt;
    }

    private GoodsReceiptPartialSnapshotService.Snapshot snapshot() {
        PurchaseOrder po = mock(PurchaseOrder.class);
        when(po.getId()).thenReturn(31L);
        when(po.getTenantId()).thenReturn(2L);
        when(po.getCompanyId()).thenReturn(1L);
        when(po.getSupplierId()).thenReturn(12L);
        when(po.getStatus()).thenReturn("CONFIRMED");

        PurchaseOrderLine line = mock(PurchaseOrderLine.class);
        when(line.getId()).thenReturn(101L);
        when(line.getTenantId()).thenReturn(2L);
        when(line.getPurchaseOrderId()).thenReturn(31L);
        when(line.getUomId()).thenReturn(5L);
        when(line.getQuantity()).thenReturn(bd("2.000"));
        when(line.getConversionFactor())
                .thenReturn(bd("10.000000"));
        when(line.getLineSubtotal()).thenReturn(bd("144.00"));
        when(line.getItemId()).thenReturn(50L);
        when(line.getDescription()).thenReturn("Articolo test");

        return new GoodsReceiptPartialSnapshotService.Snapshot(
                2L, 1L, po, List.of(line),
                Map.of(101L, bd("8.000")),
                Map.of(101L, bd("12.000"))
        );
    }

    @Test
    void buildsPlanUsingSameSnapshot() {
        var preparedSnapshot = snapshot();

        when(snapshots.prepare(
                2L, 31L, Map.of(101L, bd("12.000"))
        )).thenReturn(preparedSnapshot);

        var plan = service.plan(
                header(),
                Map.of(101L, bd("12.000"))
        );

        assertEquals(1, plan.inputs().size());

        var lines = plan.buildLines(500L);
        assertEquals(1, lines.size());
        assertEquals(500L, lines.get(0).getGoodsReceiptId());
        assertEquals(
                0,
                bd("86.4000").compareTo(
                        lines.get(0).getTotalCost()
                )
        );

        var order = inOrder(guard, snapshots);
        order.verify(guard).requireReadCommitted();
        order.verify(snapshots).prepare(
                2L, 31L, Map.of(101L, bd("12.000"))
        );

        verifyNoMoreInteractions(snapshots);

        System.out.println(
                "PARTIAL PLANNING: snapshot unico e piano FIFO coerente"
        );
    }

    @Test
    void rejectsWrongCompany() {
        var preparedSnapshot = snapshot();
        when(snapshots.prepare(anyLong(), anyLong(), anyMap()))
                .thenReturn(preparedSnapshot);

        var wrong = header();
        wrong.setCompanyId(9L);

        assertThrows(
                BusinessException.class,
                () -> service.plan(
                        wrong, Map.of(101L, bd("12.000"))
                )
        );

        System.out.println(
                "PARTIAL PLANNING: company incoerente rifiutata"
        );
    }

    @Test
    void rejectsWrongSupplier() {
        var preparedSnapshot = snapshot();
        when(snapshots.prepare(anyLong(), anyLong(), anyMap()))
                .thenReturn(preparedSnapshot);

        var wrong = header();
        wrong.setSupplierId(99L);

        assertThrows(
                BusinessException.class,
                () -> service.plan(
                        wrong, Map.of(101L, bd("12.000"))
                )
        );

        System.out.println(
                "PARTIAL PLANNING: fornitore incoerente rifiutato"
        );
    }

    @Test
    void rejectsInvalidHeaderBeforeSnapshot() {
        var invalid = header();
        invalid.setWarehouseId(null);

        assertThrows(
                BusinessException.class,
                () -> service.plan(
                        invalid, Map.of(101L, bd("12.000"))
                )
        );

        verifyNoInteractions(guard, snapshots);

        System.out.println(
                "PARTIAL PLANNING: testata incompleta rifiutata"
        );
    }

    @Test
    void rejectsIsolationBeforeSnapshot() {
        doThrow(new BusinessException(
                "READ COMMITTED obbligatorio."
        )).when(guard).requireReadCommitted();

        assertThrows(
                BusinessException.class,
                () -> service.plan(
                        header(), Map.of(101L, bd("12.000"))
                )
        );

        verifyNoInteractions(snapshots);

        System.out.println(
                "PARTIAL PLANNING: isolamento errato rifiutato"
        );
    }

    @Test
    void declaresReadCommittedTransaction() throws Exception {
        var method = GoodsReceiptPartialPlanningService.class
                .getMethod(
                        "plan",
                        GoodsReceipt.class,
                        Map.class
                );

        var tx = method.getAnnotation(Transactional.class);

        assertNotNull(tx);
        assertEquals(Isolation.READ_COMMITTED, tx.isolation());

        System.out.println(
                "PARTIAL PLANNING: transazione READ COMMITTED dichiarata"
        );
    }
}
