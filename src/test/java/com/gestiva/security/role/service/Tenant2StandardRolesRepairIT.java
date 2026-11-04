package com.gestiva.security.role.service;

import com.gestiva.security.role.entity.Role;
import com.gestiva.security.role.repository.RoleRepository;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class Tenant2StandardRolesRepairIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;

    private static final Set<String> EXPECTED = Set.of(
            "ROLE_ADMIN",
            "ROLE_SALES",
            "ROLE_PURCHASING",
            "ROLE_WAREHOUSE",
            "ROLE_ACCOUNTING"
    );

    @Autowired
    private StandardRoleBootstrapService bootstrap;

    @Autowired
    private RoleRepository roles;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void initializeMissingRolesWithoutChangingExistingAdmin()
            throws Exception {

        // Sicurezza: esclusivamente DB e account test.
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

        List<Role> before = roles.findByTenantId(TENANT);

        // Stato esatto verificato nel PASSO 1786.
        assertEquals(
                1,
                before.size(),
                "STOP: il catalogo del tenant 2 è cambiato"
        );

        Role admin = before.get(0);

        assertEquals("ROLE_ADMIN", admin.getCode());
        assertEquals(20L, admin.getId());
        assertEquals(TENANT, admin.getTenantId());

        String adminName = admin.getName();
        boolean adminSystemRole = admin.isSystemRole();

        Integer assignmentsBefore =
                jdbc.queryForObject("""
                    SELECT COUNT(*)
                    FROM user_role
                    WHERE tenant_id = ?
                      AND role_id = ?
                    """,
                    Integer.class,
                    TENANT,
                    admin.getId()
                );

        long tenant0Before = roles.findByTenantId(0L).size();

        assertEquals(
                4L,
                tenant0Before,
                "STOP: catalogo tenant 0 cambiato"
        );

        System.out.println(
                "PRECHECK OK: ROLE_ADMIN ID 20 preservabile."
        );

        // Prima esecuzione: crea i quattro ruoli mancanti.
        bootstrap.initializeForTenant(TENANT);

        List<Role> after = roles.findByTenantId(TENANT);

        assertEquals(5, after.size());

        Map<String, Role> byCode = after.stream()
                .collect(Collectors.toMap(
                        Role::getCode,
                        role -> role
                ));

        assertEquals(EXPECTED, byCode.keySet());

        Role savedAdmin = byCode.get("ROLE_ADMIN");

        assertEquals(20L, savedAdmin.getId());
        assertEquals(adminName, savedAdmin.getName());
        assertEquals(
                adminSystemRole,
                savedAdmin.isSystemRole()
        );

        for (String code : EXPECTED) {
            assertEquals(
                    TENANT,
                    byCode.get(code).getTenantId()
            );

            assertTrue(
                    byCode.get(code).isSystemRole(),
                    "Ruolo standard non marcato come system: "
                    + code
            );
        }

        Map<String, Long> idsBeforeSecondRun =
                byCode.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().getId()
                        ));

        // Seconda esecuzione: non deve creare duplicati.
        bootstrap.initializeForTenant(TENANT);

        List<Role> finalRoles =
                roles.findByTenantId(TENANT);

        assertEquals(
                5,
                finalRoles.size(),
                "Il secondo bootstrap non deve duplicare ruoli"
        );

        for (Role role : finalRoles) {
            assertEquals(
                    idsBeforeSecondRun.get(role.getCode()),
                    role.getId(),
                    "ID modificato per " + role.getCode()
            );
        }

        Integer assignmentsAfter =
                jdbc.queryForObject("""
                    SELECT COUNT(*)
                    FROM user_role
                    WHERE tenant_id = ?
                      AND role_id = ?
                    """,
                    Integer.class,
                    TENANT,
                    20L
                );

        assertEquals(
                assignmentsBefore,
                assignmentsAfter,
                "Le assegnazioni admin devono restare invariate"
        );

        assertEquals(
                tenant0Before,
                roles.findByTenantId(0L).size(),
                "Il tenant 0 non deve essere modificato"
        );

        System.out.println(
                "BOOTSTRAP TENANT 2 OK: cinque ruoli standard."
        );

        System.out.println(
                "ID ROLE_ADMIN 20 conservato."
        );

        System.out.println(
                "Idempotenza verificata: nessun duplicato."
        );

        System.out.println(
                "Tenant 0 e associazioni admin invariati."
        );
    }
}
