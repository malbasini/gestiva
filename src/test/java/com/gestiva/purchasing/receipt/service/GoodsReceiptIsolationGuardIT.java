package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptIsolationGuardIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private GoodsReceiptIsolationGuard guard;

    @Autowired
    private PlatformTransactionManager manager;

    @Autowired
    private JdbcTemplate jdbc;

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
                    )
            );
        }
    }

    private TransactionTemplate tx(int isolation) {
        TransactionTemplate template =
                new TransactionTemplate(manager);

        template.setReadOnly(false);
        template.setIsolationLevel(isolation);

        return template;
    }

    @Test
    void acceptsReadCommittedTransaction() throws Exception {
        verifyDatabase();

        tx(TransactionDefinition.ISOLATION_READ_COMMITTED)
                .execute(status -> {

                    assertDoesNotThrow(
                            guard::requireReadCommitted
                    );

                    status.setRollbackOnly();
                    return null;
                });

        System.out.println(
                "ISOLATION GUARD: READ COMMITTED accettato"
        );
    }

    @Test
    void rejectsRepeatableReadTransaction() throws Exception {
        verifyDatabase();

        tx(TransactionDefinition.ISOLATION_REPEATABLE_READ)
                .execute(status -> {

                    assertThrows(
                            BusinessException.class,
                            guard::requireReadCommitted
                    );

                    status.setRollbackOnly();
                    return null;
                });

        System.out.println(
                "ISOLATION GUARD: REPEATABLE READ rifiutato"
        );
    }

    @Test
    void rejectsCallsWithoutTransaction() throws Exception {
        verifyDatabase();

        assertThrows(
                BusinessException.class,
                guard::requireReadCommitted
        );

        System.out.println(
                "ISOLATION GUARD: assenza transazione rifiutata"
        );
    }
}
