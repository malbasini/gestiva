package com.gestiva.admin.users.service;

import com.gestiva.admin.users.web.AdminUserEditForm;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AdminUserCompanyRoleReplacementIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;

    @Autowired
    private AdminUserService service;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void changingCompaniesShouldPreserveExistingRoles()
            throws Exception {

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

        List<Long> companies = jdbc.queryForList("""
                SELECT id
                FROM company
                WHERE tenant_id = ?
                  AND active = 1
                ORDER BY id
                LIMIT 3
                """,
                Long.class,
                TENANT
        );

        assertTrue(
                companies.size() >= 3,
                "Servono tre società attive di test"
        );

        Long companyA = companies.get(0);
        Long companyB = companies.get(1);
        Long companyC = companies.get(2);

        List<Long> roleIds = jdbc.queryForList("""
                SELECT id
                FROM role
                WHERE tenant_id = ?
                  AND code IN (
                      'ROLE_SALES',
                      'ROLE_WAREHOUSE'
                  )
                ORDER BY id
                """,
                Long.class,
                TENANT
        );

        assertEquals(2, roleIds.size());

        String marker = UUID.randomUUID()
                .toString().replace("-", "");

        String email =
                "role-replacement-" + marker
                + "@example.invalid";

        Long userId = null;

        try {
            AdminUserEditForm create =
                    new AdminUserEditForm();

            create.setFirstName("Collaudo");
            create.setLastName("Ruoli");
            create.setEmail(email);
            create.setPassword("TestPassword1796!");
            create.setActive(true);
            create.setAdmin(false);
            create.setRoles(List.of(
                    "ROLE_SALES",
                    "ROLE_WAREHOUSE"
            ));
            create.setCompanyIds(List.of(
                    companyA,
                    companyB
            ));

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

            AdminUserEditForm edit =
                    service.loadEditForm(TENANT, userId);

            assertEquals(
                    Set.of(
                        "ROLE_SALES",
                        "ROLE_WAREHOUSE"
                    ),
                    new HashSet<>(edit.getRoles())
            );

            // Manteniamo A, revochiamo B, aggiungiamo C.
            edit.setCompanyIds(List.of(
                    companyA,
                    companyC
            ));

            // Ruoli invariati: riproduce il caso HTTP.
            service.updateUser(TENANT, edit);

            List<Long> finalCompanies =
                    jdbc.queryForList("""
                        SELECT company_id
                        FROM user_company_access
                        WHERE tenant_id = ?
                          AND user_id = ?
                        ORDER BY company_id
                        """,
                        Long.class,
                        TENANT,
                        userId
                    );

            assertEquals(
                    Set.of(companyA, companyC),
                    new HashSet<>(finalCompanies)
            );

            assertEquals(
                    2,
                    finalCompanies.size(),
                    "Nessuna società duplicata"
            );

            List<Long> finalRoles =
                    jdbc.queryForList("""
                        SELECT role_id
                        FROM user_role
                        WHERE tenant_id = ?
                          AND user_id = ?
                        ORDER BY role_id
                        """,
                        Long.class,
                        TENANT,
                        userId
                    );

            assertEquals(
                    new HashSet<>(roleIds),
                    new HashSet<>(finalRoles)
            );

            assertEquals(
                    2,
                    finalRoles.size(),
                    "Nessun ruolo duplicato"
            );

            System.out.println(
                    "UPDATE MULTICOMPANY OK: società A+C."
            );

            System.out.println(
                    "RUOLI INVARIATI OK: SALES + WAREHOUSE."
            );

            System.out.println(
                    "VINCOLO UNICITÀ USER_ROLE OK."
            );

        } finally {

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

            Integer leftovers =
                    jdbc.queryForObject("""
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
