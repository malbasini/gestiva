package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptQuantityCalculatorTest {

    @Test
    void calculatesFullOutstandingQuantity() {
        var result = GoodsReceiptQuantityCalculator.remainingBaseQuantity(
                new BigDecimal("2"),
                new BigDecimal("10"),
                BigDecimal.ZERO
        );

        assertEquals(new BigDecimal("20.000"), result);
    }

    @Test
    void calculatesPartialOutstandingQuantity() {
        var result = GoodsReceiptQuantityCalculator.remainingBaseQuantity(
                new BigDecimal("2"),
                new BigDecimal("10"),
                new BigDecimal("8")
        );

        assertEquals(new BigDecimal("12.000"), result);
    }

    @Test
    void returnsZeroWhenOrderIsFullyReceived() {
        var result = GoodsReceiptQuantityCalculator.remainingBaseQuantity(
                new BigDecimal("2"),
                new BigDecimal("10"),
                new BigDecimal("20")
        );

        assertEquals(new BigDecimal("0.000"), result);
    }

    @Test
    void rejectsOverReceipt() {
        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptQuantityCalculator.remainingBaseQuantity(
                        new BigDecimal("2"),
                        new BigDecimal("10"),
                        new BigDecimal("21")
                )
        );
    }

    @Test
    void rejectsInvalidConversionFactor() {
        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptQuantityCalculator.remainingBaseQuantity(
                        new BigDecimal("2"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void rejectsQuantitiesWithExcessPrecision() {
        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptQuantityCalculator.remainingBaseQuantity(
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        new BigDecimal("0.0001")
                )
        );
    }
}
