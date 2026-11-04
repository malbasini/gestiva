package com.gestiva.security.company.service;

import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CompanyAccessPersistenceDiagnosticIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void verifyRealCompanyAssignments() throws Exception {

        try (Connection c =
                     jdbc.getDataSource().getConnection()) {

            assertEquals("gestiva_test", c.getCatalog());

            String username =
                    c.getMetaData().getUserName();

            assertTrue(
                    username.equals("gestiva_test_runner")
                    || username.startsWith(
                            "gestiva_test_runner@"
                    ),
                    "Account MySQL non autorizzato"
            );
        }

        String email =
                "collaudo.multicompany1783@example.invalid";

        List<Long> userIds = jdbc.queryForList("""
                SELECT id
                FROM app_user
                WHERE tenant_id = ?
                  AND email = ?
                """,
                Long.class,
                TENANT,
                email
        );

        assertEquals(
                1,
                userIds.size(),
                "Utente di collaudo non univoco"
        );

        Long userId = userIds.get(0);

        String status = jdbc.queryForObject("""
                SELECT status
                FROM app_user
                WHERE tenant_id = ?
                  AND id = ?
                """,
                String.class,
                TENANT,
                userId
        );

        assertEquals("ACTIVE", status);

        List<String> authorizedNames =
                jdbc.queryForList("""
                    SELECT c.legal_name
                    FROM user_company_access uca
                    JOIN company c
                      ON c.id = uca.company_id
                     AND c.tenant_id = uca.tenant_id
                    WHERE uca.tenant_id = ?
                      AND uca.user_id = ?
                    ORDER BY c.legal_name
                    """,
                    String.class,
                    TENANT,
                    userId
                );

        Set<String> expected = Set.of(
                "Azienda Demo",
                "Società Collaudo Gestiva V2"
        );

        Set<String> actual =
                authorizedNames.stream()
                        .collect(Collectors.toSet());

        assertEquals(expected, actual);

        assertEquals(
                2,
                authorizedNames.size(),
                "Numero autorizzazioni inatteso"
        );

        assertFalse(
                actual.contains("Azienda Test 2")
        );

        List<String> roles = jdbc.queryForList("""
                SELECT r.code
                FROM user_role ur
                JOIN role r
                  ON r.id = ur.role_id
                 AND r.tenant_id = ur.tenant_id
                WHERE ur.tenant_id = ?
                  AND ur.user_id = ?
                ORDER BY r.code
                """,
                String.class,
                TENANT,
                userId
        );

        assertEquals(
                Set.of("ROLE_SALES", "ROLE_WAREHOUSE"),
                Set.copyOf(roles)
        );

        assertEquals(2, roles.size());

        System.out.println(
                "MYSQL AUTORIZZAZIONI OK: Demo + Collaudo V2."
        );

        System.out.println(
                "MYSQL REVOCA OK: Azienda Test 2 assente."
        );

        System.out.println(
                "MYSQL RUOLI OK: SALES + WAREHOUSE."
        );

        System.out.println(
                "DIAGNOSTICA MYSQL COMPLETATA — SOLA LETTURA."
        );
    }
}
