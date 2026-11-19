package com.gestiva.warehouse.inventory.movement.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(
        named = "GESTIVA_INVENTORY_LOCATION_IT",
        matches = "enabled"
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class InventoryUnlocatedBalanceSandboxITTest {

    private static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>("mysql:8.0.45")
                    .withDatabaseName("gestiva_inventory_location_it")
                    .withUsername("inventory_it_runner")
                    .withPassword("inventory_it_local_only");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        MYSQL.start();

        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);

        // Anche Flyway deve utilizzare esclusivamente il container.
        // Le migrazioni possono partire prima dei metodi @BeforeEach.
        registry.add("spring.flyway.url", MYSQL::getJdbcUrl);
        registry.add("spring.flyway.user", MYSQL::getUsername);
        registry.add("spring.flyway.password", MYSQL::getPassword);
        registry.add("spring.flyway.enabled", () -> true);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private InventoryMovementRepository repository;

    @BeforeEach
    void verifyContainerConnectionBeforeWriting() throws Exception {
        assertTrue(MYSQL.isRunning(), "Container MySQL non avviato");

        try (Connection connection = dataSource.getConnection()) {
            String actualUrl = connection.getMetaData().getURL();
            String expectedUrl = MYSQL.getJdbcUrl();

            assertEquals(
                    expectedUrl,
                    actualUrl,
                    "STOP: connessione JDBC diversa dal container"
            );

            assertEquals(
                    "gestiva_inventory_location_it",
                    connection.getCatalog(),
                    "STOP: schema JDBC inatteso"
            );
        }
    }

    private void insertMovement(
            long tenant,
            long company,
            Long location,
            String type,
            String qty
    ) {
        jdbc.update(
                """
                INSERT INTO inventory_movement (
                    tenant_id, company_id, warehouse_id, item_id,
                    location_id, movement_date, movement_type,
                    causal_code, quantity, reversed,
                    created_at, updated_at
                ) VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, 0,
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                )
                """,
                tenant,
                company,
                900010L,
                900020L,
                location,
                LocalDate.of(2026, 11, 19),
                type,
                "INTEGRATION_TEST",
                new BigDecimal(qty)
        );
    }

    private void assertQty(BigDecimal expected, BigDecimal actual) {
        assertNotNull(actual);
        assertEquals(0, expected.compareTo(actual));
    }

    @Test
    void unlocatedQueryMustExcludeLocationsCompaniesAndTenants() {

        insertMovement(2L, 3L, null, "IN", "8.000");
        insertMovement(2L, 3L, null, "OUT", "2.000");

        insertMovement(2L, 3L, 101L, "IN", "5.000");
        insertMovement(2L, 3L, 102L, "IN", "3.000");

        insertMovement(2L, 4L, null, "IN", "11.000");
        insertMovement(9L, 3L, null, "IN", "13.000");

        assertQty(
                new BigDecimal("6.000"),
                repository.calculateUnlocatedInventoryBalanceByCompanyAndWarehouse(
                        2L, 3L, 900010L, 900020L
                )
        );

        assertQty(
                new BigDecimal("14.000"),
                repository.calculateInventoryBalanceByCompanyAndWarehouse(
                        2L, 3L, 900010L, 900020L, null
                )
        );

        assertQty(
                new BigDecimal("5.000"),
                repository.calculateInventoryBalanceByCompanyAndWarehouse(
                        2L, 3L, 900010L, 900020L, 101L
                )
        );

        assertQty(
                new BigDecimal("11.000"),
                repository.calculateUnlocatedInventoryBalanceByCompanyAndWarehouse(
                        2L, 4L, 900010L, 900020L
                )
        );

        assertQty(
                new BigDecimal("13.000"),
                repository.calculateUnlocatedInventoryBalanceByCompanyAndWarehouse(
                        9L, 3L, 900010L, 900020L
                )
        );
    }
}
