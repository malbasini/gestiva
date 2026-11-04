package com.gestiva.security.company.controller;

import com.gestiva.security.auth.AuthenticatedUser;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class CompanySelectionSecurityIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;

    private static final String EMAIL =
            "collaudo.multicompany1783@example.invalid";

    private static final String ACTIVE_COMPANY_KEY =
            "GESTIVA_ACTIVE_COMPANY_ID";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void revokedCompanyMustReturn403AndPreserveSession()
            throws Exception {

        // Controllo obbligatorio dell'ambiente protetto.
        try (Connection c =
                     jdbc.getDataSource().getConnection()) {

            assertEquals("gestiva_test", c.getCatalog());

            String username = c.getMetaData().getUserName();

            assertTrue(
                    username.equals("gestiva_test_runner")
                    || username.startsWith(
                            "gestiva_test_runner@"
                    ),
                    "Account MySQL non autorizzato"
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

        Long demoId = companyId("Azienda Demo");
        Long forbiddenId = companyId("Azienda Test 2");

        // Le autorizzazioni devono corrispondere
        // alla configurazione già collaudata.
        assertEquals(
                1,
                accessCount(userId, demoId)
        );

        assertEquals(
                0,
                accessCount(userId, forbiddenId)
        );

        var authorities = List.of(
                new SimpleGrantedAuthority("ROLE_SALES"),
                new SimpleGrantedAuthority("ROLE_WAREHOUSE")
        );

        AuthenticatedUser principal = new AuthenticatedUser(
                userId,
                TENANT,
                "tenant-test",
                EMAIL,
                "test-only",
                true,
                authorities
        );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );

        MockHttpSession session = new MockHttpSession();

        // 1. Selezione HTTP di una società autorizzata.
        mvc.perform(
                    post("/company/select")
                            .session(session)
                            .with(authentication(authentication))
                            .with(csrf())
                            .param("companyId", demoId.toString())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        assertEquals(
                demoId,
                session.getAttribute(ACTIVE_COMPANY_KEY)
        );

        System.out.println(
                "HTTP SOCIETÀ AUTORIZZATA OK."
        );

        // 2. Tentativo diretto con società revocata,
        //    mantenendo la stessa sessione.
        mvc.perform(
                    post("/company/select")
                            .session(session)
                            .with(authentication(authentication))
                            .with(csrf())
                            .param("companyId", forbiddenId.toString())
                )
                .andExpect(status().isForbidden());

        System.out.println(
                "HTTP 403 SOCIETÀ REVOCATA OK."
        );

        // 3. Il tentativo negato non deve modificare
        //    la società operativa già selezionata.
        assertEquals(
                demoId,
                session.getAttribute(ACTIVE_COMPANY_KEY)
        );

        System.out.println(
                "SESSIONE INVARIATA DOPO 403 OK."
        );

        // Nessuna modifica alle autorizzazioni.
        assertEquals(1, accessCount(userId, demoId));
        assertEquals(0, accessCount(userId, forbiddenId));

        System.out.println(
                "AUTORIZZAZIONI MYSQL INVARIATE OK."
        );
    }

    private Long companyId(String name) {

        List<Long> ids = jdbc.queryForList("""
                SELECT id
                FROM company
                WHERE tenant_id = ?
                  AND legal_name = ?
                  AND active = 1
                """,
                Long.class,
                TENANT,
                name
        );

        assertEquals(
                1,
                ids.size(),
                "Società attiva non univoca: " + name
        );

        return ids.get(0);
    }

    private int accessCount(Long userId, Long companyId) {

        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM user_company_access
                WHERE tenant_id = ?
                  AND user_id = ?
                  AND company_id = ?
                """,
                Integer.class,
                TENANT,
                userId,
                companyId
        );

        assertNotNull(count);

        return count;
    }
}
