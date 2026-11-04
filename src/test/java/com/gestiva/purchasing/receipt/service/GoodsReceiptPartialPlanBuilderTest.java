package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoodsReceiptPartialPlanBuilderTest {

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private GoodsReceipt header() {
        GoodsReceipt header = mock(GoodsReceipt.class);

        when(header.getTenantId()).thenReturn(2L);
        when(header.getCompanyId()).thenReturn(1L);
        when(header.getPurchaseOrderId()).thenReturn(31L);

        return header;
    }

    private PurchaseOrderLine line(
            long id,
            String quantity,
            String subtotal) {

        PurchaseOrderLine line = mock(PurchaseOrderLine.class);

        when(line.getId()).thenReturn(id);
        when(line.getTenantId()).thenReturn(2L);
        when(line.getPurchaseOrderId()).thenReturn(31L);
        when(line.getUomId()).thenReturn(5L);
        when(line.getQuantity()).thenReturn(bd(quantity));
        when(line.getConversionFactor())
                .thenReturn(bd("10.000000"));
        when(line.getLineSubtotal()).thenReturn(bd(subtotal));
        when(line.getItemId()).thenReturn(id + 100L);
        when(line.getDescription()).thenReturn("Articolo " + id);

        return line;
    }

    @Test
    void preparesTwoOrderLinesInOriginalOrder() {

        var plan = GoodsReceiptPartialPlanBuilder.build(
                header(),
                List.of(
                        line(101L, "2.000", "144.00"),
                        line(102L, "1.000", "72.00")
                ),
                Map.of(
                        102L, bd("6.000"),
                        101L, bd("8.000")
                ),
                Map.of(
                        101L, bd("0.000"),
                        102L, bd("0.000")
                )
        );

        var lines = plan.buildLines(500L);

        assertEquals(2, lines.size());
        assertEquals(101L, lines.get(0).getPurchaseOrderLineId());
        assertEquals(102L, lines.get(1).getPurchaseOrderLineId());

        assertEquals(
                0, bd("57.6000").compareTo(
                        lines.get(0).getTotalCost())
        );

        assertEquals(
                0, bd("43.2000").compareTo(
                        lines.get(1).getTotalCost())
        );

        System.out.println(
                "PARTIAL PLAN: due righe ordinate e valorizzate"
        );
    }

    @Test
    void preparesSecondReceiptUsingPreviousQuantity() {

        var plan = GoodsReceiptPartialPlanBuilder.build(
                header(),
                List.of(line(101L, "2.000", "144.00")),
                Map.of(101L, bd("12.000")),
                Map.of(101L, bd("8.000"))
        );

        var lines = plan.buildLines(501L);

        assertEquals(1, lines.size());

        assertEquals(
                0, bd("86.4000").compareTo(
                        lines.get(0).getTotalCost())
        );

        assertEquals(
                0, bd("12.000").compareTo(
                        lines.get(0).getQuantityReceived())
        );

        System.out.println(
                "PARTIAL PLAN: seconda consegna usa quantità storica"
        );
    }

    @Test
    void rejectsUnknownRequestedLine() {

        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialPlanBuilder.build(
                        header(),
                        List.of(line(101L, "2.000", "144.00")),
                        Map.of(999L, bd("5.000")),
                        Map.of(101L, bd("0.000"))
                )
        );

        System.out.println(
                "PARTIAL PLAN: riga richiesta estranea rifiutata"
        );
    }

    @Test
    void rejectsMissingHistoricalQuantity() {

        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialPlanBuilder.build(
                        header(),
                        List.of(line(101L, "2.000", "144.00")),
                        Map.of(101L, bd("8.000")),
                        Map.of()
                )
        );

        System.out.println(
                "PARTIAL PLAN: storico mancante rifiutato"
        );
    }

    @Test
    void rejectsOverReceipt() {

        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialPlanBuilder.build(
                        header(),
                        List.of(line(101L, "2.000", "144.00")),
                        Map.of(101L, bd("13.000")),
                        Map.of(101L, bd("8.000"))
                )
        );

        System.out.println(
                "PARTIAL PLAN: quantità eccedente rifiutata"
        );
    }

    @Test
    void rejectsNonReconciliableFifoCost() {

        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialPlanBuilder.build(
                        header(),
                        List.of(line(101L, "0.300", "1.00")),
                        Map.of(101L, bd("3.000")),
                        Map.of(101L, bd("0.000"))
                )
        );

        System.out.println(
                "PARTIAL PLAN: costo FIFO non quadrabile rifiutato"
        );
    }
}
