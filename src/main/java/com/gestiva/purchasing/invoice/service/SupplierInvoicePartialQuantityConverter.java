package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class SupplierInvoicePartialQuantityConverter {

    private SupplierInvoicePartialQuantityConverter() {
    }

    /**
     * Converte la quantità ricevuta in UOM base nella
     * quantità commerciale da riportare in fattura.
     *
     * Utilizza esclusivamente lo snapshot UOM dell'ordine.
     * Nessun arrotondamento implicito è consentito.
     */
    public static BigDecimal toCommercialQuantity(
            BigDecimal receivedBaseQuantity,
            BigDecimal conversionFactor) {

        if (receivedBaseQuantity == null
                || conversionFactor == null) {
            throw new BusinessException(
                    "Quantità ricevuta o conversione UOM mancante."
            );
        }

        if (receivedBaseQuantity.signum() <= 0
                || conversionFactor.signum() <= 0) {
            throw new BusinessException(
                    "Quantità ricevuta e conversione UOM "
                    + "devono essere positive."
            );
        }

        final BigDecimal baseQuantity;
        final BigDecimal factor;

        try {
            baseQuantity = receivedBaseQuantity.setScale(
                    3, RoundingMode.UNNECESSARY
            );

            factor = conversionFactor.setScale(
                    6, RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException ex) {
            throw new BusinessException(
                    "Precisione della quantità o della "
                    + "conversione UOM non valida."
            );
        }

        if (baseQuantity.precision()
                    - baseQuantity.scale() > 12
                || factor.precision()
                    - factor.scale() > 13) {
            throw new BusinessException(
                    "Quantità o conversione oltre i limiti previsti."
            );
        }

        final BigDecimal commercialQuantity;

        try {
            commercialQuantity = baseQuantity.divide(
                    factor,
                    3,
                    RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException ex) {
            throw new BusinessException(
                    "La quantità ricevuta non è rappresentabile "
                    + "esattamente nell'unità commerciale."
            );
        }

        if (commercialQuantity.signum() <= 0
                || commercialQuantity.precision()
                    - commercialQuantity.scale() > 12) {
            throw new BusinessException(
                    "Quantità commerciale non rappresentabile."
            );
        }

        // Verifica di ritorno: nessuna quantità base
        // può essere introdotta o persa nella conversione.
        BigDecimal reconstructed = commercialQuantity
                .multiply(factor);

        if (reconstructed.compareTo(baseQuantity) != 0) {
            throw new BusinessException(
                    "La conversione UOM non conserva "
                    + "la quantità ricevuta."
            );
        }

        return commercialQuantity;
    }
}
