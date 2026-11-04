package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;

import java.math.BigDecimal;

public final class SupplierInvoicePartialBillabilityValidator {

    private SupplierInvoicePartialBillabilityValidator() {
    }

    public record BillableLine(
            BigDecimal commercialQuantity,
            BigDecimal subtotal,
            BigDecimal tax,
            BigDecimal total
    ) {
    }

    /**
     * Verifica quantità commerciale e importi per una
     * singola riga di fattura derivata da ricevimento.
     *
     * Non verifica l'esistenza di fatture precedenti:
     * tale controllo spetta al workflow transazionale.
     */
    public static BillableLine validate(
            BigDecimal orderedBaseQuantity,
            BigDecimal alreadyReceivedBaseQuantity,
            BigDecimal receivedBaseQuantity,
            BigDecimal conversionFactor,
            BigDecimal orderSubtotal,
            BigDecimal orderTax,
            BigDecimal orderTotal) {

        BigDecimal commercialQuantity =
                SupplierInvoicePartialQuantityConverter
                        .toCommercialQuantity(
                                receivedBaseQuantity,
                                conversionFactor
                        );

        var amounts =
                SupplierInvoicePartialAmountAllocator.allocate(
                        orderedBaseQuantity,
                        alreadyReceivedBaseQuantity,
                        receivedBaseQuantity,
                        orderSubtotal,
                        orderTax,
                        orderTotal
                );

        if (amounts.subtotal().signum() <= 0
                || amounts.total().signum() <= 0) {
            throw new BusinessException(
                    "Il ricevimento genera importi "
                    + "non fatturabili con le regole attuali."
            );
        }

        if (amounts.tax().signum() < 0
                || amounts.subtotal()
                        .add(amounts.tax())
                        .compareTo(amounts.total()) != 0) {
            throw new BusinessException(
                    "Imponibile, IVA e totale non quadrano."
            );
        }

        return new BillableLine(
                commercialQuantity,
                amounts.subtotal(),
                amounts.tax(),
                amounts.total()
        );
    }
}
