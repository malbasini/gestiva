
package com.gestiva.creditnote.controller;

import com.gestiva.accounting.due.entity.PaymentDue;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteFinancialPosition;

import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SupplierCreditNoteCompensationTemplateTest {

    @Test
    void compensationTemplateRendersWithEligibleDue() {

        ClassLoaderTemplateResolver resolver =
                new ClassLoaderTemplateResolver();

        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        CreditNote note = new CreditNote();
        note.setId(10L);
        note.setCreditNoteNumber("SCN-TEST");
        note.setStatus("REGISTERED");
        note.setCurrencyCode("EUR");
        note.setTotalAmount(new BigDecimal("63.24"));

        CreditNoteFinancialPosition position =
                new CreditNoteFinancialPosition();
        position.setStatus("OPEN");
        position.setAvailableAmount(new BigDecimal("63.24"));

        PaymentDue due = new PaymentDue();
        due.setId(7L);
        due.setDocumentNumber("SI-TEST");
        due.setCurrencyCode("EUR");
        due.setOpenAmount(new BigDecimal("25.00"));


        // PASSO 5.13.2393G
        // Contesto web simulato per i link relativi
        // e gli attributi del template Thymeleaf.

        MockServletContext servletContext =
                new MockServletContext();

        MockHttpServletRequest request =
                new MockHttpServletRequest(servletContext);

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.setContextPath("");

        var application =
                JakartaServletWebApplication.buildApplication(
                        servletContext
                );

        var exchange = application.buildExchange(
                request,
                response
        );

        WebContext context = new WebContext(exchange);

        context.setVariable(
                "_csrf",
                new org.springframework.security.web.csrf.DefaultCsrfToken(
                        "X-CSRF-TOKEN",
                        "_csrf",
                        "test-csrf-token"
                )
        );

        context.setVariable("creditNote", note);
        context.setVariable("creditNoteLines", List.of());
        context.setVariable("canRegisterCreditNote", false);
        context.setVariable("canCompensateCreditNote", true);
        context.setVariable("compensationPosition", position);
        context.setVariable("compensableDues", List.of(due));
        context.setVariable("compensationOperationKey", "op-test-123");
        context.setVariable("compensationDate", LocalDate.of(2026, 11, 12));
        // Attributi del layout generale Gestiva.
        // Valori simulati esclusivamente nel test.
        context.setVariable(
                "tenantSubscriptionActive",
                false
        );

        context.setVariable(
                "tenantSubscriptionPlan",
                "STARTER"
        );
        // PASSO 5.13.2393I
        // Rendering mirato del modulo compensazioni V10.
        // Non carica il layout generale del tenant.

        String html = engine.process(
                "creditnote/supplier-credit-note-detail",
                context
        );


        assertTrue(html.contains("Compensazione fattura fornitore"));
        assertTrue(html.contains("SI-TEST"));
        assertTrue(html.contains("op-test-123"));
    }
}
