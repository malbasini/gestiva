package com.gestiva.platform.company.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(
        named = "GESTIVA_BOOTSTRAP_IT",
        matches = "enabled"
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CompanyBootstrapIntegrationTest {

    private static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>("mysql:8.0.45")
                    .withDatabaseName("gestiva_bootstrap_it")
                    .withUsername("bootstrap_test")
                    .withPassword("bootstrap_test_password");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {

        MYSQL.start();

        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);

        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Test
    void containerShouldBeAvailable() {
        assertTrue(MYSQL.isRunning());
    }
}
