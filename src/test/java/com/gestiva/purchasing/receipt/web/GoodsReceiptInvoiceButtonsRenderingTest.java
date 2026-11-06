package com.gestiva.purchasing.receipt.web;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptInvoiceButtonsRenderingTest {

    private static final String TRADITIONAL_LABEL =
            "<span>Genera fattura</span>";

    private static final String PARTIAL_LABEL =
            "<span>Genera fattura parziale</span>";

    private static final String TRADITIONAL_ACTION =
            "data-confirm-action=\"/goods-receipts/302/invoice\"";

    private static final String PARTIAL_ACTION =
            "data-confirm-action=\"/goods-receipts/302/invoice-partial\"";

    private GoodsReceiptDetailView receipt() {
        GoodsReceiptDetailView view =
                new GoodsReceiptDetailView();

        view.setId(302L);
        view.setReceiptNumber("GR-00302");
        view.setFormattedReceiptDate("06/11/2026");
        view.setSupplierName("Fornitore Test");
        view.setPurchaseOrderId(100L);
        view.setPurchaseOrderNumber("PO-00100");
        view.setLines(List.of());

        return view;
    }

    private String render(GoodsReceiptDetailView receipt) {

        StaticApplicationContext applicationContext =
                new StaticApplicationContext();

        applicationContext.refresh();

        try {
            SpringResourceTemplateResolver resolver =
                    new SpringResourceTemplateResolver();

            resolver.setApplicationContext(applicationContext);
            resolver.setPrefix("classpath:/templates/");
            resolver.setSuffix(".html");
            resolver.setTemplateMode("HTML");
            resolver.setCharacterEncoding("UTF-8");

            SpringTemplateEngine engine =
                    new SpringTemplateEngine();

            engine.setTemplateResolver(resolver);

            MockServletContext servletContext =
                    new MockServletContext();

            MockHttpServletRequest request =
                    new MockHttpServletRequest(
                            servletContext,
                            "GET",
                            "/goods-receipts/302"
                    );

            MockHttpServletResponse response =
                    new MockHttpServletResponse();

            JakartaServletWebApplication webApplication =
                    JakartaServletWebApplication.buildApplication(
                            servletContext
                    );

            WebContext context = new WebContext(
                    webApplication.buildExchange(
                            request,
                            response
                    )
            );

            context.setVariable("goodsReceipt", receipt);
            context.setVariable("activeMenu", "goodsReceipts");
            context.setVariable("tenantSubscriptionActive", false);
            context.setVariable("tenantSubscriptionPlan", "FREE");

            context.setVariable(
                    "_csrf",
                    new DefaultCsrfToken(
                            "X-CSRF-TOKEN",
                            "_csrf",
                            "test-rendering-token"
                    )
            );

            return engine.process(
                    "purchasing/receipt/goods-receipt-detail",
                    context
            );

        } finally {
            applicationContext.close();
        }
    }

    @Test
    void completeReceiptRendersOnlyTraditionalButton() {

        GoodsReceiptDetailView view = receipt();
        view.setCanCreateSupplierInvoice(true);
        view.setCanCreateTraditionalSupplierInvoice(true);

        String html = render(view);

        assertTrue(html.contains(TRADITIONAL_LABEL));
        assertTrue(html.contains(TRADITIONAL_ACTION));

        assertFalse(html.contains(PARTIAL_LABEL));
        assertFalse(html.contains(PARTIAL_ACTION));
    }

    @Test
    void partialReceiptRendersOnlyPartialButton() {

        GoodsReceiptDetailView view = receipt();
        view.setCanCreatePartialSupplierInvoice(true);

        String html = render(view);

        assertTrue(html.contains(PARTIAL_LABEL));
        assertTrue(html.contains(PARTIAL_ACTION));

        assertFalse(html.contains(TRADITIONAL_LABEL));
        assertFalse(html.contains(TRADITIONAL_ACTION));
    }

    @Test
    void disabledActionsRenderNoGenerationButtons() {

        String html = render(receipt());

        assertFalse(html.contains(TRADITIONAL_LABEL));
        assertFalse(html.contains(PARTIAL_LABEL));
        assertFalse(html.contains(TRADITIONAL_ACTION));
        assertFalse(html.contains(PARTIAL_ACTION));
    }

    @Test
    void existingInvoiceRendersLinkWithoutGeneration() {

        GoodsReceiptDetailView view = receipt();

        view.setHasSupplierInvoice(true);
        view.setSupplierInvoiceId(501L);
        view.setSupplierInvoiceNumber("SI-00501");

        String html = render(view);

        assertTrue(html.contains(
                "href=\"/supplier-invoices/501\""
        ));
        assertTrue(html.contains("SI-00501"));

        assertFalse(html.contains(TRADITIONAL_LABEL));
        assertFalse(html.contains(PARTIAL_LABEL));
        assertFalse(html.contains(TRADITIONAL_ACTION));
        assertFalse(html.contains(PARTIAL_ACTION));
    }

    @Test
    void sharedModalRendersPostFormWithCsrf() {

        GoodsReceiptDetailView view = receipt();
        view.setCanCreatePartialSupplierInvoice(true);

        String html = render(view);

        assertTrue(html.contains(
                "id=\"confirmActionModalForm\""
        ));

        assertTrue(html.contains(
                "method=\"post\""
        ));

        assertTrue(html.contains(
                "name=\"_csrf\""
        ));

        assertTrue(html.contains(
                "value=\"test-rendering-token\""
        ));

        assertTrue(html.contains(
                "id=\"confirmActionModalSubmit\""
        ));

        assertTrue(html.contains(PARTIAL_ACTION));
    }
}
