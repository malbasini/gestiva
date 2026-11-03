package com.gestiva.admin.users.service;

import com.gestiva.admin.users.web.AdminUserEditForm;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AdminUserCompanyPersistenceIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;

    @Autowired
    private AdminUserService service;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void createAndUpdateShouldPersistCompanyAccess()
            throws Exception {

        // Nessuna scrittura prima della verifica del DB.
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
                "Serve almeno una società attiva di test"
        );

        Long companyId = companies.get(0);

        String marker = UUID.randomUUID()
                .toString()
                .replace("-", "");

        String email =
                "company-persist-" + marker + "@example.invalid";

        Long userId = null;

        try {
            AdminUserEditForm create =
                    new AdminUserEditForm();

            create.setFirstName("Test");
            create.setLastName("Originale");
            create.setEmail(email);
            create.setPassword("PasswordTest123!");
            create.setActive(true);
            create.setAdmin(false);
            create.setRoles(List.of());
            create.setCompanyIds(List.of(companyId));

            // Operazione reale, tramite servizio Spring.
            service.createUser(TENANT, create);

            userId = jdbc.queryForObject("""
                    SELECT id
                    FROM app_user
                    WHERE tenant_id = ?
                      AND email = ?
                    """,
                    Long.class,
                    TENANT,
                    email
            );

            assertNotNull(userId);

            Integer initialAccess = jdbc.queryForObject("""
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

            assertEquals(
                    1,
                    initialAccess,
                    "L'autorizzazione iniziale deve esistere"
            );

            AdminUserEditForm edit =
                    service.loadEditForm(TENANT, userId);

            assertEquals(
                    List.of(companyId),
                    edit.getCompanyIds(),
                    "Il form deve precaricare la società"
            );

            edit.setLastName("Aggiornato");

            // Seconda operazione reale sullo stesso utente.
            service.updateUser(TENANT, edit);

            String lastName = jdbc.queryForObject("""
                    SELECT last_name
                    FROM app_user
                    WHERE tenant_id = ?
                      AND id = ?
                    """,
                    String.class,
                    TENANT,
                    userId
            );

            assertEquals("Aggiornato", lastName);

            Integer accessAfterUpdate =
                    jdbc.queryForObject("""
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

            assertEquals(
                    1,
                    accessAfterUpdate,
                    "La modifica deve preservare la società"
            );

            Integer allAccesses =
                    jdbc.queryForObject("""
                        SELECT COUNT(*)
                        FROM user_company_access
                        WHERE tenant_id = ?
                          AND user_id = ?
                        """,
                        Integer.class,
                        TENANT,
                        userId
                    );

            assertEquals(
                    1,
                    allAccesses,
                    "Non devono esistere assegnazioni duplicate"
            );

            System.out.println(
                    "PERSISTENZA COMPANY OK: "
                    + "creazione, modifica e accesso verificati."
            );

        } finally {

            // Recupero difensivo in caso di eccezione
            // dopo la creazione ma prima della lettura ID.
            if (userId == null) {
                List<Long> ids = jdbc.queryForList("""
                        SELECT id
                        FROM app_user
                        WHERE tenant_id = ?
                          AND email = ?
                        """,
                        Long.class,
                        TENANT,
                        email
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
                            AND email = ?
                        """,
                        TENANT,
                        userId,
                        email
                );
            }

            Integer leftovers = jdbc.queryForObject("""
                    SELECT COUNT(*)
                    FROM app_user
                    WHERE tenant_id = ?
                      AND email = ?
                    """,
                    Integer.class,
                    TENANT,
                    email
            );

            assertEquals(
                    0,
                    leftovers,
                    "Fixture temporanea non rimossa"
            );
        }
    }
}
