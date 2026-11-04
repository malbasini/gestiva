package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoodsReceiptPartialLineFactoryTest {

    private static BigDecimal bd(String text) {
        return new BigDecimal(text);
    }

    private PurchaseOrderLine orderLine(Long itemId) {
        PurchaseOrderLine poLine =
                mock(PurchaseOrderLine.class);

        when(poLine.getId()).thenReturn(31L);
        when(poLine.getItemId()).thenReturn(itemId);
        when(poLine.getDescription())
                .thenReturn("Articolo di collaudo");

        return poLine;
    }

    private GoodsReceiptPartialFifoLineCalculator.PlannedLine plan() {
        return GoodsReceiptPartialFifoLineCalculator.calculate(
                bd("20.000"),
                bd("0.000"),
                bd("8.000"),
                bd("144.00")
        );
    }

    @Test
    void createsCompleteLineWithoutPersistence() {

        var line = GoodsReceiptPartialLineFactory.create(
                2L,
                100L,
                1,
                orderLine(15L),
                plan()
        );

        assertEquals(2L, line.getTenantId());
        assertEquals(100L, line.getGoodsReceiptId());
        assertEquals(1, line.getLineNo());
        assertEquals(31L, line.getPurchaseOrderLineId());
        assertEquals(15L, line.getItemId());
        assertEquals(
                "Articolo di collaudo",
                line.getDescription()
        );

        assertEquals(
                bd("8.000"),
                line.getQuantityReceived()
        );

        assertEquals(
                bd("7.2000"),
                line.getUnitCost()
        );

        assertEquals(
                bd("57.6000"),
                line.getTotalCost()
        );

        System.out.println(
                "PARTIAL LINE: entità creata con quantità e costi corretti"
        );
    }

    @Test
    void preservesNullableItemId() {

        var line = GoodsReceiptPartialLineFactory.create(
                2L,
                100L,
                1,
                orderLine(null),
                plan()
        );

        assertNull(line.getItemId());

        System.out.println(
                "PARTIAL LINE: itemId nullo preservato"
        );
    }

    @Test
    void rejectsInvalidDocumentReferences() {

        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialLineFactory.create(
                        2L,
                        null,
                        1,
                        orderLine(15L),
                        plan()
                )
        );

        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialLineFactory.create(
                        2L,
                        100L,
                        0,
                        orderLine(15L),
                        plan()
                )
        );

        System.out.println(
                "PARTIAL LINE: riferimenti non validi rifiutati"
        );
    }

    @Test
    void rejectsInconsistentPlannedUnitCost() {

        var inconsistent =
                new GoodsReceiptPartialFifoLineCalculator.PlannedLine(
                        bd("8.000"),
                        bd("7.2100"),
                        bd("57.6000")
                );

        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialLineFactory.create(
                        2L,
                        100L,
                        1,
                        orderLine(15L),
                        inconsistent
                )
        );

        System.out.println(
                "PARTIAL LINE: costo FIFO incoerente rifiutato"
        );
    }
}
