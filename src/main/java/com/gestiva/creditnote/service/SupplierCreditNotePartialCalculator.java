package com.gestiva.creditnote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.entity.SupplierInvoiceLine;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class SupplierCreditNotePartialCalculator {

    private static final int MONEY_SCALE = 2;
    private static final int QUANTITY_SCALE = 3;

    private SupplierCreditNotePartialCalculator() {
    }

    public record PartialAmounts(
            BigDecimal quantity,
            BigDecimal subtotal,
            BigDecimal tax,
            BigDecimal total) {
    }

    public static PartialAmounts calculate(
            SupplierInvoiceLine original,
            BigDecimal quantityToCredit) {

        if (original == null || quantityToCredit == null) {
            throw new BusinessException(
                    "Riga o quantità di rettifica mancante.");
        }

        BigDecimal originalQuantity = original.getQuantity();
        BigDecimal originalSubtotal = original.getLineSubtotal();
        BigDecimal originalTax = original.getTaxAmount();
        BigDecimal originalTotal = original.getLineTotal();

        if (originalQuantity == null
                || originalQuantity.signum() <= 0
                || originalSubtotal == null
                || originalTax == null
                || originalTotal == null
                || originalSubtotal.signum() < 0
                || originalTax.signum() < 0
                || originalTotal.signum() < 0
                || originalSubtotal.add(originalTax)
                    .compareTo(originalTotal) != 0) {
            throw new BusinessException(
                    "Importi o quantità della riga originale non validi.");
        }

        if (originalQuantity.scale() > QUANTITY_SCALE
                || quantityToCredit.scale() > QUANTITY_SCALE
                || quantityToCredit.signum() <= 0
                || quantityToCredit.compareTo(originalQuantity) > 0) {
            throw new BusinessException(
                    "Quantità da rettificare non valida.");
        }

        if (originalTotal.signum() == 0) {
            throw new BusinessException(
                    "Riga originale con totale nullo non rettificabile.");
        }

        if (quantityToCredit.compareTo(originalQuantity) == 0) {
            return new PartialAmounts(
                    quantityToCredit,
                    originalSubtotal,
                    originalTax,
                    originalTotal);
        }

        BigDecimal ratio = quantityToCredit.divide(
                originalQuantity, 16, RoundingMode.HALF_UP);

        BigDecimal subtotal = originalSubtotal.multiply(ratio)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        BigDecimal tax = originalTax.multiply(ratio)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        BigDecimal total = subtotal.add(tax);

        if (total.signum() <= 0
                || subtotal.compareTo(originalSubtotal) > 0
                || tax.compareTo(originalTax) > 0
                || total.compareTo(originalTotal) > 0) {
            throw new BusinessException(
                    "Rettifica parziale non rappresentabile "
                    + "con precisione monetaria valida.");
        }

        return new PartialAmounts(
                quantityToCredit, subtotal, tax, total);
    }
}
