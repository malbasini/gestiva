package com.gestiva.settings.company.view;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompanyManagementTemplateTest {

    @Test
    void companyListShouldRender() {

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
                            servletContext, "GET", "/settings/companies"
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

            context.setVariable("companies", List.of());
            context.setVariable("activeMenu", "settings");

            String html = engine.process(
                    "settings/company/company-list",
                    context
            );

            assertNotNull(html);
            assertTrue(html.contains("Gestione società"));
            assertTrue(html.contains("Nuova società"));
            assertTrue(html.contains("Nessuna società disponibile."));

        } finally {
            applicationContext.close();
        }
    }
}
