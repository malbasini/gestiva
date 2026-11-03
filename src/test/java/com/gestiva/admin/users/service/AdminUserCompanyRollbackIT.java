package com.gestiva.admin.users.service;

import com.gestiva.admin.users.web.AdminUserEditForm;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AdminUserCompanyRollbackIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT_ID = 2L;
    private static final Long INVALID_COMPANY_ID = Long.MAX_VALUE;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void invalidCompanyShouldRollbackUserRolesAndAccess()
            throws Exception {

        // Protezione obbligatoria prima di qualsiasi INSERT.
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

        Long adminRoleId = jdbc.queryForObject("""
                SELECT id
                FROM role
                WHERE tenant_id = ?
                  AND code = 'ROLE_ADMIN'
                LIMIT 1
                """,
                Long.class,
                TENANT_ID
        );

        assertNotNull(adminRoleId);

        List<Long> companies = jdbc.queryForList("""
                SELECT id
                FROM company
                WHERE tenant_id = ?
                  AND active = 1
                ORDER BY id
                LIMIT 1
                """,
                Long.class,
                TENANT_ID
        );

        assertFalse(
                companies.isEmpty(),
                "Nessuna società attiva nel tenant di test"
        );

        Long originalCompanyId = companies.get(0);

        Integer invalidExists = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM company
                WHERE id = ?
                """,
                Integer.class,
                INVALID_COMPANY_ID
        );

        assertEquals(
                0,
                invalidExists,
                "L'ID scelto come inesistente è già occupato"
        );

        String marker = UUID.randomUUID()
                .toString()
                .replace("-", "");

        String oldEmail =
                "company-rbk-" + marker + "@example.invalid";

        String newEmail =
                "company-changed-" + marker + "@example.invalid";

        String oldHash = passwordEncoder.encode(
                "company-original-test-only"
        );

        Long userId = null;

        try {
            // Fixture autonoma: nessun utente preesistente
            // viene modificato o cancellato.
            jdbc.update("""
                    INSERT INTO app_user (
                        tenant_id,
                        first_name,
                        last_name,
                        email,
                        password_hash,
                        status,
                        admin,
                        locale_code,
                        created_at,
                        updated_at
                    )
                    VALUES (
                        ?, 'Prima', 'Originale',
                        ?, ?, 'ACTIVE', 0, 'it',
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP
                    )
                    """,
                    TENANT_ID,
                    oldEmail,
                    oldHash
            );

            userId = jdbc.queryForObject("""
                    SELECT id
                    FROM app_user
                    WHERE tenant_id = ?
                      AND email = ?
                    """,
                    Long.class,
                    TENANT_ID,
                    oldEmail
            );

            assertNotNull(userId);

            Long fixtureUserId = userId;

            jdbc.update("""
                    INSERT INTO user_role (
                        tenant_id,
                        user_id,
                        role_id,
                        created_at,
                        updated_at
                    )
                    VALUES (
                        ?, ?, ?,
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP
                    )
                    """,
                    TENANT_ID,
                    fixtureUserId,
                    adminRoleId
            );

            jdbc.update("""
                    INSERT INTO user_company_access (
                        tenant_id,
                        user_id,
                        company_id,
                        created_at,
                        updated_at
                    )
                    VALUES (
                        ?, ?, ?,
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP
                    )
                    """,
                    TENANT_ID,
                    fixtureUserId,
                    originalCompanyId
            );

            AdminUserEditForm form =
                    new AdminUserEditForm();

            form.setId(fixtureUserId);
            form.setFirstName("Dopo");
            form.setLastName("Modificato");
            form.setEmail(newEmail);
            form.setPassword("company-changed-test-only");
            form.setActive(false);
            form.setAdmin(false);

            // La precedente associazione ROLE_ADMIN
            // viene rimossa durante l'aggiornamento.
            form.setRoles(List.of());

            // Prima società valida, seconda inesistente.
            form.setCompanyIds(List.of(
                    originalCompanyId,
                    INVALID_COMPANY_ID
            ));

            BusinessException error = assertThrows(
                    BusinessException.class,
                    () -> adminUserService.updateUser(
                            TENANT_ID, form
                    )
            );

            assertTrue(
                    error.getMessage().contains(
                            "Società non disponibile"
                    ),
                    "Errore inatteso: " + error.getMessage()
            );

            // La transazione del servizio è terminata:
            // controlliamo i dati persistiti tramite JDBC.
            var original = jdbc.queryForMap("""
                    SELECT first_name,
                           last_name,
                           email,
                           password_hash,
                           status
                    FROM app_user
                    WHERE tenant_id = ?
                      AND id = ?
                    """,
                    TENANT_ID,
                    fixtureUserId
            );

            assertEquals("Prima", original.get("first_name"));
            assertEquals("Originale", original.get("last_name"));
            assertEquals(oldEmail, original.get("email"));
            assertEquals(oldHash, original.get("password_hash"));
            assertEquals("ACTIVE", original.get("status"));

            List<Long> remainingRoles = jdbc.queryForList("""
                    SELECT role_id
                    FROM user_role
                    WHERE tenant_id = ?
                      AND user_id = ?
                    ORDER BY role_id
                    """,
                    Long.class,
                    TENANT_ID,
                    fixtureUserId
            );

            assertEquals(
                    List.of(adminRoleId),
                    remainingRoles,
                    "Il ruolo originale non è stato ripristinato"
            );

            List<Long> remainingCompanies =
                    jdbc.queryForList("""
                        SELECT company_id
                        FROM user_company_access
                        WHERE tenant_id = ?
                          AND user_id = ?
                        ORDER BY company_id
                        """,
                        Long.class,
                        TENANT_ID,
                        fixtureUserId
                    );

            assertEquals(
                    List.of(originalCompanyId),
                    remainingCompanies,
                    "Le autorizzazioni societarie sono cambiate"
            );

            System.out.println(
                    "ROLLBACK COMPANY OK: "
                    + "utente, ruoli e società originali."
            );

        } finally {

            // Pulizia limitata all'utente temporaneo.
            if (userId != null) {

                jdbc.update("""
                        DELETE FROM user_company_access
                        WHERE tenant_id = ?
                          AND user_id = ?
                        """,
                        TENANT_ID,
                        userId
                );

                jdbc.update("""
                        DELETE FROM user_role
                        WHERE tenant_id = ?
                          AND user_id = ?
                        """,
                        TENANT_ID,
                        userId
                );

                jdbc.update("""
                        DELETE FROM app_user
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        TENANT_ID,
                        userId
                );
            }

            Integer remaining = jdbc.queryForObject("""
                    SELECT COUNT(*)
                    FROM app_user
                    WHERE tenant_id = ?
                      AND email IN (?, ?)
                    """,
                    Integer.class,
                    TENANT_ID,
                    oldEmail,
                    newEmail
            );

            assertEquals(
                    0,
                    remaining,
                    "Fixture temporanea non rimossa"
            );
        }
    }
}
