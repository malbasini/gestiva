package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoodsReceiptPartialDocumentAssemblerTest {

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private PurchaseOrder order() {
        PurchaseOrder po = mock(PurchaseOrder.class);

        when(po.getId()).thenReturn(31L);
        when(po.getTenantId()).thenReturn(2L);
        when(po.getCompanyId()).thenReturn(1L);
        when(po.getSupplierId()).thenReturn(12L);
        when(po.getOrderNumber()).thenReturn("PO-00031");
        when(po.getStatus()).thenReturn("CONFIRMED");

        return po;
    }

    private com.gestiva.purchasing.receipt.entity.GoodsReceipt
            header() {

        return GoodsReceiptPartialHeaderFactory.create(
                2L, 1L, 7L,
                order(),
                "GR-00025",
                LocalDate.of(2026, 11, 4)
        );
    }

    private PurchaseOrderLine orderLine(
            long id,
            long tenantId,
            long orderId) {

        PurchaseOrderLine line = mock(PurchaseOrderLine.class);

        when(line.getId()).thenReturn(id);
        when(line.getTenantId()).thenReturn(tenantId);
        when(line.getPurchaseOrderId()).thenReturn(orderId);
        when(line.getItemId()).thenReturn(id + 100);
        when(line.getDescription()).thenReturn("Articolo " + id);

        return line;
    }

    private GoodsReceiptPartialDocumentAssembler.LineInput
            input(long id, String previous, String requested) {

        var planned =
                GoodsReceiptPartialFifoLineCalculator.calculate(
                        bd("20.000"),
                        bd(previous),
                        bd(requested),
                        bd("144.00")
                );

        return new GoodsReceiptPartialDocumentAssembler.LineInput(
                orderLine(id, 2L, 31L),
                planned
        );
    }

    @Test
    void assemblesTwoLinesUsingRealReceiptId() {

        var plan = GoodsReceiptPartialDocumentAssembler.prepare(
                header(),
                List.of(
                        input(101L, "0.000", "8.000"),
                        input(102L, "8.000", "12.000")
                )
        );

        assertNull(plan.header().getId());

        var lines = plan.buildLines(500L);

        assertEquals(2, lines.size());

        assertEquals(500L, lines.get(0).getGoodsReceiptId());
        assertEquals(500L, lines.get(1).getGoodsReceiptId());

        assertEquals(1, lines.get(0).getLineNo());
        assertEquals(2, lines.get(1).getLineNo());

        assertEquals(101L, lines.get(0).getPurchaseOrderLineId());
        assertEquals(102L, lines.get(1).getPurchaseOrderLineId());

        assertEquals(
                0,
                bd("144.0000").compareTo(
                        lines.get(0).getTotalCost().add(
                                lines.get(1).getTotalCost()
                        )
                )
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> lines.clear()
        );

        System.out.println(
                "PARTIAL DOCUMENT: due righe costruite con ID reale"
        );
    }

    @Test
    void rejectsDuplicateOrderLine() {

        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialDocumentAssembler.prepare(
                        header(),
                        List.of(
                                input(101L, "0.000", "8.000"),
                                input(101L, "8.000", "12.000")
                        )
                )
        );

        System.out.println(
                "PARTIAL DOCUMENT: righe ordine duplicate rifiutate"
        );
    }

    @Test
    void rejectsLineFromDifferentPurchaseOrder() {

        var wrongLine = new GoodsReceiptPartialDocumentAssembler.LineInput(
                orderLine(101L, 2L, 99L),
                input(102L, "0.000", "8.000").planned()
        );

        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialDocumentAssembler.prepare(
                        header(),
                        List.of(wrongLine)
                )
        );

        System.out.println(
                "PARTIAL DOCUMENT: ordine differente rifiutato"
        );
    }

    @Test
    void rejectsLineFromDifferentTenant() {

        var wrongLine = new GoodsReceiptPartialDocumentAssembler.LineInput(
                orderLine(101L, 9L, 31L),
                input(102L, "0.000", "8.000").planned()
        );

        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialDocumentAssembler.prepare(
                        header(),
                        List.of(wrongLine)
                )
        );

        System.out.println(
                "PARTIAL DOCUMENT: tenant differente rifiutato"
        );
    }

    @Test
    void rejectsMissingSavedReceiptId() {

        var plan = GoodsReceiptPartialDocumentAssembler.prepare(
                header(),
                List.of(input(101L, "0.000", "8.000"))
        );

        assertThrows(
                BusinessException.class,
                () -> plan.buildLines(null)
        );

        assertThrows(
                BusinessException.class,
                () -> plan.buildLines(0L)
        );

        System.out.println(
                "PARTIAL DOCUMENT: ID ricevimento mancante rifiutato"
        );
    }
}
