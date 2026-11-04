package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class SupplierInvoicePartialAmountAllocator {

    private SupplierInvoicePartialAmountAllocator() {
    }

    public record Amounts(
            BigDecimal subtotal,
            BigDecimal tax,
            BigDecimal total
    ) {
    }

    public static Amounts allocate(
            BigDecimal orderedBaseQuantity,
            BigDecimal alreadyReceivedBaseQuantity,
            BigDecimal requestedBaseQuantity,
            BigDecimal orderSubtotal,
            BigDecimal orderTax,
            BigDecimal orderTotal) {

        if (orderedBaseQuantity == null
                || alreadyReceivedBaseQuantity == null
                || requestedBaseQuantity == null
                || orderSubtotal == null
                || orderTax == null
                || orderTotal == null) {
            throw new BusinessException(
                    "Dati economici della fattura incompleti."
            );
        }

        final BigDecimal ordered;
        final BigDecimal already;
        final BigDecimal requested;
        final BigDecimal subtotal;
        final BigDecimal tax;
        final BigDecimal total;

        try {
            ordered = orderedBaseQuantity.setScale(
                    3, RoundingMode.UNNECESSARY
            );
            already = alreadyReceivedBaseQuantity.setScale(
                    3, RoundingMode.UNNECESSARY
            );
            requested = requestedBaseQuantity.setScale(
                    3, RoundingMode.UNNECESSARY
            );
            subtotal = orderSubtotal.setScale(
                    2, RoundingMode.UNNECESSARY
            );
            tax = orderTax.setScale(
                    2, RoundingMode.UNNECESSARY
            );
            total = orderTotal.setScale(
                    2, RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException ex) {
            throw new BusinessException(
                    "Precisione economica o delle quantità non valida."
            );
        }

        if (ordered.signum() <= 0
                || already.signum() < 0
                || requested.signum() <= 0
                || subtotal.signum() <= 0
                || tax.signum() < 0
                || total.signum() <= 0) {
            throw new BusinessException(
                    "Quantità o importi della fattura non validi."
            );
        }

        if (ordered.precision() - ordered.scale() > 12
                || already.precision() - already.scale() > 12
                || requested.precision() - requested.scale() > 12
                || subtotal.precision() - subtotal.scale() > 13
                || tax.precision() - tax.scale() > 13
                || total.precision() - total.scale() > 13) {
            throw new BusinessException(
                    "Quantità o importi oltre la capacità prevista."
            );
        }

        if (subtotal.add(tax).compareTo(total) != 0) {
            throw new BusinessException(
                    "Gli importi originali dell'ordine non quadrano."
            );
        }

        BigDecimal cumulative = already.add(requested);

        if (cumulative.compareTo(ordered) > 0) {
            throw new BusinessException(
                    "La quantità fatturabile supera quella ordinata."
            );
        }

        BigDecimal allocatedSubtotal = cumulativeShare(
                subtotal, cumulative, ordered
        ).subtract(
                cumulativeShare(subtotal, already, ordered)
        );

        BigDecimal allocatedTax = cumulativeShare(
                tax, cumulative, ordered
        ).subtract(
                cumulativeShare(tax, already, ordered)
        );

        BigDecimal allocatedTotal =
                allocatedSubtotal.add(allocatedTax);

        return new Amounts(
                allocatedSubtotal,
                allocatedTax,
                allocatedTotal
        );
    }

    private static BigDecimal cumulativeShare(
            BigDecimal amount,
            BigDecimal cumulativeQuantity,
            BigDecimal orderedQuantity) {

        return amount.multiply(cumulativeQuantity)
                .divide(orderedQuantity, 2, RoundingMode.HALF_UP);
    }
}
