package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SupplierInvoicePartialQuantityConverterTest {

    private BigDecimal convert(String base, String factor) {
        return SupplierInvoicePartialQuantityConverter
                .toCommercialQuantity(
                        new BigDecimal(base),
                        new BigDecimal(factor)
                );
    }

    @Test
    void eightPiecesOfTenPieceBoxBecomePointEightBoxes() {
        assertEquals(
                new BigDecimal("0.800"),
                convert("8.000", "10.000000")
        );
    }

    @Test
    void completeOrderPreservesCommercialQuantity() {
        assertEquals(
                new BigDecimal("2.000"),
                convert("20.000", "10.000000")
        );
    }

    @Test
    void oneToOneConversionPreservesQuantity() {
        assertEquals(
                new BigDecimal("7.000"),
                convert("7.000", "1.000000")
        );
    }

    @Test
    void exactFractionalQuantityIsAccepted() {
        assertEquals(
                new BigDecimal("0.125"),
                convert("1.000", "8.000000")
        );
    }

    @Test
    void inexactCommercialQuantityIsRejected() {
        assertThrows(
                BusinessException.class,
                () -> convert("1.000", "3.000000")
        );
    }

    @Test
    void zeroConversionFactorIsRejected() {
        assertThrows(
                BusinessException.class,
                () -> convert("8.000", "0")
        );
    }

    @Test
    void excessiveBasePrecisionIsRejected() {
        assertThrows(
                BusinessException.class,
                () -> convert("8.0001", "10")
        );
    }

    @Test
    void excessiveCommercialQuantityIsRejected() {
        assertThrows(
                BusinessException.class,
                () -> convert("999999999999.999", "0.001000")
        );
    }
}
