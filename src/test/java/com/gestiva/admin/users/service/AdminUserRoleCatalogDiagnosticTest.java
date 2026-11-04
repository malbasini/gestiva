package com.gestiva.admin.users.service;

import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class AdminUserRoleCatalogDiagnosticTest
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void inspectTenantRoleCatalog() throws Exception {

        try (Connection c =
                     jdbc.getDataSource().getConnection()) {

            assertEquals("gestiva_test", c.getCatalog());

            String username =
                    c.getMetaData().getUserName();

            assertTrue(
                    username.equals("gestiva_test_runner")
                    || username.startsWith("gestiva_test_runner@")
            );
        }

        System.out.println(
            "\n========== CATALOGO RUOLI TENANT 2 =========="
        );

        jdbc.query("""
                SELECT id, code, name
                FROM role
                WHERE tenant_id = 2
                ORDER BY id
                """,
                rs -> {
                    System.out.printf(
                        "ID=%d | CODE=%s | NAME=%s%n",
                        rs.getLong("id"),
                        rs.getString("code"),
                        rs.getString("name")
                    );
                }
        );

        System.out.println(
            "\n========== ASSOCIAZIONI RUOLI TENANT 2 =========="
        );

        jdbc.query("""
                SELECT r.code, COUNT(*) AS total
                FROM user_role ur
                JOIN role r
                  ON r.id = ur.role_id
                 AND r.tenant_id = ur.tenant_id
                WHERE ur.tenant_id = 2
                GROUP BY r.code
                ORDER BY r.code
                """,
                rs -> {
                    System.out.printf(
                        "ROLE=%s | ASSEGNAZIONI=%d%n",
                        rs.getString("code"),
                        rs.getInt("total")
                    );
                }
        );

        System.out.println(
            "\n========== DISTRIBUZIONE RUOLI PER TENANT =========="
        );

        jdbc.query("""
                SELECT tenant_id, COUNT(*) AS total
                FROM role
                GROUP BY tenant_id
                ORDER BY tenant_id
                """,
                rs -> {
                    System.out.printf(
                        "TENANT=%s | RUOLI=%d%n",
                        rs.getObject("tenant_id"),
                        rs.getInt("total")
                    );
                }
        );

        System.out.println(
            "\nDIAGNOSTICA RUOLI COMPLETATA - SOLO LETTURA"
        );
    }
}
