package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptPartialCostAllocatorTest {

    private BigDecimal allocate(
            String already, String requested) {
        return GoodsReceiptPartialCostAllocator.allocate(
                new BigDecimal("20.000"),
                new BigDecimal(already),
                new BigDecimal(requested),
                new BigDecimal("144.00")
        );
    }

    @Test
    void firstReceiptGetsProportionalCost() {
        assertEquals(
                new BigDecimal("57.6000"),
                allocate("0", "8")
        );
    }

    @Test
    void secondReceiptGetsProportionalCost() {
        assertEquals(
                new BigDecimal("50.4000"),
                allocate("8", "7")
        );
    }

    @Test
    void finalReceiptGetsRemainingCost() {
        assertEquals(
                new BigDecimal("36.0000"),
                allocate("15", "5")
        );
    }

    @Test
    void threeReceiptsReconcileToOrderSubtotal() {
        BigDecimal total = allocate("0", "8")
                .add(allocate("8", "7"))
                .add(allocate("15", "5"));

        assertEquals(new BigDecimal("144.0000"), total);
    }

    @Test
    void cumulativeRoundingReconcilesSmallAmounts() {
        BigDecimal ordered = new BigDecimal("3");
        BigDecimal subtotal = new BigDecimal("0.01");

        BigDecimal first = GoodsReceiptPartialCostAllocator.allocate(
                ordered, BigDecimal.ZERO, BigDecimal.ONE, subtotal
        );

        BigDecimal second = GoodsReceiptPartialCostAllocator.allocate(
                ordered, BigDecimal.ONE, BigDecimal.ONE, subtotal
        );

        BigDecimal third = GoodsReceiptPartialCostAllocator.allocate(
                ordered, new BigDecimal("2"), BigDecimal.ONE, subtotal
        );

        assertEquals(new BigDecimal("0.0033"), first);
        assertEquals(new BigDecimal("0.0034"), second);
        assertEquals(new BigDecimal("0.0033"), third);

        assertEquals(
                new BigDecimal("0.0100"),
                first.add(second).add(third)
        );
    }

    @Test
    void rejectsOverReceipt() {
        assertThrows(
                BusinessException.class,
                () -> allocate("15", "6")
        );
    }

    @Test
    void rejectsZeroRequestedQuantity() {
        assertThrows(
                BusinessException.class,
                () -> allocate("8", "0")
        );
    }

    @Test
    void rejectsInexactBaseQuantity() {
        assertThrows(
                BusinessException.class,
                () -> allocate("8", "1.0001")
        );
    }
}
