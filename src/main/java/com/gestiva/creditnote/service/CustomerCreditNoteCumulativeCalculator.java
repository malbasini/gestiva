
package com.gestiva.creditnote.service;

import com.gestiva.billing.invoice.entity.InvoiceLine;
import com.gestiva.common.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CustomerCreditNoteCumulativeCalculator {

    private static final int QTY_SCALE = 3;
    private static final int MONEY_SCALE = 2;

    private CustomerCreditNoteCumulativeCalculator() {
    }

    public record PreviousCredits(
            BigDecimal quantity,
            BigDecimal subtotal,
            BigDecimal tax) {
    }

    public record PartialAmounts(
            BigDecimal quantity,
            BigDecimal subtotal,
            BigDecimal tax,
            BigDecimal total) {
    }

    public static PartialAmounts calculate(
            InvoiceLine original,
            PreviousCredits previous,
            BigDecimal requestedQuantity) {

        if (original == null
                || previous == null
                || requestedQuantity == null) {
            throw new BusinessException(
                    "Dati rettifica cliente mancanti.");
        }

        BigDecimal originalQty = original.getQuantity();
        BigDecimal originalTotal = original.getLineTotal();
        BigDecimal originalTax = original.getTaxAmount();

        if (originalQty == null
                || originalTotal == null
                || originalTax == null
                || originalQty.signum() <= 0
                || originalQty.scale() > QTY_SCALE
                || originalTotal.scale() > MONEY_SCALE
                || originalTax.scale() > MONEY_SCALE
                || originalTotal.signum() <= 0
                || originalTax.signum() < 0
                || originalTax.compareTo(originalTotal) > 0) {
            throw new BusinessException(
                    "Riga fattura cliente non valida.");
        }

        // InvoiceLine.lineTotal è l'imponibile IVA esclusa.
        BigDecimal originalSubtotal = originalTotal;

        BigDecimal usedQty = previous.quantity();
        BigDecimal usedSubtotal = previous.subtotal();
        BigDecimal usedTax = previous.tax();

        if (usedQty == null
                || usedSubtotal == null
                || usedTax == null
                || usedQty.signum() < 0
                || usedSubtotal.signum() < 0
                || usedTax.signum() < 0
                || usedQty.scale() > QTY_SCALE
                || usedSubtotal.scale() > MONEY_SCALE
                || usedTax.scale() > MONEY_SCALE
                || usedQty.compareTo(originalQty) > 0
                || usedSubtotal.compareTo(originalSubtotal) > 0
                || usedTax.compareTo(originalTax) > 0
                || (usedQty.signum() == 0
                && (usedSubtotal.signum() != 0
                || usedTax.signum() != 0))) {
            throw new BusinessException(
                    "Rettifiche cliente precedenti incoerenti.");
        }

        if (requestedQuantity.signum() <= 0
                || requestedQuantity.scale() > QTY_SCALE) {
            throw new BusinessException(
                    "Quantità da rettificare non valida.");
        }

        BigDecimal remainingQty =
                originalQty.subtract(usedQty);

        if (requestedQuantity.compareTo(remainingQty) > 0) {
            throw new BusinessException(
                    "Quantità cumulativa superiore alla fattura.");
        }

        BigDecimal subtotal;
        BigDecimal tax;

        if (requestedQuantity.compareTo(remainingQty) == 0) {

            // Ultima rettifica: recupero esatto
            // dei centesimi residui.
            subtotal = originalSubtotal.subtract(usedSubtotal);
            tax = originalTax.subtract(usedTax);

        } else {

            BigDecimal ratio = requestedQuantity.divide(
                    originalQty, 16, RoundingMode.HALF_UP);

            subtotal = originalSubtotal.multiply(ratio)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            tax = originalTax.multiply(ratio)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal total = subtotal.add(tax);

        if (subtotal.signum() < 0
                || tax.signum() < 0
                || total.signum() <= 0
                || subtotal.compareTo(
                originalSubtotal.subtract(usedSubtotal)) > 0
                || tax.compareTo(
                originalTax.subtract(usedTax)) > 0) {
            throw new BusinessException(
                    "Importi cumulativi superiori al residuo.");
        }

        return new PartialAmounts(
                requestedQuantity,
                subtotal,
                tax,
                total);
    }
}
