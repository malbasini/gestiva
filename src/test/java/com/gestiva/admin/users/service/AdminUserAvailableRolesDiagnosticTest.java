package com.gestiva.admin.users.service;

import com.gestiva.support.AbstractMySqlIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class AdminUserAvailableRolesDiagnosticTest
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void inspectAvailableRoles() throws Exception {

        try (Connection c =
                     jdbc.getDataSource().getConnection()) {

            assertEquals("gestiva_test", c.getCatalog());

            String account =
                    c.getMetaData().getUserName();

            assertTrue(
                    account.equals("gestiva_test_runner")
                    || account.startsWith("gestiva_test_runner@")
            );
        }

        System.out.println(
            "\n========== RUOLI TENANT 2 =========="
        );

        List<String> codes = jdbc.queryForList("""
                SELECT code
                FROM role
                WHERE tenant_id = 2
                ORDER BY code
                """, String.class);

        for (String code : codes) {
            System.out.println("PRESENTE: " + code);
        }

        Set<String> present = new HashSet<>(codes);

        System.out.println(
            "\n========== CONFRONTO CON LA UI =========="
        );

        for (String expected :
                AdminUserService.AVAILABLE_ROLES) {

            if (present.contains(expected)) {
                System.out.println("OK: " + expected);
            } else {
                System.out.println("MANCANTE: " + expected);
            }
        }

        System.out.println(
            "\nDIAGNOSTICA COMPLETATA - SOLO LETTURA"
        );
    }
}
