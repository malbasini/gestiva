package com.gestiva.admin.users.service;

import com.gestiva.admin.users.web.AdminUserEditForm;
import com.gestiva.security.role.entity.UserRole;
import com.gestiva.security.role.repository.RoleRepository;
import com.gestiva.security.role.repository.UserRoleRepository;
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

class AdminUserRollbackIT extends AbstractMySqlIntegrationTest {

    private static final Long TENANT_ID = 2L;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @MockitoSpyBean
    private UserRoleRepository userRoleRepository;

    @Test
    void updateUserShouldRollbackAllChangesWhenRoleSaveFails()
            throws Exception {

        // Protezione aggiuntiva prima di qualsiasi INSERT.
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

        // Deve esistere un ruolo admin per il tenant.
        // Non creiamo e non modifichiamo ruoli reali.
        Long adminRoleId = roleRepository
                .findByTenantIdAndCode(
                        TENANT_ID, "ROLE_ADMIN"
                )
                .orElseThrow(() -> new IllegalStateException(
                        "Fixture impossibile: ROLE_ADMIN assente "
                        + "nel tenant di test"
                ))
                .getId();

        String marker = UUID.randomUUID()
                .toString()
                .replace("-", "");

        String oldEmail =
                "rbk-" + marker + "@example.invalid";

        String newEmail =
                "changed-" + marker + "@example.invalid";

        String oldHash =
                passwordEncoder.encode("original-test-only");

        Long userId = null;

        try {
            jdbc.update("""
                INSERT INTO app_user (
                    tenant_id, first_name, last_name,
                    email, password_hash, status,
                    admin, locale_code,
                    created_at, updated_at
                )
                VALUES (
                    ?, 'Prima', 'Originale',
                    ?, ?, 'ACTIVE',
                    0, 'it',
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

            // Associazione iniziale autonoma della fixture.
            jdbc.update("""
                INSERT INTO user_role (
                    tenant_id, user_id, role_id,
                    created_at, updated_at
                )
                VALUES (
                    ?, ?, ?,
                    CURRENT_TIMESTAMP,
                    CURRENT_TIMESTAMP
                )
                """,
                TENANT_ID,
                userId,
                adminRoleId
            );

            Long fixtureUserId = userId;

            AtomicBoolean failureReached =
                    new AtomicBoolean(false);

            // Il repository è uno spy: tutte le altre
            // operazioni di persistenza rimangono reali.
            doAnswer(invocation -> {

                // Forziamo l'esecuzione SQL delle
                // modifiche prima dell'errore simulato.
                entityManager.flush();

                String changedName =
                        jdbc.queryForObject("""
                            SELECT first_name
                            FROM app_user
                            WHERE tenant_id = ?
                              AND id = ?
                            """,
                            String.class,
                            TENANT_ID,
                            fixtureUserId
                        );

                assertEquals("Dopo", changedName);

                Integer previousRoles =
                        jdbc.queryForObject("""
                            SELECT COUNT(*)
                            FROM user_role
                            WHERE tenant_id = ?
                              AND user_id = ?
                            """,
                            Integer.class,
                            TENANT_ID,
                            fixtureUserId
                        );

                assertEquals(
                        0,
                        previousRoles,
                        "Le associazioni precedenti "
                        + "devono essere già state eliminate"
                );

                failureReached.set(true);

                throw new IllegalStateException(
                        "IT: errore simulato salvataggio ruolo"
                );

            }).when(userRoleRepository)
              .save(any(UserRole.class));

            AdminUserEditForm form =
                    new AdminUserEditForm();

            form.setId(fixtureUserId);
            form.setFirstName("Dopo");
            form.setLastName("Modificato");
            form.setEmail(newEmail);
            form.setPassword("changed-test-only");
            form.setActive(false);
            form.setAdmin(true);
            form.setRoles(List.of());

            IllegalStateException error =
                    assertThrows(
                            IllegalStateException.class,
                            () -> adminUserService.updateUser(
                                    TENANT_ID, form
                            )
                    );

            assertEquals(
                    "IT: errore simulato salvataggio ruolo",
                    error.getMessage()
            );

            assertTrue(
                    failureReached.get(),
                    "Errore non raggiunto dopo il flush SQL"
            );

            // Lettura dopo la transazione fallita:
            // JdbcTemplate apre una nuova transazione
            // o connessione indipendente dal servizio.
            var original = jdbc.queryForMap("""
                SELECT
                    first_name,
                    last_name,
                    email,
                    password_hash,
                    status,
                    admin
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

            Object adminValue = original.get("admin");

            // MySQL JDBC può restituire BIT/BOOLEAN come
            // Boolean oppure come valore numerico.
            boolean adminAfterRollback;

            if (adminValue instanceof Boolean value) {
                adminAfterRollback = value;
            } else if (adminValue instanceof Number value) {
                adminAfterRollback = value.intValue() != 0;
            } else {
                throw new AssertionError(
                        "Tipo JDBC inatteso per admin: "
                        + (adminValue == null
                                ? "null"
                                : adminValue.getClass().getName())
                );
            }

            assertFalse(
                    adminAfterRollback,
                    "Il flag admin non è stato ripristinato"
            );

            Long remainingRole = jdbc.queryForObject("""
                SELECT role_id
                FROM user_role
                WHERE tenant_id = ?
                  AND user_id = ?
                """,
                Long.class,
                TENANT_ID,
                fixtureUserId
            );

            assertEquals(adminRoleId, remainingRole);

            Integer roleCount = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM user_role
                WHERE tenant_id = ?
                  AND user_id = ?
                """,
                Integer.class,
                TENANT_ID,
                fixtureUserId
            );

            assertEquals(1, roleCount);

            System.out.println(
                    "ROLLBACK UTENTI OK: dati e ruolo originali."
            );

        } finally {

            // Cancelliamo solamente la fixture del test,
            // mai utenti o associazioni preesistenti.
            if (userId != null) {

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

            Integer leftovers = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM app_user
                WHERE tenant_id = ?
                  AND (email = ? OR email = ?)
                """,
                Integer.class,
                TENANT_ID,
                oldEmail,
                newEmail
            );

            assertEquals(
                    0,
                    leftovers,
                    "Fixture utente non completamente rimossa"
            );
        }
    }
}
