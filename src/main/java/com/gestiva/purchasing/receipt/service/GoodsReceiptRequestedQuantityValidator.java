package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class GoodsReceiptRequestedQuantityValidator {

    private GoodsReceiptRequestedQuantityValidator() {
    }

    public static BigDecimal validate(
            BigDecimal requestedBaseQuantity,
            BigDecimal remainingBaseQuantity) {

        if (requestedBaseQuantity == null
                || remainingBaseQuantity == null) {
            throw new BusinessException(
                    "Quantità richiesta o residua mancante."
            );
        }

        if (requestedBaseQuantity.signum() <= 0) {
            throw new BusinessException(
                    "La quantità da ricevere deve essere maggiore di zero."
            );
        }

        if (remainingBaseQuantity.signum() < 0) {
            throw new BusinessException(
                    "La quantità residua non può essere negativa."
            );
        }

        final BigDecimal requested;
        final BigDecimal remaining;

        try {
            requested = requestedBaseQuantity.setScale(
                    3, RoundingMode.UNNECESSARY
            );

            remaining = remainingBaseQuantity.setScale(
                    3, RoundingMode.UNNECESSARY
            );

        } catch (ArithmeticException ex) {
            throw new BusinessException(
                    "Le quantità devono avere al massimo tre decimali."
            );
        }

        if (requested.precision() - requested.scale() > 12
                || remaining.precision() - remaining.scale() > 12) {
            throw new BusinessException(
                    "La quantità supera la capacità del magazzino."
            );
        }

        if (requested.compareTo(remaining) > 0) {
            throw new BusinessException(
                    "La quantità richiesta supera quella ancora ricevibile."
            );
        }

        return requested;
    }
}
