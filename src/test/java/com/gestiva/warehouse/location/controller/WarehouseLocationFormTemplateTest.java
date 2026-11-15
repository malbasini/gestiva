package com.gestiva.warehouse.location.controller;

import com.gestiva.warehouse.location.entity.WarehouseLocation;
import com.gestiva.warehouse.location.entity.WarehouseLocationType;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.web.servlet.support.RequestContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.context.webmvc.SpringWebMvcThymeleafRequestContext;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WarehouseLocationFormTemplateTest {

    private String render(List<WarehouseLocation> parents) {
        MockServletContext servletContext =
                new MockServletContext();

        StaticWebApplicationContext applicationContext =
                new StaticWebApplicationContext();

        applicationContext.setServletContext(servletContext);
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

            MockHttpServletRequest request =
                    new MockHttpServletRequest(
                            servletContext,
                            "GET",
                            "/warehouse/locations/new"
                    );
            MockHttpServletResponse response =
                    new MockHttpServletResponse();

            request.setAttribute(
                    org.springframework.web.servlet.DispatcherServlet
                            .WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                    applicationContext
            );

            JakartaServletWebApplication webApplication =
                    JakartaServletWebApplication.buildApplication(
                            servletContext
                    );

            WebContext context = new WebContext(
                    webApplication.buildExchange(request, response)
            );

            WarehouseLocationForm form = new WarehouseLocationForm();
            form.setWarehouseId(10L);

            var warehouse =
                    mock(com.gestiva.warehouse.location.entity.Warehouse.class);
            when(warehouse.getId()).thenReturn(10L);
            when(warehouse.getCode()).thenReturn("MAG-TEST-001");
            when(warehouse.getName()).thenReturn("Magazzino Test");

            Map<String, Object> model = new HashMap<>();
            model.put("locationForm", form);
            model.put("warehouses", List.of(warehouse));
            model.put("locationTypes", WarehouseLocationType.values());
            model.put("parentLocations", parents);

            RequestContext requestContext =
                    new RequestContext(
                            request,
                            response,
                            servletContext,
                            model
                    );

            context.setVariable(
                    "thymeleafRequestContext",
                    new SpringWebMvcThymeleafRequestContext(
                            requestContext,
                            request
                    )
            );

            model.forEach(context::setVariable);
            context.setVariable("activeMenu", "locations");
            context.setVariable("tenantSubscriptionActive", false);
            context.setVariable("tenantSubscriptionPlan", "FREE");
            context.setVariable(
                    "_csrf",
                    new DefaultCsrfToken(
                            "X-CSRF-TOKEN",
                            "_csrf",
                            "location-form-test-token"
                    )
            );

            return engine.process(
                    "warehouse/location/location-form",
                    context
            );
        } finally {
            applicationContext.close();
        }
    }

    @Test
    void firstLocationFormShouldRender() {
        String html = render(List.of());

        assertNotNull(html);
        assertTrue(html.contains("Nuova ubicazione"));
        assertTrue(html.contains("MAG-TEST-001"));
        assertTrue(html.contains("name=\"warehouseId\""));
        assertTrue(html.contains("name=\"code\""));
        assertTrue(html.contains("name=\"name\""));
        assertTrue(html.contains("name=\"locationType\""));
        assertTrue(html.contains("name=\"parentLocationId\""));
        assertTrue(html.contains("name=\"barcode\""));
        assertTrue(html.contains("location-form-test-token"));
        assertTrue(html.contains("Salva ubicazione"));

        for (WarehouseLocationType type : WarehouseLocationType.values()) {
            assertTrue(
                    html.contains("value=\"" + type.name() + "\""),
                    "Tipo mancante: " + type
            );
        }
    }

    @Test
    void parentLocationShouldAppearInForm() {
        WarehouseLocation parent = mock(WarehouseLocation.class);
        when(parent.getId()).thenReturn(25L);
        when(parent.getCode()).thenReturn("A-01");
        when(parent.getName()).thenReturn("Area A");

        String html = render(List.of(parent));

        assertNotNull(html);
        assertTrue(html.contains("A-01 - Area A"));
        assertTrue(html.contains("value=\"25\""));
    }
}
