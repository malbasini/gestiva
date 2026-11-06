package com.gestiva.purchasing.receipt.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptDetailInvoiceActionsTest {

    @Test
    void allInvoiceActionsAreDisabledByDefault() {

        GoodsReceiptDetailView view =
                new GoodsReceiptDetailView();

        assertFalse(view.isCanCreateSupplierInvoice());
        assertFalse(view.isCanCreateTraditionalSupplierInvoice());
        assertFalse(view.isCanCreatePartialSupplierInvoice());
    }

    @Test
    void traditionalActionIsIndependent() {

        GoodsReceiptDetailView view =
                new GoodsReceiptDetailView();

        view.setCanCreateTraditionalSupplierInvoice(true);

        assertTrue(view.isCanCreateTraditionalSupplierInvoice());
        assertFalse(view.isCanCreatePartialSupplierInvoice());
        assertFalse(view.isCanCreateSupplierInvoice());
    }

    @Test
    void partialActionIsIndependent() {

        GoodsReceiptDetailView view =
                new GoodsReceiptDetailView();

        view.setCanCreatePartialSupplierInvoice(true);

        assertTrue(view.isCanCreatePartialSupplierInvoice());
        assertFalse(view.isCanCreateTraditionalSupplierInvoice());
        assertFalse(view.isCanCreateSupplierInvoice());
    }

    @Test
    void legacyActionRemainsCompatible() {

        GoodsReceiptDetailView view =
                new GoodsReceiptDetailView();

        view.setCanCreateSupplierInvoice(true);

        assertTrue(view.isCanCreateSupplierInvoice());
        assertFalse(view.isCanCreateTraditionalSupplierInvoice());
        assertFalse(view.isCanCreatePartialSupplierInvoice());
    }

    @Test
    void independentFlagsCanBeReset() {

        GoodsReceiptDetailView view =
                new GoodsReceiptDetailView();

        view.setCanCreateTraditionalSupplierInvoice(true);
        view.setCanCreatePartialSupplierInvoice(true);

        view.setCanCreateTraditionalSupplierInvoice(false);
        view.setCanCreatePartialSupplierInvoice(false);

        assertFalse(view.isCanCreateTraditionalSupplierInvoice());
        assertFalse(view.isCanCreatePartialSupplierInvoice());
    }
}
