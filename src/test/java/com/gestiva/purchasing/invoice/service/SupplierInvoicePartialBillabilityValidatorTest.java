package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SupplierInvoicePartialBillabilityValidatorTest {

    private SupplierInvoicePartialBillabilityValidator.BillableLine
    validate(String already, String received) {

        return SupplierInvoicePartialBillabilityValidator.validate(
                new BigDecimal("20"),
                new BigDecimal(already),
                new BigDecimal(received),
                new BigDecimal("10"),
                new BigDecimal("144.00"),
                new BigDecimal("31.68"),
                new BigDecimal("175.68")
        );
    }

    @Test
    void firstPartialReceiptIsBillable() {
        var result = validate("0", "8");

        assertEquals(
                new BigDecimal("0.800"),
                result.commercialQuantity()
        );
        assertEquals(
                new BigDecimal("57.60"),
                result.subtotal()
        );
        assertEquals(
                new BigDecimal("12.67"),
                result.tax()
        );
        assertEquals(
                new BigDecimal("70.27"),
                result.total()
        );
    }

    @Test
    void secondPartialReceiptIsBillable() {
        var result = validate("8", "7");

        assertEquals(
                new BigDecimal("0.700"),
                result.commercialQuantity()
        );
        assertEquals(
                new BigDecimal("50.40"),
                result.subtotal()
        );
        assertEquals(
                new BigDecimal("11.09"),
                result.tax()
        );
    }

    @Test
    void finalPartialReceiptIsBillable() {
        var result = validate("15", "5");

        assertEquals(
                new BigDecimal("0.500"),
                result.commercialQuantity()
        );
        assertEquals(
                new BigDecimal("43.92"),
                result.total()
        );
    }

    @Test
    void completeReceiptPreservesOriginalAmounts() {
        var result = validate("0", "20");

        assertEquals(
                new BigDecimal("2.000"),
                result.commercialQuantity()
        );
        assertEquals(
                new BigDecimal("144.00"),
                result.subtotal()
        );
        assertEquals(
                new BigDecimal("31.68"),
                result.tax()
        );
        assertEquals(
                new BigDecimal("175.68"),
                result.total()
        );
    }

    @Test
    void rejectsInexactCommercialConversion() {
        assertThrows(
                BusinessException.class,
                () -> SupplierInvoicePartialBillabilityValidator
                        .validate(
                                new BigDecimal("3"),
                                BigDecimal.ZERO,
                                BigDecimal.ONE,
                                new BigDecimal("3"),
                                new BigDecimal("9.00"),
                                new BigDecimal("1.98"),
                                new BigDecimal("10.98")
                        )
        );
    }

    @Test
    void rejectsZeroAllocatedSubtotal() {
        assertThrows(
                BusinessException.class,
                () -> SupplierInvoicePartialBillabilityValidator
                        .validate(
                                new BigDecimal("1000"),
                                BigDecimal.ZERO,
                                BigDecimal.ONE,
                                BigDecimal.ONE,
                                new BigDecimal("0.01"),
                                BigDecimal.ZERO,
                                new BigDecimal("0.01")
                        )
        );
    }

    @Test
    void rejectsOverReceivedQuantity() {
        assertThrows(
                BusinessException.class,
                () -> validate("18", "3")
        );
    }
}
