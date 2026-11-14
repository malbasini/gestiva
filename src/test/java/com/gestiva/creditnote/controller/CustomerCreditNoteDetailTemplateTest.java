
package com.gestiva.creditnote.controller;

import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteLine;
import com.gestiva.creditnote.entity.CreditNoteFinancialPosition;
import com.gestiva.accounting.due.entity.PaymentDue;

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
        when(line.getLineSubtotal())
                .thenReturn(new BigDecimal("100.00"));
        when(line.getTaxAmount())
                .thenReturn(new BigDecimal("22.00"));
        when(line.getLineTotal())
                .thenReturn(new BigDecimal("122.00"));

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


        return render(
                note, lines, canRegister,
                false, null, List.of()
        );
    }

    private String render(
            CreditNote note,
            List<CreditNoteLine> lines,
            boolean canRegister,
            boolean canCompensate,
            CreditNoteFinancialPosition position,
            List<PaymentDue> eligibleDues) {

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
                    "canCompensateCreditNote", canCompensate);
            context.setVariable(
                    "compensationPosition", position);
            context.setVariable(
                    "compensableDues", eligibleDues);
            context.setVariable(
                    "compensationOperationKey",
                    "v11-template-test-operation");
            context.setVariable(
                    "compensationDate",
                    LocalDate.of(2026, 11, 14));

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

    // GESTIVA V2 - PASSO 5.13.2395MX
    // L'imponibile della riga esclude l'IVA.

    @Test
    void creditNoteLineImponibileExcludesVat() {

        String html = render(
                note("DRAFT"),
                List.of(line()));

        int descriptionIndex =
                html.indexOf("Prodotto test CCN");

        assertTrue(
                descriptionIndex >= 0,
                "Riga commerciale non trovata");

        int rowStart =
                html.lastIndexOf("<tr", descriptionIndex);

        int rowEnd =
                html.indexOf("</tr>", descriptionIndex);

        assertTrue(
                rowStart >= 0 && rowEnd > rowStart,
                "Riga HTML non individuata");

        String row = html.substring(
                rowStart, rowEnd + "</tr>".length());

        var matcher = java.util.regex.Pattern
                .compile(
                        "(?s)<td\\b[^>]*>(.*?)</td>")
                .matcher(row);

        java.util.List<String> cells =
                new java.util.ArrayList<>();

        while (matcher.find()) {
            cells.add(
                    matcher.group(1)
                            .replaceAll("(?s)<[^>]+>", "")
                            .trim()
            );
        }

        assertEquals(
                8, cells.size(),
                "Numero colonne riga inatteso");

        String imponibile = cells.get(7);

        assertEquals(
                "100,00",
                imponibile,
                "L'imponibile deve escludere l'IVA");

        assertNotEquals(
                "122,00",
                imponibile,
                "Non mostrare il totale IVA inclusa");

        assertTrue(
                html.contains("122,00"),
                "Il totale della nota deve rimanere visibile");

        System.out.println(
                "CCN_LINE_SUBTOTAL_EXCLUDES_VAT: VERIFIED");
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

    // GESTIVA V2 - PASSO 5.13.2395LB
    // Rendering reale del form V11 senza database.

    @Test
    void registeredCompensableNoteRendersV11Form() {
        CreditNoteFinancialPosition position =
                mock(CreditNoteFinancialPosition.class);
        when(position.getAvailableAmount())
                .thenReturn(new BigDecimal("93.33"));
        when(position.getStatus()).thenReturn("OPEN");

        PaymentDue due = mock(PaymentDue.class);
        when(due.getId()).thenReturn(9L);
        when(due.getDocumentNumber())
                .thenReturn("INV-TEST-00001");
        when(due.getOpenAmount())
                .thenReturn(new BigDecimal("186.66"));
        when(due.getCurrencyCode()).thenReturn("EUR");

        String html = render(
                note("REGISTERED"),
                List.of(line()),
                false,
                true,
                position,
                List.of(due)
        );

        assertTrue(html.contains("Compensazione fattura cliente"));
        assertTrue(html.contains(
                "action=\"/customer-credit-notes/50/compensations\""));
        assertTrue(html.contains("INV-TEST-00001"));
        assertTrue(html.contains("186,66"));
        assertTrue(html.contains("93,33"));
        assertTrue(html.contains("v11-template-test-operation"));
        assertTrue(html.contains("name=\"paymentDueId\""));
        assertTrue(html.contains("name=\"amount\""));
        assertTrue(html.contains("name=\"compensationDate\""));
        assertTrue(html.contains("name=\"operationKey\""));
        assertTrue(html.contains("name=\"_csrf\""));
        assertTrue(html.contains("ccn-detail-test-token"));
        assertTrue(html.contains("2026-11-14"));

        System.out.println("V11_CUSTOMER_COMPENSABLE_RENDERING: VERIFIED");
    }

    @Test
    void draftNoteDoesNotRenderV11CompensationForm() {
        String html = render(
                note("DRAFT"),
                List.of(line()),
                true,
                false,
                null,
                List.of()
        );

        assertFalse(html.contains(
                "action=\"/customer-credit-notes/50/compensations\""));
        assertTrue(html.contains(
                "data-confirm-action=\"/customer-credit-notes/50/register\""));

        System.out.println("V11_CUSTOMER_DRAFT_COMPENSATION_HIDDEN: VERIFIED");
    }

    @Test
    void registeredNotCompensableNoteHidesV11Form() {
        String html = render(
                note("REGISTERED"),
                List.of(line()),
                false,
                false,
                null,
                List.of()
        );

        assertFalse(html.contains(
                "action=\"/customer-credit-notes/50/compensations\""));
        assertTrue(html.contains("CCN-TEST-00050"));

        System.out.println("V11_CUSTOMER_UNAVAILABLE_FORM_HIDDEN: VERIFIED");
    }
}
