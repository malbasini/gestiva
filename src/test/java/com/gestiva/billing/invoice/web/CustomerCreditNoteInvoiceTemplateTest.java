
package com.gestiva.billing.invoice.web;

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

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class CustomerCreditNoteInvoiceTemplateTest {

    private static final String FULL_ACTION =
            "action=\"/customer-credit-notes\"";

    private static final String PARTIAL_ACTION =
            "action=\"/customer-credit-notes/partial\"";

    private InvoiceDetailLineView line(
            long id,
            int lineNo,
            String description,
            String quantity) {

        InvoiceDetailLineView value =
                new InvoiceDetailLineView();

        value.setId(id);
        value.setLineNo(lineNo);
        value.setDescription(description);
        value.setQuantity(new BigDecimal(quantity));
        value.setFormattedQuantity(quantity);
        value.setUnitOfMeasure("PZ");
        value.setFormattedUnitPrice("10,00");
        value.setFormattedDiscountPct("0,00");
        value.setFormattedTaxPct("22");
        value.setFormattedLineTotal("122,00");

        return value;
    }

    private InvoiceDetailView invoice() {

        InvoiceDetailView value =
                new InvoiceDetailView();

        value.setId(500L);
        value.setInvoiceNumber("INV-CCN-TEST");
        value.setStatus("ISSUED");
        value.setCustomerName("Cliente test");
        value.setCurrencyCode("EUR");

        value.setLines(List.of(
                line(101L, 1, "Prodotto A", "10.000"),
                line(205L, 2, "Prodotto B", "5.000"),
                line(309L, 3, "Prodotto C", "3.000")
        ));

        return value;
    }

    private String render(boolean canCreate) {

        StaticApplicationContext applicationContext =
                new StaticApplicationContext();

        applicationContext.refresh();

        try {
            SpringResourceTemplateResolver resolver =
                    new SpringResourceTemplateResolver();

            resolver.setApplicationContext(
                    applicationContext);

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
                            "/invoices/500");

            MockHttpServletResponse response =
                    new MockHttpServletResponse();

            var webApplication =
                    JakartaServletWebApplication
                            .buildApplication(servletContext);

            WebContext context =
                    new WebContext(
                            webApplication.buildExchange(
                                    request,
                                    response));

            context.setVariable("invoice", invoice());
            context.setVariable("tenantId", 2L);
            context.setVariable("activeMenu", "invoices");

            context.setVariable(
                    "customerCreditNotes",
                    List.of());

            context.setVariable(
                    "canCreateCustomerCreditNote",
                    canCreate);

            // Variabili richieste dal layout Gestiva.
            context.setVariable(
                    "tenantSubscriptionActive",
                    false);

            context.setVariable(
                    "tenantSubscriptionPlan",
                    "FREE");

            context.setVariable(
                    "_csrf",
                    new DefaultCsrfToken(
                            "X-CSRF-TOKEN",
                            "_csrf",
                            "ccn-test-csrf-token"));

            return engine.process(
                    "invoice/invoice-detail",
                    context);

        } finally {
            applicationContext.close();
        }
    }

    private static List<String> inputsWithName(
            String html,
            String name) {

        Pattern pattern = Pattern.compile(
                "<input\\b[^>]*>",
                Pattern.CASE_INSENSITIVE
                        | Pattern.DOTALL);

        Matcher matcher = pattern.matcher(html);

        List<String> result = new java.util.ArrayList<>();

        while (matcher.find()) {
            String input = matcher.group();

            if (input.contains(
                    "name=\"" + name + "\"")) {
                result.add(input);
            }
        }

        return result;
    }

    @Test
    void authorizedUserSeesBothCreationForms() {

        String html = render(true);

        assertTrue(html.contains(FULL_ACTION));
        assertTrue(html.contains(PARTIAL_ACTION));

        assertTrue(html.contains(
                "Nuova nota di credito completa"));

        assertTrue(html.contains(
                "Nuova nota di credito parziale"));
    }

    @Test
    void partialFormUsesRealInvoiceLineIds() {

        String html = render(true);

        List<String> checkboxes =
                inputsWithName(html, "lineIds");

        assertEquals(3, checkboxes.size());

        assertTrue(checkboxes.get(0)
                .contains("value=\"101\""));

        assertTrue(checkboxes.get(1)
                .contains("value=\"205\""));

        assertTrue(checkboxes.get(2)
                .contains("value=\"309\""));

        System.out.println(
                "CCN_TEMPLATE_REAL_LINE_IDS: OK");
    }

    @Test
    void partialFormUsesNumericQuantityLimits() {

        String html = render(true);

        List<String> quantities =
                inputsWithName(html, "quantities");

        assertEquals(3, quantities.size());

        assertTrue(quantities.get(0)
                .contains("max=\"10.000\""));

        assertTrue(quantities.get(1)
                .contains("max=\"5.000\""));

        assertTrue(quantities.get(2)
                .contains("max=\"3.000\""));

        for (String input : quantities) {
            assertTrue(input.contains(
                    "type=\"number\""));

            assertTrue(input.contains(
                    "step=\"0.001\""));
        }

        System.out.println(
                "CCN_TEMPLATE_NUMERIC_LIMITS: OK");
    }

    @Test
    void quantityInputsAreInitiallyDisabled() {

        String html = render(true);

        List<String> quantities =
                inputsWithName(html, "quantities");

        assertEquals(3, quantities.size());

        for (String input : quantities) {
            assertTrue(
                    Pattern.compile(
                                    "\\bdisabled(?:\\s|=|/|>)",
                                    Pattern.CASE_INSENSITIVE)
                            .matcher(input)
                            .find(),
                    "Campo quantità non disabilitato: "
                            + input);
        }

        System.out.println(
                "CCN_TEMPLATE_QUANTITIES_DISABLED: OK");
    }

    @Test
    void unauthorizedCreationHidesBothForms() {

        String html = render(false);

        assertFalse(html.contains(FULL_ACTION));
        assertFalse(html.contains(PARTIAL_ACTION));

        assertTrue(
                inputsWithName(html, "lineIds")
                        .isEmpty());

        assertTrue(
                inputsWithName(html, "quantities")
                        .isEmpty());

        // La consultazione delle note rimane visibile.
        assertTrue(html.contains(
                "Note di credito cliente"));

        System.out.println(
                "CCN_TEMPLATE_UNAUTHORIZED_HIDDEN: OK");
    }
}
