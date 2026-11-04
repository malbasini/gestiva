package com.gestiva.billing.invoice.controller;

import com.gestiva.billing.invoice.service.InvoicePdfService;
import com.gestiva.security.auth.AuthenticatedUser;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@AutoConfigureMockMvc
class InvoicePdfRoleDiagnosticIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private InvoicePdfService pdfService;

    @Test
    void diagnosePdfAccessByRole() throws Exception {

        String[] roles = {
                "WAREHOUSE",
                "SALES",
                "ACCOUNTING",
                "ADMIN"
        };

        for (String role : roles) {

            clearInvocations(pdfService);

            when(pdfService.generatePdf(2L, 1L))
                    .thenReturn(new byte[] {
                            37, 80, 68, 70
                    });

            var authorities = List.of(
                    new SimpleGrantedAuthority(
                            "ROLE_" + role
                    )
            );

            var principal = new AuthenticatedUser(
                    100L,
                    2L,
                    "tenant-test",
                    "diagnostic@example.invalid",
                    "test-only",
                    true,
                    authorities
            );

            var auth =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            authorities
                    );

            MockHttpSession session = new MockHttpSession();

            MvcResult result = mvc.perform(
                    get("/api/invoices/1/pdf")
                            .session(session)
                            .with(authentication(auth))
            ).andReturn();

            int status = result.getResponse().getStatus();

            boolean pdfInvoked =
                    mockingDetails(pdfService)
                            .getInvocations()
                            .stream()
                            .anyMatch(invocation ->
                                    invocation.getMethod()
                                            .getName()
                                            .equals("generatePdf")
                            );

            System.out.println(
                    "ROLE_" + role
                    + " | HTTP " + status
                    + " | PDF invocato: " + pdfInvoked
            );

            if (result.getResolvedException() != null) {
                System.out.println(
                        "  Eccezione MVC: "
                        + result.getResolvedException()
                                .getClass().getSimpleName()
                );
            }

            // Registriamo il comportamento senza assumere
            // che la configurazione attuale sia corretta.
            assertTrue(
                    status >= 100 && status <= 599
            );
        }

        System.out.println(
                "DIAGNOSTICA RUOLI PDF COMPLETATA."
        );
    }
}
