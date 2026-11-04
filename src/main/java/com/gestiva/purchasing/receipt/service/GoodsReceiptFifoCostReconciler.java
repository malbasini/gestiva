package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class GoodsReceiptFifoCostReconciler {

    private GoodsReceiptFifoCostReconciler() {
    }

    /**
     * Determina il costo unitario rappresentabile dal FIFO
     * senza differenze rispetto al costo del ricevimento.
     *
     * Non crea documenti e non registra movimenti.
     */
    public static BigDecimal reconcile(
            BigDecimal receivedBaseQuantity,
            BigDecimal allocatedTotalCost) {

        if (receivedBaseQuantity == null
                || allocatedTotalCost == null) {
            throw new BusinessException(
                    "Quantità o costo del ricevimento mancanti."
            );
        }

        final BigDecimal quantity;
        final BigDecimal totalCost;

        try {
            quantity = receivedBaseQuantity.setScale(
                    3, RoundingMode.UNNECESSARY
            );

            totalCost = allocatedTotalCost.setScale(
                    4, RoundingMode.UNNECESSARY
            );

        } catch (ArithmeticException ex) {
            throw new BusinessException(
                    "Precisione della quantità o del costo non valida."
            );
        }

        if (quantity.signum() <= 0
                || totalCost.signum() <= 0) {
            throw new BusinessException(
                    "Quantità e costo devono essere positivi."
            );
        }

        if (quantity.precision() - quantity.scale() > 12
                || totalCost.precision() - totalCost.scale() > 11) {
            throw new BusinessException(
                    "Quantità o costo oltre la capacità prevista."
            );
        }

        BigDecimal unitCost = totalCost.divide(
                quantity, 4, RoundingMode.HALF_UP
        );

        if (unitCost.signum() <= 0
                || unitCost.precision() - unitCost.scale() > 11) {
            throw new BusinessException(
                    "Costo unitario FIFO non rappresentabile."
            );
        }

        BigDecimal fifoTotal = quantity.multiply(unitCost)
                .setScale(4, RoundingMode.HALF_UP);

        if (fifoTotal.compareTo(totalCost) != 0) {
            throw new BusinessException(
                    "Il costo del ricevimento non è "
                    + "riconciliabile con il costo unitario FIFO "
                    + "a quattro decimali."
            );
        }

        return unitCost;
    }
}
