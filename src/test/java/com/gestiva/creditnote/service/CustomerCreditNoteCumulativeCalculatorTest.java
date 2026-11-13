
package com.gestiva.creditnote.service;

import com.gestiva.billing.invoice.entity.InvoiceLine;
import com.gestiva.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CustomerCreditNoteCumulativeCalculatorTest {

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private InvoiceLine original() {
        InvoiceLine line = new InvoiceLine();
        line.setQuantity(bd("3.000"));
        line.setLineTotal(bd("100.00"));
        line.setTaxAmount(bd("22.00"));
        return line;
    }

    private CustomerCreditNoteCumulativeCalculator.PreviousCredits
    previous(String qty, String subtotal, String tax) {
        return new CustomerCreditNoteCumulativeCalculator.PreviousCredits(
                bd(qty), bd(subtotal), bd(tax));
    }

    @Test
    void calculatesRealInvoicePartialCreditWithoutSubtractingTaxTwice() {
        InvoiceLine original = new InvoiceLine();
        original.setQuantity(bd("2.000"));
        original.setLineTotal(bd("153.00"));
        original.setTaxAmount(bd("33.66"));

        var first = CustomerCreditNoteCumulativeCalculator.calculate(
                original,
                previous("0.000", "0.00", "0.00"),
                bd("1.000"));

        assertEquals(0, bd("76.50").compareTo(first.subtotal()));
        assertEquals(0, bd("16.83").compareTo(first.tax()));
        assertEquals(0, bd("93.33").compareTo(first.total()));

        var second = CustomerCreditNoteCumulativeCalculator.calculate(
                original,
                previous("1.000", "76.50", "16.83"),
                bd("1.000"));

        assertEquals(0, bd("76.50").compareTo(second.subtotal()));
        assertEquals(0, bd("16.83").compareTo(second.tax()));
        assertEquals(0, bd("93.33").compareTo(second.total()));
    }

    @Test
    void calculatesFirstPartialCredit() {
        var result = CustomerCreditNoteCumulativeCalculator.calculate(
                original(),
                previous("0.000", "0.00", "0.00"),
                bd("1.000"));

        assertEquals(0, bd("1.000").compareTo(result.quantity()));
        assertEquals(0, bd("33.33").compareTo(result.subtotal()));
        assertEquals(0, bd("7.33").compareTo(result.tax()));
        assertEquals(0, bd("40.66").compareTo(result.total()));
    }

    @Test
    void lastCreditRecoversExactRemainingCents() {
        var result = CustomerCreditNoteCumulativeCalculator.calculate(
                original(),
                previous("2.000", "66.66", "14.66"),
                bd("1.000"));

        assertEquals(0, bd("33.34").compareTo(result.subtotal()));
        assertEquals(0, bd("7.34").compareTo(result.tax()));
        assertEquals(0, bd("40.68").compareTo(result.total()));
    }

    @Test
    void rejectsQuantityAboveRemaining() {
        assertThrows(BusinessException.class,
                () -> CustomerCreditNoteCumulativeCalculator.calculate(
                        original(),
                        previous("2.000", "66.66", "14.66"),
                        bd("2.000")));
    }

    @Test
    void rejectsInconsistentPreviousAmounts() {
        assertThrows(BusinessException.class,
                () -> CustomerCreditNoteCumulativeCalculator.calculate(
                        original(),
                        previous("1.000", "101.00", "7.33"),
                        bd("1.000")));
    }

    @Test
    void rejectsInvalidRequestedQuantity() {
        assertThrows(BusinessException.class,
                () -> CustomerCreditNoteCumulativeCalculator.calculate(
                        original(),
                        previous("0.000", "0.00", "0.00"),
                        bd("0.000")));

        assertThrows(BusinessException.class,
                () -> CustomerCreditNoteCumulativeCalculator.calculate(
                        original(),
                        previous("0.000", "0.00", "0.00"),
                        bd("0.0001")));
    }
}
