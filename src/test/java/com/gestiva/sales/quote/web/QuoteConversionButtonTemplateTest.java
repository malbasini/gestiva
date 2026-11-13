package com.gestiva.sales.quote.web;

import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class QuoteConversionButtonTemplateTest {

    private static final Path TEMPLATE = Path.of(
            "src/main/resources/templates/quote/quote-detail.html"
    );

    private String renderConversionButton(
            String status,
            boolean locked
    ) throws Exception {

        String html = Files.readString(
                TEMPLATE,
                StandardCharsets.UTF_8
        );

        Pattern pattern = Pattern.compile(
                "<form\\s+th:if=\"([^\"]+)\""
        );

        Matcher matcher = pattern.matcher(html);

        assertTrue(
                matcher.find(),
                "Condizione Thymeleaf conversione non trovata"
        );

        String condition = matcher.group(1);

        assertTrue(
                html.contains("Converti in ordine"),
                "Pulsante conversione assente nel template"
        );

        QuoteDetailView quote = new QuoteDetailView();
        quote.setStatus(status);
        quote.setLocked(locked);

        Context context = new Context();
        context.setVariable("quote", quote);

        SpringTemplateEngine engine = new SpringTemplateEngine();

        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode("HTML");
        engine.setTemplateResolver(resolver);

        String snippet =
                "<form th:if=\"" + condition + "\">"
                + "Converti in ordine"
                + "</form>";

        return engine.process(snippet, context);
    }

    @Test
    void draftDoesNotShowConversionButton() throws Exception {
        assertFalse(
                renderConversionButton("DRAFT", false)
                        .contains("Converti in ordine")
        );
    }

    @Test
    void sentDoesNotShowConversionButton() throws Exception {
        assertFalse(
                renderConversionButton("SENT", false)
                        .contains("Converti in ordine")
        );
    }

    @Test
    void rejectedDoesNotShowConversionButton() throws Exception {
        assertFalse(
                renderConversionButton("REJECTED", false)
                        .contains("Converti in ordine")
        );
    }

    @Test
    void acceptedNotConvertedShowsButton() throws Exception {
        assertTrue(
                renderConversionButton("ACCEPTED", false)
                        .contains("Converti in ordine")
        );
    }

    @Test
    void acceptedAlreadyConvertedHidesButton() throws Exception {
        assertFalse(
                renderConversionButton("ACCEPTED", true)
                        .contains("Converti in ordine")
        );
    }
}
