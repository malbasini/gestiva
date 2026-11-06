package com.gestiva.purchasing.receipt.web;

import org.junit.jupiter.api.Test;

import static com.gestiva.purchasing.receipt.web
        .GoodsReceiptInvoiceQuantityClassifier.Result.*;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptInvoiceActionPolicyTest {

    private GoodsReceiptInvoiceActionPolicy.Actions evaluate(
            GoodsReceiptInvoiceQuantityClassifier.Result classification,
            boolean confirmed,
            boolean invoiced,
            boolean authorized,
            boolean test,
            boolean general,
            boolean partial) {

        return GoodsReceiptInvoiceActionPolicy.evaluate(
                classification,
                confirmed,
                invoiced,
                authorized,
                test,
                general,
                partial
        );
    }

    @Test
    void completeReceiptUsesTraditionalAction() {

        var result = evaluate(
                COMPLETE, true, false,
                true, true, true, true
        );

        assertTrue(result.traditional());
        assertFalse(result.partial());
    }

    @Test
    void partialReceiptUsesPartialAction() {

        var result = evaluate(
                PARTIAL, true, false,
                true, true, true, true
        );

        assertFalse(result.traditional());
        assertTrue(result.partial());
    }

    @Test
    void partialReceiptWithoutPartialFlagHasNoAction() {

        var result = evaluate(
                PARTIAL, true, false,
                true, true, true, false
        );

        assertFalse(result.traditional());
        assertFalse(result.partial());
    }

    @Test
    void invalidReceiptHasNoAction() {

        var result = evaluate(
                INVALID, true, false,
                true, true, true, true
        );

        assertFalse(result.traditional());
        assertFalse(result.partial());
    }

    @Test
    void nullClassificationHasNoAction() {

        var result = evaluate(
                null, true, false,
                true, true, true, true
        );

        assertFalse(result.traditional());
        assertFalse(result.partial());
    }

    @Test
    void alreadyInvoicedReceiptHasNoAction() {

        var result = evaluate(
                COMPLETE, true, true,
                true, true, true, true
        );

        assertFalse(result.traditional());
        assertFalse(result.partial());
    }

    @Test
    void unconfirmedOrderHasNoAction() {

        var result = evaluate(
                PARTIAL, false, false,
                true, true, true, true
        );

        assertFalse(result.traditional());
        assertFalse(result.partial());
    }

    @Test
    void unauthorizedRoleHasNoAction() {

        var result = evaluate(
                PARTIAL, true, false,
                false, true, true, true
        );

        assertFalse(result.traditional());
        assertFalse(result.partial());
    }

    @Test
    void nonTestProfileHasNoActionEvenWithFlags() {

        var result = evaluate(
                PARTIAL, true, false,
                true, false, true, true
        );

        assertFalse(result.traditional());
        assertFalse(result.partial());
    }

    @Test
    void disabledGeneralFlagBlocksBothActions() {

        var traditional = evaluate(
                COMPLETE, true, false,
                true, true, false, true
        );

        var partial = evaluate(
                PARTIAL, true, false,
                true, true, false, true
        );

        assertFalse(traditional.traditional());
        assertFalse(traditional.partial());
        assertFalse(partial.traditional());
        assertFalse(partial.partial());
    }

    @Test
    void traditionalActionDoesNotRequirePartialFlag() {

        var result = evaluate(
                COMPLETE, true, false,
                true, true, true, false
        );

        assertTrue(result.traditional());
        assertFalse(result.partial());
    }

    @Test
    void noScenarioShowsBothActions() {

        for (var classification :
                GoodsReceiptInvoiceQuantityClassifier.Result.values()) {

            for (boolean partialEnabled :
                    new boolean[]{false, true}) {

                var result = evaluate(
                        classification,
                        true,
                        false,
                        true,
                        true,
                        true,
                        partialEnabled
                );

                assertFalse(
                        result.traditional() && result.partial(),
                        "Le due azioni non devono comparire insieme."
                );
            }
        }
    }
}
