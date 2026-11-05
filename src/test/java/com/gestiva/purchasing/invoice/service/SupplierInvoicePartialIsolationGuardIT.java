package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class SupplierInvoicePartialIsolationGuardIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private SupplierInvoicePartialIsolationGuard guard;

    @Autowired
    private PlatformTransactionManager manager;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void verifyTestDatabase() throws Exception {
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

    private TransactionTemplate transaction(
            int isolation,
            boolean readOnly) {

        TransactionTemplate tx =
                new TransactionTemplate(manager);

        tx.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );
        tx.setIsolationLevel(isolation);
        tx.setReadOnly(readOnly);

        return tx;
    }

    @Test
    void rejectsMissingTransaction() {
        assertFalse(
                TransactionSynchronizationManager
                        .isActualTransactionActive()
        );

        assertThrows(
                BusinessException.class,
                guard::requireReadCommittedWriteTransaction
        );
    }

    @Test
    void acceptsReadCommittedWriteTransaction() {
        transaction(
                TransactionDefinition.ISOLATION_READ_COMMITTED,
                false
        ).execute(status -> {

            assertTrue(
                    TransactionSynchronizationManager
                            .isActualTransactionActive()
            );

            assertFalse(
                    TransactionSynchronizationManager
                            .isCurrentTransactionReadOnly()
            );

            assertDoesNotThrow(
                    guard::requireReadCommittedWriteTransaction
            );

            Integer actualIsolation = jdbc.execute(
                    (org.springframework.jdbc.core.ConnectionCallback<Integer>)
                            Connection::getTransactionIsolation
            );

            assertEquals(
                    Connection.TRANSACTION_READ_COMMITTED,
                    actualIsolation
            );

            status.setRollbackOnly();
            return null;
        });

        System.out.println(
                "ISOLATION GUARD: READ_COMMITTED scrivibile accettato"
        );
    }

    @Test
    void rejectsRepeatableReadTransaction() {
        transaction(
                TransactionDefinition.ISOLATION_REPEATABLE_READ,
                false
        ).execute(status -> {

            assertTrue(
                    TransactionSynchronizationManager
                            .isActualTransactionActive()
            );

            assertThrows(
                    BusinessException.class,
                    guard::requireReadCommittedWriteTransaction
            );

            status.setRollbackOnly();
            return null;
        });

        System.out.println(
                "ISOLATION GUARD: REPEATABLE_READ rifiutato"
        );
    }

    @Test
    void rejectsReadOnlyTransaction() {
        transaction(
                TransactionDefinition.ISOLATION_READ_COMMITTED,
                true
        ).execute(status -> {

            assertTrue(
                    TransactionSynchronizationManager
                            .isActualTransactionActive()
            );

            assertTrue(
                    TransactionSynchronizationManager
                            .isCurrentTransactionReadOnly()
            );

            assertThrows(
                    BusinessException.class,
                    guard::requireReadCommittedWriteTransaction
            );

            status.setRollbackOnly();
            return null;
        });

        System.out.println(
                "ISOLATION GUARD: transazione read-only rifiutata"
        );
    }
}
