package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class GoodsReceiptPartialCostAllocator {

    private GoodsReceiptPartialCostAllocator() {
    }

    public static BigDecimal allocate(
            BigDecimal orderedBaseQuantity,
            BigDecimal alreadyReceivedBaseQuantity,
            BigDecimal requestedBaseQuantity,
            BigDecimal orderNetSubtotal) {

        if (orderedBaseQuantity == null
                || alreadyReceivedBaseQuantity == null
                || requestedBaseQuantity == null
                || orderNetSubtotal == null) {
            throw new BusinessException(
                    "Dati economici del ricevimento incompleti."
            );
        }

        final BigDecimal ordered;
        final BigDecimal already;
        final BigDecimal requested;
        final BigDecimal netSubtotal;

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
            netSubtotal = orderNetSubtotal.setScale(
                    4, RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException ex) {
            throw new BusinessException(
                    "Precisione delle quantità o del costo non valida."
            );
        }

        if (ordered.signum() <= 0
                || already.signum() < 0
                || requested.signum() <= 0
                || netSubtotal.signum() <= 0) {
            throw new BusinessException(
                    "Quantità o costo netto non validi."
            );
        }

        if (ordered.precision() - ordered.scale() > 12
                || already.precision() - already.scale() > 12
                || requested.precision() - requested.scale() > 12
                || netSubtotal.precision() - netSubtotal.scale() > 11) {
            throw new BusinessException(
                    "Quantità o costo oltre la capacità prevista."
            );
        }

        BigDecimal cumulative = already.add(requested);

        if (cumulative.compareTo(ordered) > 0) {
            throw new BusinessException(
                    "Il ricevimento supera la quantità ordinata."
            );
        }

        BigDecimal previousCost = netSubtotal
                .multiply(already)
                .divide(ordered, 4, RoundingMode.HALF_UP);

        BigDecimal cumulativeCost = netSubtotal
                .multiply(cumulative)
                .divide(ordered, 4, RoundingMode.HALF_UP);

        return cumulativeCost.subtract(previousCost)
                .setScale(4, RoundingMode.UNNECESSARY);
    }
}
