package com.gestiva.purchasing.order.web;

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

class PurchaseOrderDetailTemplateTest {


    @Test
    void rendersCompleteReceiptForConfirmedOrderWithoutReceipts() {

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

            SpringTemplateEngine engine = new SpringTemplateEngine();
            engine.setTemplateResolver(resolver);

            MockServletContext servletContext = new MockServletContext();
            MockHttpServletRequest request =
                    new MockHttpServletRequest(
                            servletContext, "GET", "/purchase-orders/100"
                    );

            MockHttpServletResponse response =
                    new MockHttpServletResponse();

            JakartaServletWebApplication webApplication =
                    JakartaServletWebApplication.buildApplication(
                            servletContext
                    );

            WebContext context = new WebContext(
                    webApplication.buildExchange(request, response)
            );

            PurchaseOrderDetailView order =
                    new PurchaseOrderDetailView();

            order.setId(100L);
            order.setOrderNumber("PO-TEST-100");
            order.setSupplierName("Fornitore Test");
            order.setStatus("CONFIRMED");
            order.setCurrencyCode("EUR");
            order.setFormattedOrderDate("05/11/2026");
            order.setFormattedExpectedDeliveryDate("-");
            order.setFormattedSubtotalAmount("144,00 €");
            order.setFormattedTaxAmount("31,68 €");
            order.setFormattedTotalAmount("175,68 €");
            order.setCanEdit(false);
            order.setHasGoodsReceipt(false);
            order.setCanReceiveGoods(true);
            order.setCanReceivePartialGoods(false);
            order.setGoodsReceipts(List.of());

            PurchaseOrderDetailLineView line =
                    new PurchaseOrderDetailLineView();

            line.setLineId(200L);
            line.setLineNo(1);
            line.setDescription("Prodotto ordinato in BOX");
            line.setFormattedQuantity("2");
            line.setUnitOfMeasureCode("BOX");
            line.setFormattedOrderedBaseQuantity("20");
            line.setFormattedReceivedBaseQuantity("0");
            line.setFormattedRemainingBaseQuantity("20");
            line.setFormattedUnitPrice("72,00 €");
            line.setFormattedDiscountPct("0%");
            line.setFormattedTaxPct("22%");
            line.setFormattedLineTotal("144,00 €");

            order.setLines(List.of(line));

            context.setVariable("purchaseOrder", order);
            context.setVariable("warehouseOptions", List.of());
            context.setVariable("activeMenu", "purchaseOrders");
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

            String html = engine.process(
                    "purchasing/order/purchase-order-detail",
                    context
            );

            assertTrue(
                    html.contains("id=\"receiveGoodsModal\""),
                    "Modale ricevimento completo assente."
            );

            assertTrue(
                    html.contains("Ricevi merce"),
                    "Pulsante ricevimento completo assente."
            );

            assertTrue(
                    html.contains(
                            "action=\"/purchase-orders/100/receive\""
                    ),
                    "Form ricevimento completo non corretto."
            );

            assertFalse(
                    html.contains("href=\"/goods-receipts/301\""),
                    "Collegamento GR inesistente."
            );

            System.out.println(
                    "THYMELEAF PO: ricevimento completo disponibile"
            );
            System.out.println(
                    "THYMELEAF PO: form /purchase-orders/100/receive"
            );
            System.out.println(
                    "THYMELEAF PO: nessun GR inesistente visualizzato"
            );

        } finally {
            applicationContext.close();
        }
    }

    @Test
    void rendersMultipleReceiptsAndRemainingQuantities() {

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
                            "/purchase-orders/100"
                    );

            MockHttpServletResponse response =
                    new MockHttpServletResponse();

            JakartaServletWebApplication webApplication =
                    JakartaServletWebApplication.buildApplication(
                            servletContext
                    );

            WebContext context = new WebContext(
                    webApplication.buildExchange(request, response)
            );

            PurchaseOrderDetailView order =
                    new PurchaseOrderDetailView();

            order.setId(100L);
            order.setOrderNumber("PO-TEST-100");
            order.setSupplierName("Fornitore Test");
            order.setStatus("CONFIRMED");
            order.setCurrencyCode("EUR");
            order.setFormattedOrderDate("05/11/2026");
            order.setFormattedExpectedDeliveryDate("-");
            order.setFormattedSubtotalAmount("144,00 €");
            order.setFormattedTaxAmount("31,68 €");
            order.setFormattedTotalAmount("175,68 €");
            order.setCanEdit(false);
            order.setHasGoodsReceipt(true);
            order.setCanReceiveGoods(false);
            order.setCanReceivePartialGoods(false);

            order.setGoodsReceipts(List.of(
                    new PurchaseOrderDetailView.GoodsReceiptLink(
                            301L, "GR-001"
                    ),
                    new PurchaseOrderDetailView.GoodsReceiptLink(
                            302L, "GR-002"
                    )
            ));

            PurchaseOrderDetailLineView line =
                    new PurchaseOrderDetailLineView();

            line.setLineId(200L);
            line.setLineNo(1);
            line.setDescription("Prodotto ordinato in BOX");
            line.setFormattedQuantity("2");
            line.setUnitOfMeasureCode("BOX");
            line.setFormattedOrderedBaseQuantity("20");
            line.setFormattedReceivedBaseQuantity("8");
            line.setFormattedRemainingBaseQuantity("12");
            line.setFormattedUnitPrice("72,00 €");
            line.setFormattedDiscountPct("0%");
            line.setFormattedTaxPct("22%");
            line.setFormattedLineTotal("144,00 €");

            order.setLines(List.of(line));

            context.setVariable("purchaseOrder", order);
            context.setVariable("warehouseOptions", List.of());
            context.setVariable("activeMenu", "purchaseOrders");
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

            String html = engine.process(
                    "purchasing/order/purchase-order-detail",
                    context
            );

            assertNotNull(html);

            assertTrue(
                    html.contains("href=\"/goods-receipts/301\""),
                    "Collegamento al primo GR mancante."
            );

            assertTrue(
                    html.contains("href=\"/goods-receipts/302\""),
                    "Collegamento al secondo GR mancante."
            );

            assertTrue(html.contains("Ricezione GR-001"));
            assertTrue(html.contains("Ricezione GR-002"));

            assertTrue(html.contains("Ordinato (base)"));
            assertTrue(html.contains("Ricevuto (base)"));
            assertTrue(html.contains("Residuo (base)"));

            assertTrue(
                    html.matches("(?s).*"
                            + "class=\"text-end\"[^>]*>20</td>.*"),
                    "Quantità ordinata base non renderizzata."
            );

            assertTrue(
                    html.matches("(?s).*"
                            + "class=\"text-end\"[^>]*>8</td>.*"),
                    "Quantità ricevuta base non renderizzata."
            );

            assertTrue(
                    html.matches("(?s).*"
                            + "class=\"text-end\"[^>]*>12</td>.*"),
                    "Quantità residua base non renderizzata."
            );

            assertFalse(
                    html.contains("id=\"receiveGoodsModal\""),
                    "Modale del ricevimento completo ancora visibile."
            );

            assertFalse(
                    html.contains("th:each=\"receipt"),
                    "Il ciclo GR non è stato elaborato da Thymeleaf."
            );

            System.out.println(
                    "THYMELEAF PO: due collegamenti GR renderizzati"
            );
            System.out.println(
                    "THYMELEAF PO: ordinati 20, ricevuti 8, residui 12"
            );
            System.out.println(
                    "THYMELEAF PO: modale ricevimento completo assente"
            );

        } finally {
            applicationContext.close();
        }
    }
}
