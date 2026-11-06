package com.gestiva.purchasing.receipt.web;

import static com.gestiva.purchasing.receipt.web
        .GoodsReceiptInvoiceQuantityClassifier.Result;

/**
 * Regole di presentazione delle azioni di fatturazione.
 *
 * Non costituisce autorizzazione applicativa:
 * SecurityConfig, feature gate e servizi transazionali
 * mantengono tutte le verifiche definitive.
 */
public final class GoodsReceiptInvoiceActionPolicy {

    public record Actions(
            boolean traditional,
            boolean partial
    ) {
    }

    private GoodsReceiptInvoiceActionPolicy() {
    }

    public static Actions evaluate(
            Result quantityClassification,
            boolean confirmedOrder,
            boolean invoiceAlreadyExists,
            boolean authorizedRole,
            boolean testProfile,
            boolean generalInvoiceEnabled,
            boolean partialInvoiceEnabled) {

        if (quantityClassification == null
                || !confirmedOrder
                || invoiceAlreadyExists
                || !authorizedRole
                || !testProfile
                || !generalInvoiceEnabled) {

            return new Actions(false, false);
        }

        if (quantityClassification == Result.COMPLETE) {
            return new Actions(true, false);
        }

        if (quantityClassification == Result.PARTIAL
                && partialInvoiceEnabled) {

            return new Actions(false, true);
        }

        return new Actions(false, false);
    }
}
