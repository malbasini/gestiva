package com.gestiva.purchasing.receipt.web;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptInvoiceButtonsTemplateTest {

    private static final Path TEMPLATE = Path.of(
            "src/main/resources/templates/purchasing/receipt/"
                    + "goods-receipt-detail.html"
    );

    private String html() throws Exception {
        return Files.readString(TEMPLATE);
    }

    private int count(String text, String value) {
        return text.split(
                java.util.regex.Pattern.quote(value), -1
        ).length - 1;
    }

    @Test
    void traditionalButtonPreserved() throws Exception {
        String html = html();

        assertEquals(1, count(html,
                "th:if=\"${goodsReceipt.canCreateSupplierInvoice}\""));

        assertEquals(1, count(html,
                "data-confirm-action=@{/goods-receipts/{id}/invoice(id=${goodsReceipt.id})}"));

        assertEquals(1, count(html,
                "<span>Genera fattura</span>"));
    }

    @Test
    void partialButtonHasIndependentCondition()
            throws Exception {

        String html = html();

        assertEquals(1, count(html,
                "th:if=\"${goodsReceipt.canCreatePartialSupplierInvoice}\""));

        assertEquals(1, count(html,
                "<span>Genera fattura parziale</span>"));
    }

    @Test
    void partialButtonUsesDedicatedEndpoint()
            throws Exception {

        String html = html();

        assertEquals(1, count(html,
                "data-confirm-action=@{/goods-receipts/{id}/invoice-partial(id=${goodsReceipt.id})}"));
    }

    @Test
    void buttonsReuseSharedModal() throws Exception {
        String html = html();

        assertEquals(2, count(
                html, "onclick=\"openConfirmModal(this)\""));

        assertEquals(2, count(
                html, "data-confirm-action="));

        assertTrue(html.contains(
                "fragments/layout :: modalFragment"));
    }

    @Test
    void existingInvoiceLinkPreserved()
            throws Exception {

        String html = html();

        assertTrue(html.contains(
                "th:if=\"${goodsReceipt.hasSupplierInvoice}\""));

        assertTrue(html.contains(
                "@{/supplier-invoices/{id}(id=${goodsReceipt.supplierInvoiceId})}"));
    }
}
