package com.gestiva.admin.users.service;

import com.gestiva.admin.users.web.AdminUserEditForm;
import com.gestiva.security.company.entity.UserCompanyAccess;
import com.gestiva.security.company.repository.UserCompanyAccessRepository;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminUserCompanyWriteRollbackIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;

    @Autowired
    private AdminUserService service;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @MockitoSpyBean
    private UserCompanyAccessRepository accessRepository;

    @Test
    void companySaveFailureShouldRollbackUserRolesAndAccess()
            throws Exception {

        // Verifica di sicurezza prima di qualsiasi INSERT.
        try (Connection connection =
                     jdbc.getDataSource().getConnection()) {

            assertEquals(
                    "gestiva_test",
                    connection.getCatalog()
            );

            String username =
                    connection.getMetaData().getUserName();

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
                TENANT
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
                TENANT
        );

        assertFalse(
                companies.isEmpty(),
                "Nessuna società attiva nel tenant di test"
        );

        Long companyId = companies.get(0);

        String marker = UUID.randomUUID()
                .toString()
                .replace("-", "");

        String oldEmail =
                "write-rbk-" + marker + "@example.invalid";

        String newEmail =
                "write-changed-" + marker + "@example.invalid";

        String originalHash =
                passwordEncoder.encode("original-test-only");

        Long userId = null;

        try {
            // Fixture completamente autonoma.
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
                    TENANT,
                    oldEmail,
                    originalHash
            );

            userId = jdbc.queryForObject("""
                    SELECT id
                    FROM app_user
                    WHERE tenant_id = ?
                      AND email = ?
                    """,
                    Long.class,
                    TENANT,
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
                    TENANT,
                    fixtureUserId,
                    adminRoleId
            );

            Integer initialAccessCount =
                    jdbc.queryForObject("""
                        SELECT COUNT(*)
                        FROM user_company_access
                        WHERE tenant_id = ?
                          AND user_id = ?
                        """,
                        Integer.class,
                        TENANT,
                        fixtureUserId
                    );

            assertEquals(0, initialAccessCount);

            AtomicBoolean failureReached =
                    new AtomicBoolean(false);

            // Intercettiamo esclusivamente il salvataggio
            // della nuova autorizzazione societaria.
            doAnswer(invocation -> {

                UserCompanyAccess attempted =
                        invocation.getArgument(0);

                assertEquals(TENANT, attempted.getTenantId());
                assertEquals(fixtureUserId, attempted.getUserId());
                assertEquals(companyId, attempted.getCompanyId());

                // Le modifiche precedenti vengono inviate
                // realmente a MySQL nella transazione corrente.
                entityManager.flush();

                String changedName =
                        jdbc.queryForObject("""
                            SELECT first_name
                            FROM app_user
                            WHERE tenant_id = ?
                              AND id = ?
                            """,
                            String.class,
                            TENANT,
                            fixtureUserId
                        );

                assertEquals(
                        "Dopo",
                        changedName,
                        "Il nuovo nome deve essere già scritto "
                        + "nella transazione prima del guasto"
                );

                Integer rolesDuringUpdate =
                        jdbc.queryForObject("""
                            SELECT COUNT(*)
                            FROM user_role
                            WHERE tenant_id = ?
                              AND user_id = ?
                            """,
                            Integer.class,
                            TENANT,
                            fixtureUserId
                        );

                assertEquals(
                        0,
                        rolesDuringUpdate,
                        "I vecchi ruoli devono essere stati "
                        + "rimossi nella transazione corrente"
                );

                failureReached.set(true);

                throw new IllegalStateException(
                        "IT: errore simulato salvataggio società"
                );

            }).when(accessRepository)
              .save(any(UserCompanyAccess.class));

            AdminUserEditForm form =
                    new AdminUserEditForm();

            form.setId(fixtureUserId);
            form.setFirstName("Dopo");
            form.setLastName("Modificato");
            form.setEmail(newEmail);
            form.setPassword("nuova-password-test");
            form.setActive(false);
            form.setAdmin(false);
            form.setRoles(List.of());
            form.setCompanyIds(List.of(companyId));

            IllegalStateException error = assertThrows(
                    IllegalStateException.class,
                    () -> service.updateUser(TENANT, form)
            );

            assertEquals(
                    "IT: errore simulato salvataggio società",
                    error.getMessage()
            );

            assertTrue(
                    failureReached.get(),
                    "Il guasto deve avvenire durante "
                    + "il salvataggio della società"
            );

            // L'operazione Spring è terminata.
            // Verifichiamo lo stato persistito.
            var original = jdbc.queryForMap("""
                    SELECT first_name,
                           last_name,
                           email,
                           password_hash,
                           status,
                           admin
                    FROM app_user
                    WHERE tenant_id = ?
                      AND id = ?
                    """,
                    TENANT,
                    fixtureUserId
            );

            assertEquals("Prima", original.get("first_name"));
            assertEquals("Originale", original.get("last_name"));
            assertEquals(oldEmail, original.get("email"));
            assertEquals(
                    originalHash,
                    original.get("password_hash")
            );
            assertEquals("ACTIVE", original.get("status"));

            Object adminValue = original.get("admin");

            if (adminValue instanceof Boolean value) {
                assertFalse(value);
            } else if (adminValue instanceof Number value) {
                assertEquals(0, value.intValue());
            } else {
                fail(
                    "Tipo JDBC inatteso per admin: "
                    + (adminValue == null
                        ? "null"
                        : adminValue.getClass().getName())
                );
            }

            List<Long> remainingRoles =
                    jdbc.queryForList("""
                        SELECT role_id
                        FROM user_role
                        WHERE tenant_id = ?
                          AND user_id = ?
                        ORDER BY role_id
                        """,
                        Long.class,
                        TENANT,
                        fixtureUserId
                    );

            assertEquals(
                    List.of(adminRoleId),
                    remainingRoles,
                    "Il ruolo originale deve essere ripristinato"
            );

            Integer remainingAccesses =
                    jdbc.queryForObject("""
                        SELECT COUNT(*)
                        FROM user_company_access
                        WHERE tenant_id = ?
                          AND user_id = ?
                        """,
                        Integer.class,
                        TENANT,
                        fixtureUserId
                    );

            assertEquals(
                    0,
                    remainingAccesses,
                    "Nessuna nuova autorizzazione deve "
                    + "rimanere dopo il rollback"
            );

            System.out.println(
                    "ROLLBACK COMPANY WRITE OK: "
                    + "utente, ruoli e società ripristinati."
            );

        } finally {

            // Rimuoviamo lo spy prima delle eventuali
            // operazioni di pulizia della fixture.
            reset(accessRepository);

            if (userId == null) {
                List<Long> ids = jdbc.queryForList("""
                        SELECT id
                        FROM app_user
                        WHERE tenant_id = ?
                          AND email IN (?, ?)
                        """,
                        Long.class,
                        TENANT,
                        oldEmail,
                        newEmail
                );

                if (ids.size() == 1) {
                    userId = ids.get(0);
                }
            }

            if (userId != null) {

                jdbc.update("""
                        DELETE FROM user_company_access
                        WHERE tenant_id = ?
                          AND user_id = ?
                        """,
                        TENANT,
                        userId
                );

                jdbc.update("""
                        DELETE FROM user_role
                        WHERE tenant_id = ?
                          AND user_id = ?
                        """,
                        TENANT,
                        userId
                );

                jdbc.update("""
                        DELETE FROM app_user
                        WHERE tenant_id = ?
                          AND id = ?
                            AND email IN (?, ?)
                        """,
                        TENANT,
                        userId,
                        oldEmail,
                        newEmail
                );
            }

            Integer leftovers = jdbc.queryForObject("""
                    SELECT COUNT(*)
                    FROM app_user
                    WHERE tenant_id = ?
                      AND email IN (?, ?)
                    """,
                    Integer.class,
                    TENANT,
                    oldEmail,
                    newEmail
            );

            assertEquals(
                    0,
                    leftovers,
                    "Fixture temporanea non rimossa"
            );
        }
    }
}
