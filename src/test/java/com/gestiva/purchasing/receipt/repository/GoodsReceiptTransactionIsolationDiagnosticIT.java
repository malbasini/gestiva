package com.gestiva.purchasing.receipt.repository;

import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptTransactionIsolationDiagnosticIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private void verifyDatabase() throws Exception {

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
                    "Account MySQL non autorizzato."
            );
        }
    }

    @Test
    void inspectMysqlIsolationAndAutocommit() throws Exception {

        verifyDatabase();

        String isolation = jdbc.queryForObject(
                "SELECT @@session.transaction_isolation",
                String.class
        );

        Integer autocommit = jdbc.queryForObject(
                "SELECT @@session.autocommit",
                Integer.class
        );

        String database = jdbc.queryForObject(
                "SELECT DATABASE()",
                String.class
        );

        assertEquals("gestiva_test", database);
        assertNotNull(isolation);
        assertNotNull(autocommit);

        System.out.println(
                "MYSQL DIAGNOSTIC: database=" + database
        );

        System.out.println(
                "MYSQL DIAGNOSTIC: isolation=" + isolation
        );

        System.out.println(
                "MYSQL DIAGNOSTIC: autocommit=" + autocommit
        );
    }

    @Test
    void inspectSpringTransactionIsolation() throws Exception {

        verifyDatabase();

        TransactionTemplate template =
                new TransactionTemplate(transactionManager);

        template.setReadOnly(true);

        String isolation = template.execute(status ->
                jdbc.queryForObject(
                        "SELECT @@session.transaction_isolation",
                        String.class
                )
        );

        assertNotNull(isolation);

        System.out.println(
                "SPRING DIAGNOSTIC: isolation=" + isolation
        );
    }

    @Test
    void verifyPurchasingTablesUseInnoDb() throws Exception {

        verifyDatabase();

        for (String table : new String[] {
                "purchase_order",
                "goods_receipt",
                "goods_receipt_line"
        }) {

            String engine = jdbc.queryForObject("""
                SELECT ENGINE
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                """,
                String.class,
                table
            );

            assertEquals(
                    "InnoDB",
                    engine,
                    "Storage engine inatteso: " + table
            );

            System.out.println(
                    "MYSQL DIAGNOSTIC: "
                    + table + " engine=" + engine
            );
        }
    }
}
