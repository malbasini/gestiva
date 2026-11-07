package com.gestiva.creditnote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.entity.SupplierInvoiceLine;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class SupplierCreditNoteCumulativeCalculator {

    private static final int QUANTITY_SCALE = 3;
    private static final int MONEY_SCALE = 2;

    private SupplierCreditNoteCumulativeCalculator() {
    }

    public record PreviousCredits(
            BigDecimal quantity,
            BigDecimal subtotal,
            BigDecimal tax) {
    }

    public static SupplierCreditNotePartialCalculator.PartialAmounts
    calculate(
            SupplierInvoiceLine original,
            PreviousCredits previous,
            BigDecimal requestedQuantity) {

        if (original == null || previous == null
                || requestedQuantity == null) {
            throw new BusinessException(
                    "Dati della rettifica cumulativa mancanti.");
        }

        BigDecimal originalQty = original.getQuantity();
        BigDecimal originalSubtotal = original.getLineSubtotal();
        BigDecimal originalTax = original.getTaxAmount();
        BigDecimal originalTotal = original.getLineTotal();

        if (originalQty == null || originalSubtotal == null
                || originalTax == null || originalTotal == null
                || originalQty.signum() <= 0
                || originalQty.scale() > QUANTITY_SCALE
                || originalSubtotal.signum() < 0
                || originalTax.signum() < 0
                || originalTotal.signum() <= 0
                || originalSubtotal.add(originalTax)
                        .compareTo(originalTotal) != 0) {
            throw new BusinessException(
                    "Riga della fattura originale non valida.");
        }

        BigDecimal usedQty = previous.quantity();
        BigDecimal usedSubtotal = previous.subtotal();
        BigDecimal usedTax = previous.tax();

        if (usedQty == null || usedSubtotal == null
                || usedTax == null
                || usedQty.signum() < 0
                || usedSubtotal.signum() < 0
                || usedTax.signum() < 0
                || usedQty.scale() > QUANTITY_SCALE
                || usedQty.compareTo(originalQty) > 0
                || usedSubtotal.compareTo(originalSubtotal) > 0
                || usedTax.compareTo(originalTax) > 0
                || (usedQty.signum() == 0
                    && (usedSubtotal.signum() != 0
                        || usedTax.signum() != 0))) {
            throw new BusinessException(
                    "Rettifiche precedenti incoerenti.");
        }

        if (requestedQuantity.signum() <= 0
                || requestedQuantity.scale() > QUANTITY_SCALE) {
            throw new BusinessException(
                    "Quantità da rettificare non valida.");
        }

        BigDecimal remainingQty = originalQty.subtract(usedQty);

        if (requestedQuantity.compareTo(remainingQty) > 0) {
            throw new BusinessException(
                    "Quantità cumulativa superiore alla fattura.");
        }

        BigDecimal subtotal;
        BigDecimal tax;

        if (requestedQuantity.compareTo(remainingQty) == 0) {
            // Ultima rettifica: recupera tutti i centesimi residui.
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

        if (subtotal.signum() < 0 || tax.signum() < 0
                || total.signum() <= 0
                || subtotal.compareTo(
                        originalSubtotal.subtract(usedSubtotal)) > 0
                || tax.compareTo(
                        originalTax.subtract(usedTax)) > 0) {
            throw new BusinessException(
                    "Importi cumulativi superiori al residuo disponibile.");
        }

        return new SupplierCreditNotePartialCalculator.PartialAmounts(
                requestedQuantity, subtotal, tax, total);
    }
}
