package com.gestiva.creditnote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.entity.SupplierInvoiceLine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SupplierCreditNoteCumulativeCalculatorTest {

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private SupplierInvoiceLine original() {
        var line = new SupplierInvoiceLine();
        line.setQuantity(bd("3.000"));
        line.setLineSubtotal(bd("10.00"));
        line.setTaxAmount(bd("2.20"));
        line.setLineTotal(bd("12.20"));
        return line;
    }

    private SupplierCreditNoteCumulativeCalculator.PreviousCredits
    previous(String quantity, String subtotal, String tax) {
        return new SupplierCreditNoteCumulativeCalculator.PreviousCredits(
                bd(quantity), bd(subtotal), bd(tax));
    }

    private void equalAmount(String expected, BigDecimal actual) {
        assertEquals(0, bd(expected).compareTo(actual));
    }

    @Test
    void threeConsecutiveCreditsRecoverExactRemainder() {
        var line = original();

        var first = SupplierCreditNoteCumulativeCalculator.calculate(
                line, previous("0.000", "0.00", "0.00"), bd("1.000"));

        equalAmount("3.33", first.subtotal());
        equalAmount("0.73", first.tax());
        equalAmount("4.06", first.total());

        var second = SupplierCreditNoteCumulativeCalculator.calculate(
                line,
                previous("1.000", "3.33", "0.73"),
                bd("1.000"));

        equalAmount("3.33", second.subtotal());
        equalAmount("0.73", second.tax());
        equalAmount("4.06", second.total());

        var third = SupplierCreditNoteCumulativeCalculator.calculate(
                line,
                previous("2.000", "6.66", "1.46"),
                bd("1.000"));

        equalAmount("3.34", third.subtotal());
        equalAmount("0.74", third.tax());
        equalAmount("4.08", third.total());

        equalAmount("10.00", first.subtotal()
                .add(second.subtotal()).add(third.subtotal()));

        equalAmount("2.20", first.tax()
                .add(second.tax()).add(third.tax()));

        equalAmount("12.20", first.total()
                .add(second.total()).add(third.total()));
    }

    @Test
    void fullCreditWithoutPreviousPreservesOriginal() {
        var result = SupplierCreditNoteCumulativeCalculator.calculate(
                original(),
                previous("0.000", "0.00", "0.00"),
                bd("3.000"));

        equalAmount("3.000", result.quantity());
        equalAmount("10.00", result.subtotal());
        equalAmount("2.20", result.tax());
        equalAmount("12.20", result.total());
    }

    @Test
    void rejectsQuantityGreaterThanRemaining() {
        assertThrows(
                BusinessException.class,
                () -> SupplierCreditNoteCumulativeCalculator.calculate(
                        original(),
                        previous("2.000", "6.66", "1.46"),
                        bd("1.001"))
        );
    }

    @Test
    void rejectsAlreadyFullyCreditedLine() {
        assertThrows(
                BusinessException.class,
                () -> SupplierCreditNoteCumulativeCalculator.calculate(
                        original(),
                        previous("3.000", "10.00", "2.20"),
                        bd("0.001"))
        );
    }

    @Test
    void rejectsPreviousAmountsGreaterThanOriginal() {
        assertThrows(
                BusinessException.class,
                () -> SupplierCreditNoteCumulativeCalculator.calculate(
                        original(),
                        previous("1.000", "10.01", "0.73"),
                        bd("1.000"))
        );
    }

    @Test
    void rejectsPreviousAmountsWithoutPreviousQuantity() {
        assertThrows(
                BusinessException.class,
                () -> SupplierCreditNoteCumulativeCalculator.calculate(
                        original(),
                        previous("0.000", "3.33", "0.73"),
                        bd("1.000"))
        );
    }
}
