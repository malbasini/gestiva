package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptRequestedQuantityValidatorTest {

    @Test
    void acceptsPartialQuantity() {
        BigDecimal result =
                GoodsReceiptRequestedQuantityValidator.validate(
                        new BigDecimal("7"),
                        new BigDecimal("12")
                );

        assertEquals(new BigDecimal("7.000"), result);
    }

    @Test
    void acceptsExactRemainingQuantity() {
        BigDecimal result =
                GoodsReceiptRequestedQuantityValidator.validate(
                        new BigDecimal("12"),
                        new BigDecimal("12")
                );

        assertEquals(new BigDecimal("12.000"), result);
    }

    @Test
    void rejectsQuantityGreaterThanRemaining() {
        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptRequestedQuantityValidator.validate(
                        new BigDecimal("13"),
                        new BigDecimal("12")
                )
        );
    }

    @Test
    void rejectsZeroQuantity() {
        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptRequestedQuantityValidator.validate(
                        BigDecimal.ZERO,
                        new BigDecimal("12")
                )
        );
    }

    @Test
    void rejectsReceivingCompletedOrder() {
        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptRequestedQuantityValidator.validate(
                        BigDecimal.ONE,
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void rejectsExcessDecimalPrecision() {
        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptRequestedQuantityValidator.validate(
                        new BigDecimal("1.0001"),
                        new BigDecimal("12")
                )
        );
    }
}
