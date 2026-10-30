package com.gestiva.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GestivaAccessDeniedHandlerTest {

    private final GestivaAccessDeniedHandler handler =
            new GestivaAccessDeniedHandler();

    private MockHttpServletResponse execute(
            String path,
            String accept,
            boolean ajax
    ) throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", path);

        request.setServletPath(path);

        if (accept != null) {
            request.addHeader("Accept", accept);
        }

        if (ajax) {
            request.addHeader(
                    "X-Requested-With",
                    "XMLHttpRequest"
            );
        }

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        handler.handle(
                request,
                response,
                new AccessDeniedException("Dettaglio tecnico riservato")
        );

        return response;
    }

    @Test
    void shouldReturnHtml403ForBrowserPage() throws Exception {
        var response = execute(
                "/settings/users",
                "text/html",
                false
        );

        assertEquals(403, response.getStatus());
        assertEquals("/403", response.getForwardedUrl());
    }

    @Test
    void shouldReturnJson403ForRestApi() throws Exception {
        var response = execute(
                "/api/customers/1",
                "application/json",
                false
        );

        assertEquals(403, response.getStatus());
        assertTrue(
                response.getContentType().contains("application/problem+json")
        );
        assertFalse(
                response.getContentAsString().contains(
                        "Dettaglio tecnico riservato"
                )
        );
    }

    @Test
    void shouldReturnJson403ForAjax() throws Exception {
        var response = execute(
                "/help/chat",
                "*/*",
                true
        );

        assertEquals(403, response.getStatus());
        assertTrue(
                response.getContentType().contains("application/problem+json")
        );
    }

    @Test
    void shouldReturnJson403ForExport() throws Exception {
        var response = execute(
                "/accounting/trial-balance/export.xlsx",
                "*/*",
                false
        );

        assertEquals(403, response.getStatus());
        assertTrue(
                response.getContentType().contains("application/problem+json")
        );
        assertFalse(
                response.getContentAsString().contains(
                        "Dettaglio tecnico riservato"
                )
        );
    }

    @Test
    void shouldReturnJson403ForPdfDownload() throws Exception {
        var response = execute(
                "/api/invoices/1/pdf",
                "application/pdf",
                false
        );

        assertEquals(403, response.getStatus());
        assertTrue(
                response.getContentType().contains("application/problem+json")
        );
    }
}
