package com.gestiva.purchasing.receipt.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class GoodsReceiptPartialFifoLineCalculator {

    private GoodsReceiptPartialFifoLineCalculator() {
    }

    public record PlannedLine(
            BigDecimal quantityReceived,
            BigDecimal unitCost,
            BigDecimal totalCost
    ) {
    }

    /**
     * Prepara i valori economici di una riga parziale.
     *
     * Quantità ricevuta in UOM base.
     * Costi con quattro decimali.
     *
     * Non accede al database e non registra movimenti.
     */
    public static PlannedLine calculate(
            BigDecimal orderedBaseQuantity,
            BigDecimal alreadyReceivedBaseQuantity,
            BigDecimal requestedBaseQuantity,
            BigDecimal orderNetSubtotal) {

        BigDecimal totalCost =
                GoodsReceiptPartialCostAllocator.allocate(
                        orderedBaseQuantity,
                        alreadyReceivedBaseQuantity,
                        requestedBaseQuantity,
                        orderNetSubtotal
                );

        BigDecimal unitCost =
                GoodsReceiptFifoCostReconciler.reconcile(
                        requestedBaseQuantity,
                        totalCost
                );

        BigDecimal quantity = requestedBaseQuantity.setScale(
                3,
                RoundingMode.UNNECESSARY
        );

        return new PlannedLine(
                quantity,
                unitCost,
                totalCost
        );
    }
}
