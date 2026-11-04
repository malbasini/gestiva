package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptFifoCostReconcilerTest {

    private BigDecimal reconcile(String quantity, String cost) {
        return GoodsReceiptFifoCostReconciler.reconcile(
                new BigDecimal(quantity),
                new BigDecimal(cost)
        );
    }

    @Test
    void firstPartialReceiptReconciles() {
        assertEquals(
                new BigDecimal("7.2000"),
                reconcile("8", "57.6000")
        );
    }

    @Test
    void secondPartialReceiptReconciles() {
        assertEquals(
                new BigDecimal("7.2000"),
                reconcile("7", "50.4000")
        );
    }

    @Test
    void finalPartialReceiptReconciles() {
        assertEquals(
                new BigDecimal("7.2000"),
                reconcile("5", "36.0000")
        );
    }

    @Test
    void rejectsUnrepresentableFifoTotal() {
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> reconcile("3", "0.0100")
        );

        assertTrue(
                ex.getMessage().contains("riconciliabile")
        );
    }

    @Test
    void smallSingleUnitCanReconcile() {
        assertEquals(
                new BigDecimal("0.0034"),
                reconcile("1", "0.0034")
        );
    }

    @Test
    void rejectsZeroCost() {
        assertThrows(
                BusinessException.class,
                () -> reconcile("5", "0")
        );
    }

    @Test
    void rejectsInvalidQuantityPrecision() {
        assertThrows(
                BusinessException.class,
                () -> reconcile("1.0001", "10")
        );
    }

    @Test
    void rejectsUnrepresentableUnitCost() {
        assertThrows(
                BusinessException.class,
                () -> reconcile("0.001", "99999999999.0000")
        );
    }
}
