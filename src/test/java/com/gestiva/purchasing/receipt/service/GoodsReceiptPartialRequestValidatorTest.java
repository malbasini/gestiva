package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptPartialRequestValidatorTest {

    private final Map<Long, BigDecimal> remaining =
            Map.of(
                    101L, new BigDecimal("12.000"),
                    102L, new BigDecimal("5.000"),
                    103L, new BigDecimal("20.000")
            );

    @Test
    void acceptsSelectedOrderLines() {
        var request = Map.of(
                101L, new BigDecimal("8"),
                102L, new BigDecimal("3")
        );

        var result =
                GoodsReceiptPartialRequestValidator.validate(
                        request, remaining
                );

        assertEquals(2, result.size());
        assertEquals(
                new BigDecimal("8.000"),
                result.get(101L)
        );
        assertFalse(result.containsKey(103L));
    }

    @Test
    void acceptsCompleteRemainingQuantity() {
        var result =
                GoodsReceiptPartialRequestValidator.validate(
                        Map.of(101L, new BigDecimal("12")),
                        remaining
                );

        assertEquals(
                new BigDecimal("12.000"),
                result.get(101L)
        );
    }

    @Test
    void rejectsUnknownOrderLine() {
        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialRequestValidator.validate(
                        Map.of(999L, BigDecimal.ONE),
                        remaining
                )
        );
    }

    @Test
    void rejectsOverReceipt() {
        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialRequestValidator.validate(
                        Map.of(101L, new BigDecimal("13")),
                        remaining
                )
        );
    }

    @Test
    void rejectsZeroQuantity() {
        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialRequestValidator.validate(
                        Map.of(101L, BigDecimal.ZERO),
                        remaining
                )
        );
    }

    @Test
    void rejectsEmptyRequest() {
        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialRequestValidator.validate(
                        Map.of(),
                        remaining
                )
        );
    }

    @Test
    void rejectsNullQuantity() {
        var request = new LinkedHashMap<Long, BigDecimal>();
        request.put(101L, null);

        assertThrows(
                BusinessException.class,
                () -> GoodsReceiptPartialRequestValidator.validate(
                        request,
                        remaining
                )
        );
    }

    @Test
    void returnsUnmodifiableValidatedQuantities() {
        var result =
                GoodsReceiptPartialRequestValidator.validate(
                        Map.of(101L, BigDecimal.ONE),
                        remaining
                );

        assertThrows(
                UnsupportedOperationException.class,
                () -> result.put(102L, BigDecimal.ONE)
        );
    }
}
