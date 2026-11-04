package com.gestiva.billing.invoice.controller;

import com.gestiva.billing.invoice.service.InvoicePdfService;
import com.gestiva.security.auth.AuthenticatedUser;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class InvoicePdfAuthorizationIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private InvoicePdfService pdfService;

    @Test
    void warehouseOnlyMustNotDownloadInvoices()
            throws Exception {

        var authorities = List.of(
                new SimpleGrantedAuthority("ROLE_WAREHOUSE")
        );

        AuthenticatedUser principal = new AuthenticatedUser(
                100L,
                2L,
                "tenant-test",
                "warehouse@example.invalid",
                "test-only",
                true,
                authorities
        );

        var auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                authorities
        );

        // Se l'autorizzazione è corretta,
        // il controller non deve essere raggiunto.
        mvc.perform(
                get("/api/invoices/1/pdf")
                        .with(authentication(auth))
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(pdfService);

        System.out.println(
                "PDF FATTURE: ROLE_WAREHOUSE RESPINTO."
        );
    }
}
