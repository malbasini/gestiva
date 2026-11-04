package com.gestiva.admin.users.service;

import com.gestiva.support.AbstractMySqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class AdminUserGlobalRolesDiagnosticTest
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void inspectGlobalAndTenantRoles() throws Exception {

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

        System.out.println(
            "\n========== RUOLI TENANT 0 =========="
        );

        jdbc.query("""
                SELECT id, tenant_id, code, name
                FROM role
                WHERE tenant_id = 0
                ORDER BY id
                """,
                (org.springframework.jdbc.core.RowCallbackHandler) rs -> System.out.printf(
                    "ID=%d | TENANT=%d | CODE=%s | NAME=%s%n",
                    rs.getLong("id"),
                    rs.getLong("tenant_id"),
                    rs.getString("code"),
                    rs.getString("name")
                )
        );

        System.out.println(
            "\n========== RUOLI TENANT 2 =========="
        );

        jdbc.query("""
                SELECT id, tenant_id, code, name
                FROM role
                WHERE tenant_id = 2
                ORDER BY id
                """,
                (org.springframework.jdbc.core.RowCallbackHandler) rs -> System.out.printf(
                    "ID=%d | TENANT=%d | CODE=%s | NAME=%s%n",
                    rs.getLong("id"),
                    rs.getLong("tenant_id"),
                    rs.getString("code"),
                    rs.getString("name")
                )
        );

        System.out.println(
            "\n========== DIAGNOSTICA COMPLETATA =========="
        );
    }
}
