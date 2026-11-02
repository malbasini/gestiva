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

            // Variabili normalmente fornite dal contesto applicativo.
            // Nel test isolato rappresentiamo un tenant senza
            // abbonamento attivo.
            context.setVariable("tenantSubscriptionActive", false);
            context.setVariable("tenantSubscriptionPlan", "FREE");

            // Token CSRF simulato esclusivamente per il rendering.
            // Non viene eseguita alcuna richiesta HTTP di modifica.
            org.springframework.security.web.csrf.CsrfToken csrfToken =
                    new org.springframework.security.web.csrf.DefaultCsrfToken(
                            "X-CSRF-TOKEN",
                            "_csrf",
                            "test-rendering-token"
                    );

            context.setVariable("_csrf", csrfToken);


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
    @Test
    void companyCreateShouldRender() throws Exception {

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

            context.setVariable(
                    "companyCreateForm",
                    new com.gestiva.settings.company.web.CompanyCreateForm()
            );
            context.setVariable("activeMenu", "settings");

            // Variabili normalmente fornite dal contesto applicativo.
            // Nel test isolato rappresentiamo un tenant senza
            // abbonamento attivo.
            context.setVariable("tenantSubscriptionActive", false);
            context.setVariable("tenantSubscriptionPlan", "FREE");

            // Token CSRF simulato esclusivamente per il rendering.
            // Non viene eseguita alcuna richiesta HTTP di modifica.
            org.springframework.security.web.csrf.CsrfToken csrfToken =
                    new org.springframework.security.web.csrf.DefaultCsrfToken(
                            "X-CSRF-TOKEN",
                            "_csrf",
                            "test-rendering-token"
                    );

            context.setVariable("_csrf", csrfToken);


            org.springframework.web.context.support.GenericWebApplicationContext
                    webContext =
                    new org.springframework.web.context.support.GenericWebApplicationContext(
                            servletContext
                    );

            webContext.refresh();

            servletContext.setAttribute(
                    org.springframework.web.context.WebApplicationContext
                            .ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                    webContext
            );

            try {
                org.thymeleaf.spring6.view.ThymeleafViewResolver viewResolver =
                        new org.thymeleaf.spring6.view.ThymeleafViewResolver();

                viewResolver.setTemplateEngine(engine);
                viewResolver.setApplicationContext(webContext);

                org.springframework.web.servlet.View view =
                        viewResolver.resolveViewName(
                                "settings/company/company-create",
                                java.util.Locale.ITALIAN
                        );

                assertNotNull(view);

                java.util.Map<String, Object> model =
                        new java.util.HashMap<>();

                model.put(
                        "companyCreateForm",
                        new com.gestiva.settings.company.web.CompanyCreateForm()
                );

                model.put("activeMenu", "settings");
                model.put("tenantSubscriptionActive", false);
                model.put("tenantSubscriptionPlan", "FREE");
                model.put("_csrf", csrfToken);

                view.render(model, request, response);

            } finally {
                webContext.close();
            }

            String html = response.getContentAsString();

            assertNotNull(html);
            assertTrue(html.contains("Nuova società"));
            assertTrue(html.contains("name=\"code\""));
            assertTrue(html.contains("name=\"legalName\""));
            assertTrue(html.contains("name=\"_csrf\""));
            assertTrue(html.contains("test-rendering-token"));

        } finally {
            applicationContext.close();
        }
    }
    @Test
    void companyEditShouldRender() throws Exception {

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

            context.setVariable(
                    "companyCreateForm",
                    new com.gestiva.settings.company.web.CompanyCreateForm()
            );
            context.setVariable("activeMenu", "settings");

            // Variabili normalmente fornite dal contesto applicativo.
            // Nel test isolato rappresentiamo un tenant senza
            // abbonamento attivo.
            context.setVariable("tenantSubscriptionActive", false);
            context.setVariable("tenantSubscriptionPlan", "FREE");

            // Token CSRF simulato esclusivamente per il rendering.
            // Non viene eseguita alcuna richiesta HTTP di modifica.
            org.springframework.security.web.csrf.CsrfToken csrfToken =
                    new org.springframework.security.web.csrf.DefaultCsrfToken(
                            "X-CSRF-TOKEN",
                            "_csrf",
                            "test-rendering-token"
                    );

            context.setVariable("_csrf", csrfToken);


            org.springframework.web.context.support.GenericWebApplicationContext
                    webContext =
                    new org.springframework.web.context.support.GenericWebApplicationContext(
                            servletContext
                    );

            webContext.refresh();

            servletContext.setAttribute(
                    org.springframework.web.context.WebApplicationContext
                            .ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                    webContext
            );

            try {
                org.thymeleaf.spring6.view.ThymeleafViewResolver viewResolver =
                        new org.thymeleaf.spring6.view.ThymeleafViewResolver();

                viewResolver.setTemplateEngine(engine);
                viewResolver.setApplicationContext(webContext);

                org.springframework.web.servlet.View view =
                        viewResolver.resolveViewName(
                                "settings/company/company-edit",
                                java.util.Locale.ITALIAN
                        );

                assertNotNull(view);

                java.util.Map<String, Object> model =
                        new java.util.HashMap<>();

                com.gestiva.platform.company.entity.Company company =
                        new com.gestiva.platform.company.entity.Company();

                company.setId(500L);
                company.setTenantId(999L);
                company.setCode("TEST01");
                company.setLegalName("Società esistente");
                company.setActive(true);

                com.gestiva.settings.company.web.CompanyEditForm form =
                        new com.gestiva.settings.company.web.CompanyEditForm();

                form.setLegalName(company.getLegalName());
                form.setActive(company.isActive());

                model.put("company", company);
                model.put("companyEditForm", form);

                model.put("activeMenu", "settings");
                model.put("tenantSubscriptionActive", false);
                model.put("tenantSubscriptionPlan", "FREE");
                model.put("_csrf", csrfToken);

                view.render(model, request, response);

            } finally {
                webContext.close();
            }

            String html = response.getContentAsString();

            assertNotNull(html);
            assertTrue(html.contains("Modifica società"));
            assertTrue(html.contains("TEST01"));
            assertTrue(html.contains("readonly"));
            assertTrue(html.contains("name=\"legalName\""));
            assertTrue(html.contains("name=\"active\""));
            assertTrue(html.contains("name=\"_csrf\""));
            assertTrue(html.contains("test-rendering-token"));

        } finally {
            applicationContext.close();
        }
    }
}
