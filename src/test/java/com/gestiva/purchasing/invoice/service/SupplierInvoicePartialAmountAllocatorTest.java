package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SupplierInvoicePartialAmountAllocatorTest {

    private SupplierInvoicePartialAmountAllocator.Amounts allocate(
            String already, String requested) {

        return SupplierInvoicePartialAmountAllocator.allocate(
                new BigDecimal("20"),
                new BigDecimal(already),
                new BigDecimal(requested),
                new BigDecimal("144.00"),
                new BigDecimal("31.68"),
                new BigDecimal("175.68")
        );
    }

    @Test
    void firstPartialInvoiceIsCorrect() {
        var amounts = allocate("0", "8");

        assertEquals(
                new BigDecimal("57.60"),
                amounts.subtotal()
        );
        assertEquals(
                new BigDecimal("12.67"),
                amounts.tax()
        );
        assertEquals(
                new BigDecimal("70.27"),
                amounts.total()
        );
    }

    @Test
    void secondPartialInvoiceIsCorrect() {
        var amounts = allocate("8", "7");

        assertEquals(
                new BigDecimal("50.40"),
                amounts.subtotal()
        );
        assertEquals(
                new BigDecimal("11.09"),
                amounts.tax()
        );
        assertEquals(
                new BigDecimal("61.49"),
                amounts.total()
        );
    }

    @Test
    void finalPartialInvoiceIsCorrect() {
        var amounts = allocate("15", "5");

        assertEquals(
                new BigDecimal("36.00"),
                amounts.subtotal()
        );
        assertEquals(
                new BigDecimal("7.92"),
                amounts.tax()
        );
        assertEquals(
                new BigDecimal("43.92"),
                amounts.total()
        );
    }

    @Test
    void allInvoicesReconcileWithOriginalOrder() {
        var first = allocate("0", "8");
        var second = allocate("8", "7");
        var third = allocate("15", "5");

        assertEquals(
                new BigDecimal("144.00"),
                first.subtotal()
                        .add(second.subtotal())
                        .add(third.subtotal())
        );

        assertEquals(
                new BigDecimal("31.68"),
                first.tax()
                        .add(second.tax())
                        .add(third.tax())
        );

        assertEquals(
                new BigDecimal("175.68"),
                first.total()
                        .add(second.total())
                        .add(third.total())
        );
    }

    @Test
    void centRoundingReconcilesAcrossDeliveries() {
        var first = SupplierInvoicePartialAmountAllocator.allocate(
                new BigDecimal("3"),
                BigDecimal.ZERO,
                BigDecimal.ONE,
                new BigDecimal("0.01"),
                new BigDecimal("0.01"),
                new BigDecimal("0.02")
        );

        var second = SupplierInvoicePartialAmountAllocator.allocate(
                new BigDecimal("3"),
                BigDecimal.ONE,
                BigDecimal.ONE,
                new BigDecimal("0.01"),
                new BigDecimal("0.01"),
                new BigDecimal("0.02")
        );

        var third = SupplierInvoicePartialAmountAllocator.allocate(
                new BigDecimal("3"),
                new BigDecimal("2"),
                BigDecimal.ONE,
                new BigDecimal("0.01"),
                new BigDecimal("0.01"),
                new BigDecimal("0.02")
        );

        assertEquals(
                new BigDecimal("0.01"),
                first.subtotal()
                        .add(second.subtotal())
                        .add(third.subtotal())
        );

        assertEquals(
                new BigDecimal("0.01"),
                first.tax()
                        .add(second.tax())
                        .add(third.tax())
        );

        assertEquals(
                new BigDecimal("0.02"),
                first.total()
                        .add(second.total())
                        .add(third.total())
        );
    }

    @Test
    void rejectsOverInvoicing() {
        assertThrows(
                BusinessException.class,
                () -> allocate("18", "3")
        );
    }

    @Test
    void rejectsUnbalancedOriginalAmounts() {
        assertThrows(
                BusinessException.class,
                () -> SupplierInvoicePartialAmountAllocator.allocate(
                        new BigDecimal("20"),
                        BigDecimal.ZERO,
                        new BigDecimal("8"),
                        new BigDecimal("144.00"),
                        new BigDecimal("31.68"),
                        new BigDecimal("175.69")
                )
        );
    }

    @Test
    void rejectsExcessDecimalPrecision() {
        assertThrows(
                BusinessException.class,
                () -> SupplierInvoicePartialAmountAllocator.allocate(
                        new BigDecimal("20"),
                        BigDecimal.ZERO,
                        new BigDecimal("8.0001"),
                        new BigDecimal("144.00"),
                        new BigDecimal("31.68"),
                        new BigDecimal("175.68")
                )
        );
    }
}
