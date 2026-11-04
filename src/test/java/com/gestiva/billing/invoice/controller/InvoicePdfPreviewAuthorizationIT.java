package com.gestiva.billing.invoice.controller;

import com.gestiva.billing.invoice.service.InvoicePdfService;
import com.gestiva.security.auth.AuthenticatedUser;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@AutoConfigureMockMvc
class InvoicePdfPreviewAuthorizationIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;

    private static final String EMAIL =
            "collaudo.multicompany1783@example.invalid";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private InvoicePdfService pdfService;

    @Test
    void previewMustRespectFunctionalRoles() throws Exception {

        try (Connection c =
                     jdbc.getDataSource().getConnection()) {

            assertEquals("gestiva_test", c.getCatalog());

            String username = c.getMetaData().getUserName();

            assertTrue(
                    username.equals("gestiva_test_runner")
                    || username.startsWith(
                            "gestiva_test_runner@"
                    )
            );
        }

        List<Long> users = jdbc.queryForList("""
                SELECT id
                FROM app_user
                WHERE tenant_id = ?
                  AND email = ?
                  AND status = 'ACTIVE'
                  AND admin = 0
                """,
                Long.class,
                TENANT,
                EMAIL
        );

        assertEquals(1, users.size());
        Long userId = users.get(0);

        List<Long> companies = jdbc.queryForList("""
                SELECT c.id
                FROM company c
                JOIN user_company_access uca
                  ON uca.tenant_id = c.tenant_id
                 AND uca.company_id = c.id
                WHERE c.tenant_id = ?
                  AND uca.user_id = ?
                  AND c.legal_name = 'Azienda Demo'
                  AND c.active = 1
                """,
                Long.class,
                TENANT,
                userId
        );

        assertEquals(1, companies.size());
        Long companyId = companies.get(0);

        String[] roles = {
                "WAREHOUSE",
                "PURCHASING",
                "SALES",
                "ACCOUNTING",
                "ADMIN"
        };

        for (String role : roles) {

            reset(pdfService);

            when(pdfService.generatePdf(TENANT, 1L))
                    .thenReturn(new byte[] {
                            37, 80, 68, 70
                    });

            var authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + role)
            );

            AuthenticatedUser principal =
                    new AuthenticatedUser(
                            userId,
                            TENANT,
                            "tenant-test",
                            EMAIL,
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

            session.setAttribute(
                    "GESTIVA_ACTIVE_COMPANY_ID",
                    companyId
            );

            MvcResult result = mvc.perform(
                    get("/api/invoices/1/preview")
                            .session(session)
                            .with(authentication(auth))
            ).andReturn();

            int status = result.getResponse().getStatus();

            boolean invoked =
                    mockingDetails(pdfService)
                            .getInvocations()
                            .stream()
                            .anyMatch(invocation ->
                                    invocation.getMethod()
                                            .getName()
                                            .equals("generatePdf")
                            );

            System.out.println(
                    "PREVIEW ROLE_" + role
                    + " | HTTP " + status
                    + " | PDF invocato: " + invoked
            );

            boolean permitted =
                    "SALES".equals(role)
                    || "ACCOUNTING".equals(role)
                    || "ADMIN".equals(role);

            if (permitted) {
                assertEquals(200, status);
                assertTrue(invoked);
            } else {
                assertEquals(403, status);
                assertFalse(invoked);
            }
        }

        System.out.println(
                "AUTORIZZAZIONI ANTEPRIMA PDF OK."
        );
    }
}
