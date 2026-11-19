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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(
        named = "GESTIVA_INVENTORY_LOCATION_LOCAL_IT",
        matches = "enabled"
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
class InventoryUnlocatedBalanceLocalMysqlITTest {


    private static final String SCHEMA =
            "gestiva_inventory_location_it";

    private static final String JDBC_URL =
            "jdbc:mysql://127.0.0.1:33309/"
            + SCHEMA
            + "?sslMode=REQUIRED";

    private static final Path WORKSPACE = Path.of(
            "/private/tmp/gestiva_inventory_jpql_it_33309"
    );

    private static String applicationPassword() {
        try {
            Path file = WORKSPACE.resolve("app-password.local");

            if (Files.isSymbolicLink(file)
                    || !Files.isRegularFile(file)
                    || !Files.getPosixFilePermissions(file).equals(
                            PosixFilePermissions.fromString("rw-------"))) {
                throw new IllegalStateException(
                        "STOP: file credenziali applicative non sicuro"
                );
            }

            return Files.readString(file).strip();

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "STOP: credenziali sandbox non disponibili",
                    ex
            );
        }
    }

    private static void verifyIdentity(Connection connection)
            throws Exception {

        String expectedDirectory =
                WORKSPACE.resolve("data").toRealPath().toString();

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT @@datadir, @@port, @@bind_address, "
                     + "@@skip_networking, CURRENT_USER(), "
                     + "DATABASE(), @@version"
             )) {

            if (!result.next()) {
                throw new IllegalStateException(
                        "STOP: identificazione MySQL vuota"
                );
            }

            String actualDirectory = Path.of(
                    result.getString(1)
            ).toRealPath().toString();

            boolean valid =
                    expectedDirectory.equals(actualDirectory)
                    && result.getInt(2) == 33309
                    && "127.0.0.1".equals(result.getString(3))
                    && result.getInt(4) == 0
                    && "gestiva_jpql_it@localhost".equals(
                            result.getString(5))
                    && SCHEMA.equals(result.getString(6))
                    && result.getString(7).startsWith("8.0.");

            if (!valid) {
                throw new IllegalStateException(
                        "STOP: identità MySQL sandbox non conforme"
                );
            }
        }
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry)
            throws Exception {

        if (!"enabled".equals(
                System.getenv("GESTIVA_INVENTORY_LOCATION_LOCAL_IT"))) {
            throw new IllegalStateException(
                    "STOP: abilitazione esplicita assente"
            );
        }

        String password = applicationPassword();

        // Controllo JDBC PRIMA dell'avvio di Flyway.
        try (Connection connection = DriverManager.getConnection(
                JDBC_URL, "gestiva_jpql_it", password)) {
            verifyIdentity(connection);
        }

        registry.add("spring.datasource.url", () -> JDBC_URL);
        registry.add("spring.datasource.username",
                () -> "gestiva_jpql_it");
        registry.add("spring.datasource.password", () -> password);

        registry.add("spring.flyway.url", () -> JDBC_URL);
        registry.add("spring.flyway.user",
                () -> "gestiva_jpql_it");
        registry.add("spring.flyway.password", () -> password);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.flyway.clean-disabled", () -> true);
        registry.add("spring.flyway.baseline-on-migrate",
                () -> false);

        registry.add("spring.jpa.hibernate.ddl-auto",
                () -> "validate");
    }

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private InventoryMovementRepository repository;

    @BeforeEach
    void verifyLocalMysqlConnectionBeforeWriting() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            verifyIdentity(connection);
            assertEquals(SCHEMA, connection.getCatalog());
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
