package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class GoodsReceiptQuantityCalculator {

    private GoodsReceiptQuantityCalculator() {
    }

    public static BigDecimal remainingBaseQuantity(
            BigDecimal commercialQuantity,
            BigDecimal conversionFactor,
            BigDecimal alreadyReceivedBaseQuantity) {

        if (commercialQuantity == null
                || conversionFactor == null
                || alreadyReceivedBaseQuantity == null) {
            throw new BusinessException(
                    "Dati delle quantità di ricevimento incompleti."
            );
        }

        if (commercialQuantity.signum() <= 0
                || conversionFactor.signum() <= 0
                || alreadyReceivedBaseQuantity.signum() < 0) {
            throw new BusinessException(
                    "Quantità o fattore di conversione non validi."
            );
        }

        final BigDecimal orderedBaseQuantity;
        final BigDecimal receivedBaseQuantity;

        try {
            orderedBaseQuantity = commercialQuantity
                    .multiply(conversionFactor)
                    .setScale(3, RoundingMode.UNNECESSARY);

            receivedBaseQuantity = alreadyReceivedBaseQuantity
                    .setScale(3, RoundingMode.UNNECESSARY);

        } catch (ArithmeticException ex) {
            throw new BusinessException(
                    "Le quantità non sono rappresentabili "
                    + "con tre decimali."
            );
        }

        if (orderedBaseQuantity.precision()
                - orderedBaseQuantity.scale() > 12
                || receivedBaseQuantity.precision()
                - receivedBaseQuantity.scale() > 12) {
            throw new BusinessException(
                    "La quantità supera la capacità del magazzino."
            );
        }

        BigDecimal remaining = orderedBaseQuantity.subtract(
                receivedBaseQuantity
        );

        if (remaining.signum() < 0) {
            throw new BusinessException(
                    "La quantità già ricevuta supera quella ordinata."
            );
        }

        return remaining;
    }
}
