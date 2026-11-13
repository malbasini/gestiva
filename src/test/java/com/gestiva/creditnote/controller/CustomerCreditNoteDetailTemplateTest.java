
package com.gestiva.creditnote.controller;

import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteLine;

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
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerCreditNoteDetailTemplateTest {

    private static final String TEMPLATE =
            "creditnote/customer-credit-note-detail";

    private CreditNote note(String status) {

        CreditNote note = mock(CreditNote.class);

        when(note.getId()).thenReturn(50L);
        when(note.getCreditNoteNumber())
                .thenReturn("CCN-TEST-00050");
        when(note.getCreditNoteDate())
                .thenReturn(LocalDate.of(2026, 11, 13));
        when(note.getStatus()).thenReturn(status);
        when(note.getOriginalInvoiceId())
                .thenReturn(500L);
        when(note.getCurrencyCode()).thenReturn("EUR");
        when(note.getReason())
                .thenReturn("Rettifica fattura cliente");
        when(note.getSubtotalAmount())
                .thenReturn(new BigDecimal("100.00"));
        when(note.getTaxAmount())
                .thenReturn(new BigDecimal("22.00"));
        when(note.getTotalAmount())
                .thenReturn(new BigDecimal("122.00"));

        return note;
    }

    private CreditNoteLine line() {

        CreditNoteLine line = mock(CreditNoteLine.class);

        when(line.getLineNo()).thenReturn(1);
        when(line.getDescription())
                .thenReturn("Prodotto test CCN");
        when(line.getQuantity())
                .thenReturn(new BigDecimal("2.000"));
        when(line.getUomCode()).thenReturn("PZ");
        when(line.getUnitPrice())
                .thenReturn(new BigDecimal("50.00"));
        when(line.getDiscountPct())
                .thenReturn(BigDecimal.ZERO);
        when(line.getTaxPct())
                .thenReturn(new BigDecimal("22.00"));
        when(line.getLineTotal())
                .thenReturn(new BigDecimal("100.00"));

        return line;
    }


    private String render(
            CreditNote note,
            List<CreditNoteLine> lines) {

        return render(note, lines, false);
    }

    private String render(
            CreditNote note,
            List<CreditNoteLine> lines,
            boolean canRegister) {


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
                            "/customer-credit-notes/50");

            MockHttpServletResponse response =
                    new MockHttpServletResponse();

            var application =
                    JakartaServletWebApplication
                            .buildApplication(servletContext);

            WebContext context =
                    new WebContext(
                            application.buildExchange(
                                    request,
                                    response));

            context.setVariable("creditNote", note);
            context.setVariable("creditNoteLines", lines);
            context.setVariable(
                    "canRegisterCreditNote", canRegister);
            context.setVariable("activeMenu", "invoices");

            context.setVariable(
                    "tenantSubscriptionActive", false);
            context.setVariable(
                    "tenantSubscriptionPlan", "FREE");

            context.setVariable(
                    "_csrf",
                    new DefaultCsrfToken(
                            "X-CSRF-TOKEN",
                            "_csrf",
                            "ccn-detail-test-token"));

            return engine.process(TEMPLATE, context);

        } finally {
            applicationContext.close();
        }
    }

    @Test
    void draftNoteRendersDocumentInformation() {

        String html = render(
                note("DRAFT"),
                List.of(line()));

        assertTrue(html.contains("CCN-TEST-00050"));
        assertTrue(html.contains("DRAFT"));
        assertTrue(html.contains(
                "Rettifica fattura cliente"));
        assertTrue(html.contains("13/11/2026"));
        assertTrue(html.contains("EUR"));

        assertTrue(html.contains(
                "href=\"/invoices/500\""));

        System.out.println(
                "CCN_DETAIL_DRAFT_RENDERING: OK");
    }

    @Test
    void registeredNoteRendersAmountsAndStatus() {

        String html = render(
                note("REGISTERED"),
                List.of(line()));

        assertTrue(html.contains("REGISTERED"));
        assertTrue(html.contains("Totali nota di credito"));
        assertTrue(html.contains("Imponibile"));
        assertTrue(html.contains("IVA"));
        assertTrue(html.contains("122,00"));

        assertFalse(html.contains(
                "action=\"/customer-credit-notes/50/register\""));

        System.out.println(
                "CCN_DETAIL_REGISTERED_RENDERING: OK");
    }

    @Test
    void creditNoteLinesRenderCorrectly() {

        String html = render(
                note("DRAFT"),
                List.of(line()));

        assertTrue(html.contains("Righe rettificate"));
        assertTrue(html.contains("Prodotto test CCN"));
        assertTrue(html.contains("PZ"));
        assertTrue(html.contains("100,00"));

        System.out.println(
                "CCN_DETAIL_LINES_RENDERING: OK");
    }


    // GESTIVA V2 - PASSO 5.13.2395DV

    @Test
    void authorizedDraftRendersRegistrationActionAndCsrf() {

        String html = render(
                note("DRAFT"),
                List.of(line()),
                true
        );

        assertTrue(html.contains(
                "data-confirm-action=\"/customer-credit-notes/50/register\""
        ));

        assertTrue(html.contains(
                "data-confirm-submit=\"Registra e contabilizza\""
        ));

        assertTrue(html.contains(
                "onclick=\"openConfirmModal(this)\""
        ));

        // Il form POST appartiene alla modale condivisa.
        assertTrue(html.contains(
                "id=\"confirmActionModalForm\""
        ));

        assertTrue(html.contains(
                "name=\"_csrf\""
        ));

        assertTrue(html.contains(
                "value=\"ccn-detail-test-token\""
        ));

        System.out.println(
                "CCN_REGISTER_AUTHORIZED_DRAFT_RENDERING: OK"
        );
    }

    @Test
    void unauthorizedDraftDoesNotRenderRegistrationAction() {

        String html = render(
                note("DRAFT"),
                List.of(line()),
                false
        );

        assertFalse(html.contains(
                "data-confirm-action=\"/customer-credit-notes/50/register\""
        ));

        assertFalse(html.contains(
                "data-confirm-submit=\"Registra e contabilizza\""
        ));

        assertTrue(html.contains("CCN-TEST-00050"));

        System.out.println(
                "CCN_REGISTER_UNAUTHORIZED_DRAFT_HIDDEN: OK"
        );
    }

    @Test
    void registeredNoteDoesNotRenderRegistrationAction() {

        String html = render(
                note("REGISTERED"),
                List.of(line()),
                false
        );

        assertTrue(html.contains("REGISTERED"));

        assertFalse(html.contains(
                "data-confirm-action=\"/customer-credit-notes/50/register\""
        ));

        assertFalse(html.contains(
                "data-confirm-submit=\"Registra e contabilizza\""
        ));

        System.out.println(
                "CCN_REGISTER_ALREADY_REGISTERED_HIDDEN: OK"
        );
    }
}
