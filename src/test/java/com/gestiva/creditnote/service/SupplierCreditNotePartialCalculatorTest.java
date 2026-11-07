package com.gestiva.creditnote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.entity.SupplierInvoiceLine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SupplierCreditNotePartialCalculatorTest {

    private SupplierInvoiceLine original(
            String quantity,
            String subtotal,
            String tax,
            String total) {

        SupplierInvoiceLine line = new SupplierInvoiceLine();
        line.setQuantity(new BigDecimal(quantity));
        line.setLineSubtotal(new BigDecimal(subtotal));
        line.setTaxAmount(new BigDecimal(tax));
        line.setLineTotal(new BigDecimal(total));
        return line;
    }

    private void moneyEquals(String expected, BigDecimal actual) {
        assertEquals(
                0,
                new BigDecimal(expected).compareTo(actual)
        );
    }

    @Test
    void fullQuantityPreservesExactOriginalAmounts() {
        var line = original(
                "0.800", "51.84", "11.40", "63.24");

        var result = SupplierCreditNotePartialCalculator.calculate(
                line, new BigDecimal("0.800"));

        moneyEquals("0.800", result.quantity());
        moneyEquals("51.84", result.subtotal());
        moneyEquals("11.40", result.tax());
        moneyEquals("63.24", result.total());
    }

    @Test
    void halfQuantityCalculatesProportionalAmounts() {
        var line = original(
                "0.800", "51.84", "11.40", "63.24");

        var result = SupplierCreditNotePartialCalculator.calculate(
                line, new BigDecimal("0.400"));

        moneyEquals("0.400", result.quantity());
        moneyEquals("25.92", result.subtotal());
        moneyEquals("5.70", result.tax());
        moneyEquals("31.62", result.total());
    }

    @Test
    void partialQuantityAppliesHalfUpRounding() {
        var line = original(
                "3.000", "10.00", "2.20", "12.20");

        var result = SupplierCreditNotePartialCalculator.calculate(
                line, new BigDecimal("1.000"));

        moneyEquals("3.33", result.subtotal());
        moneyEquals("0.73", result.tax());
        moneyEquals("4.06", result.total());
    }

    @Test
    void rejectsQuantityExceedingOriginal() {
        var line = original(
                "0.800", "51.84", "11.40", "63.24");

        assertThrows(
                BusinessException.class,
                () -> SupplierCreditNotePartialCalculator.calculate(
                        line, new BigDecimal("0.801"))
        );
    }

    @Test
    void rejectsZeroOrNegativeQuantity() {
        var line = original(
                "0.800", "51.84", "11.40", "63.24");

        assertThrows(
                BusinessException.class,
                () -> SupplierCreditNotePartialCalculator.calculate(
                        line, BigDecimal.ZERO)
        );

        assertThrows(
                BusinessException.class,
                () -> SupplierCreditNotePartialCalculator.calculate(
                        line, new BigDecimal("-0.100"))
        );
    }

    @Test
    void rejectsMoreThanThreeQuantityDecimals() {
        var line = original(
                "0.800", "51.84", "11.40", "63.24");

        assertThrows(
                BusinessException.class,
                () -> SupplierCreditNotePartialCalculator.calculate(
                        line, new BigDecimal("0.4001"))
        );
    }

    @Test
    void rejectsInconsistentOriginalAmounts() {
        var line = original(
                "0.800", "51.84", "11.40", "63.23");

        assertThrows(
                BusinessException.class,
                () -> SupplierCreditNotePartialCalculator.calculate(
                        line, new BigDecimal("0.400"))
        );
    }

    @Test
    void rejectsZeroTotalOriginalLine() {
        var line = original(
                "1.000", "0.00", "0.00", "0.00");

        assertThrows(
                BusinessException.class,
                () -> SupplierCreditNotePartialCalculator.calculate(
                        line, new BigDecimal("0.500"))
        );
    }
}
