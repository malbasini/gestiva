package com.gestiva.security.role.service;

import com.gestiva.security.role.entity.Role;
import com.gestiva.security.role.repository.RoleRepository;
import com.gestiva.security.tenant.entity.Tenant;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class StandardRoleBootstrapIntegrationTest
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private StandardRoleBootstrapService bootstrap;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @Transactional
    @Rollback
    void shouldPersistFiveRolesOnlyOnceAndPreserveExistingData()
            throws Exception {

        // Protezione aggiuntiva prima di creare la fixture.
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

        String marker = UUID.randomUUID()
                .toString()
                .replace("-", "");

        // Tenant autonomo e temporaneo.
        // @Rollback annulla l'intera transazione di test.
        Tenant tenant = new Tenant();

        tenant.setName("IT Ruoli " + marker);
        tenant.setSlug("it-roles-" + marker);
        tenant.setEmail(
                "it-roles-" + marker + "@example.invalid"
        );
        tenant.setStatus("ACTIVE");
        tenant.setDefaultLocale("it");
        tenant.setDefaultCurrency("EUR");
        tenant.setSubscriptionActive(false);
        tenant.setSubscriptionStatus("PENDING");

        Tenant savedTenant =
                tenantRepository.saveAndFlush(tenant);

        Long tenantId = savedTenant.getId();

        assertNotNull(tenantId);

        // Prima inizializzazione.
        bootstrap.initializeForTenant(tenantId);
        roleRepository.flush();

        List<Role> first = roleRepository
                .findByTenantId(tenantId);

        assertEquals(5, first.size());

        Set<String> expected = Set.of(
                "ROLE_ADMIN",
                "ROLE_SALES",
                "ROLE_PURCHASING",
                "ROLE_WAREHOUSE",
                "ROLE_ACCOUNTING"
        );

        assertEquals(
                expected,
                first.stream()
                        .map(Role::getCode)
                        .collect(Collectors.toSet())
        );

        assertTrue(
                first.stream().allMatch(role ->
                        tenantId.equals(role.getTenantId())
                )
        );

        // Personalizziamo il nome di un ruolo esistente.
        Role sales = roleRepository
                .findByTenantIdAndCode(
                        tenantId, "ROLE_SALES"
                )
                .orElseThrow();

        Long originalSalesId = sales.getId();

        sales.setName("Vendite personalizzate");

        roleRepository.saveAndFlush(sales);

        // Seconda inizializzazione:
        // nessun duplicato e nessuna sovrascrittura.
        bootstrap.initializeForTenant(tenantId);
        roleRepository.flush();

        List<Role> second = roleRepository
                .findByTenantId(tenantId);

        assertEquals(
                5,
                second.size(),
                "Bootstrap non idempotente"
        );

        Role salesAfter = roleRepository
                .findByTenantIdAndCode(
                        tenantId, "ROLE_SALES"
                )
                .orElseThrow();

        assertEquals(
                originalSalesId,
                salesAfter.getId()
        );

        assertEquals(
                "Vendite personalizzate",
                salesAfter.getName(),
                "Il bootstrap ha sovrascritto un ruolo esistente"
        );

        Long persistedCount = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM role
                WHERE tenant_id = ?
                """,
                Long.class,
                tenantId
        );

        assertEquals(5L, persistedCount);

        System.out.println(
                "BOOTSTRAP MYSQL OK: cinque ruoli, "
                + "nessun duplicato, dati esistenti preservati."
        );

        // Il rollback automatico del test elimina
        // il tenant e tutti i suoi cinque ruoli.
    }
}
