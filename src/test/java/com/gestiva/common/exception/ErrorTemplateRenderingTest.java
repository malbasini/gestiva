package com.gestiva.common.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class ErrorTemplateRenderingTest {

    private SpringTemplateEngine templateEngine;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver =
                new ClassLoaderTemplateResolver();

        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);
    }

    private String render(String template, IWebContext context) {
        return templateEngine.process(template, context);
    }

    private IWebContext context() {
        return context(java.util.Map.of());
    }

    private IWebContext context(java.util.Map<String, Object> variables) {
        MockServletContext servletContext = new MockServletContext();

        MockHttpServletRequest request =
                new MockHttpServletRequest(servletContext);

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        JakartaServletWebApplication application =
                JakartaServletWebApplication.buildApplication(servletContext);

        return new WebContext(
                application.buildExchange(request, response),
                Locale.ITALIAN,
                variables
        );
    }

    @Test
    void shouldRender403Template() {
        String html = render("error/403", context());

        assertTrue(html.contains("Accesso negato"));
        assertTrue(html.contains("Non hai i permessi necessari"));
        assertTrue(html.contains("<main"));
        assertTrue(html.contains("<h1"));
    }

    @Test
    void shouldRender404Template() {
        String html = render("error/404", context());

        assertTrue(html.contains("Pagina non trovata"));
        assertTrue(html.contains("La pagina richiesta non esiste"));
        assertTrue(html.contains("<main"));
        assertTrue(html.contains("<h1"));
    }

    @Test
    void shouldRender500Template() {
        String html = render("error/500", context());

        assertTrue(html.contains("Si è verificato un problema"));
        assertTrue(html.contains("Non è stato possibile completare"));
        assertTrue(html.contains("<main"));
        assertTrue(html.contains("<h1"));

        assertFalse(html.contains("java.lang.IllegalStateException"));
        assertFalse(html.contains("SQLException"));
        assertFalse(html.contains("stackTrace"));
    }

    @Test
    void shouldRenderBusinessErrorTemplate() {
        IWebContext context = context(
                java.util.Map.of(
                        "errorMessage",
                        "Operazione non consentita"
                )
        );

        String html = render("error/business-error", context);

        assertTrue(html.contains("Operazione non completata"));
        assertTrue(html.contains("Operazione non consentita"));
        assertTrue(html.contains("<main"));
        assertTrue(html.contains("<h1"));
    }

    @Test
    void shouldRenderBusinessErrorFallbackMessage() {
        String html = render("error/business-error", context());

        assertTrue(
                html.contains(
                    "Non è stato possibile completare l’operazione richiesta."
                )
        );
    }
}
