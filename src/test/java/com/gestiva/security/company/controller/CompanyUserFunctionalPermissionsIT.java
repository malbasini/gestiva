package com.gestiva.security.company.controller;

import com.gestiva.security.auth.AuthenticatedUser;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Connection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class CompanyUserFunctionalPermissionsIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;

    private static final String EMAIL =
            "collaudo.multicompany1783@example.invalid";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void salesAndWarehouseMustNotAccessOtherModules()
            throws Exception {

        // Sicurezza dell'ambiente prima delle verifiche.
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

        Long userId = jdbc.queryForObject("""
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

        assertNotNull(userId);

        List<String> actualRoles = jdbc.queryForList("""
                SELECT r.code
                FROM user_role ur
                JOIN role r
                  ON r.id = ur.role_id
                 AND r.tenant_id = ur.tenant_id
                WHERE ur.tenant_id = ?
                  AND ur.user_id = ?
                """,
                String.class,
                TENANT,
                userId
        );

        assertEquals(
                Set.of("ROLE_SALES", "ROLE_WAREHOUSE"),
                Set.copyOf(actualRoles)
        );

        assertEquals(2, actualRoles.size());

        var authorities = List.of(
                new SimpleGrantedAuthority("ROLE_SALES"),
                new SimpleGrantedAuthority("ROLE_WAREHOUSE")
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

        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );

        String[] forbiddenEndpoints = {
                "/purchase-orders",
                "/suppliers",
                "/payments",
                "/accounting-dashboard",
                "/settings"
        };

        for (String endpoint : forbiddenEndpoints) {

            mvc.perform(
                    get(endpoint)
                            .with(authentication(auth))
            )
            .andExpect(status().isForbidden());

            System.out.println(
                    "HTTP 403 CORRETTO: " + endpoint
            );
        }

        System.out.println(
                "ISOLAMENTO RUOLI FUNZIONALI OK."
        );
    }
}
