package com.gestiva.warehouse.location.controller;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WarehouseLocationTemplateTest {

    private String render(
            Long selectedWarehouseId,
            List<?> locations) {

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
                            "/warehouse/locations"
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

            context.setVariable("warehouses", List.of());
            context.setVariable(
                    "selectedWarehouseId",
                    selectedWarehouseId
            );
            context.setVariable("locations", locations);
            context.setVariable("activeMenu", "locations");

            context.setVariable(
                    "tenantSubscriptionActive",
                    false
            );
            context.setVariable(
                    "tenantSubscriptionPlan",
                    "FREE"
            );

            var csrfToken =
                    new org.springframework.security.web.csrf.DefaultCsrfToken(
                            "X-CSRF-TOKEN",
                            "_csrf",
                            "test-rendering-token"
                    );

            context.setVariable("_csrf", csrfToken);

            return engine.process(
                    "warehouse/location/location-list",
                    context
            );

        } finally {
            applicationContext.close();
        }
    }

    @Test
    void noWarehouseSelectedShouldRender() {
        String html = render(null, List.of());

        assertNotNull(html);
        assertTrue(html.contains(
                "Ubicazioni di magazzino"
        ));
        assertTrue(html.contains(
                "Seleziona un magazzino per visualizzare"
        ));
        assertTrue(html.contains(
                "name=\"warehouseId\""
        ));
    }

    @Test
    void selectedWarehouseWithoutLocationsShouldRender() {
        String html = render(10L, List.of());

        assertNotNull(html);
        assertTrue(html.contains(
                "Nessuna ubicazione configurata"
        ));
        assertTrue(html.contains(
                "per il magazzino selezionato"
        ));
    }

    @Test
    void populatedLocationWithParentShouldRender() {
        var parent =
                new com.gestiva.warehouse.location.entity.WarehouseLocation();
        parent.setCode("A-01");

        var child =
                new com.gestiva.warehouse.location.entity.WarehouseLocation();
        child.setCode("A-01-01");
        child.setName("Scaffale 01");
        child.setLocationType(
                com.gestiva.warehouse.location.entity.WarehouseLocationType.STORAGE
        );
        child.setParentLocation(parent);
        child.setBarcode("LOC-A0101");
        child.setActive(true);

        String html = render(10L, List.of(child));

        assertNotNull(html);
        assertTrue(html.contains("A-01-01"));
        assertTrue(html.contains("Scaffale 01"));
        assertTrue(html.contains("LOC-A0101"));
        assertTrue(html.contains("A-01"));
        assertTrue(html.contains("Attiva"));
    }

}
