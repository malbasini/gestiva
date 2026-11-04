package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptPartialWriteTransactionGuardTest {

    private final GoodsReceiptPartialWriteTransactionGuard guard =
            new GoodsReceiptPartialWriteTransactionGuard();

    @AfterEach
    void cleanup() {
        TransactionSynchronizationManager
                .setActualTransactionActive(false);
        TransactionSynchronizationManager
                .setCurrentTransactionReadOnly(false);
        TransactionSynchronizationManager
                .setCurrentTransactionIsolationLevel(null);
    }

    @Test
    void rejectsMissingTransaction() {
        TransactionSynchronizationManager
                .setActualTransactionActive(false);

        assertThrows(
                BusinessException.class,
                guard::requireWritableReadCommitted
        );

        System.out.println(
                "PARTIAL WRITE TX: transazione assente rifiutata"
        );
    }

    @Test
    void rejectsReadOnlyTransaction() {
        TransactionSynchronizationManager
                .setActualTransactionActive(true);
        TransactionSynchronizationManager
                .setCurrentTransactionReadOnly(true);
        TransactionSynchronizationManager
                .setCurrentTransactionIsolationLevel(
                        TransactionDefinition.ISOLATION_READ_COMMITTED
                );

        assertThrows(
                BusinessException.class,
                guard::requireWritableReadCommitted
        );

        System.out.println(
                "PARTIAL WRITE TX: read-only rifiutata"
        );
    }

    @Test
    void rejectsWrongIsolation() {
        TransactionSynchronizationManager
                .setActualTransactionActive(true);
        TransactionSynchronizationManager
                .setCurrentTransactionReadOnly(false);
        TransactionSynchronizationManager
                .setCurrentTransactionIsolationLevel(
                        TransactionDefinition.ISOLATION_REPEATABLE_READ
                );

        assertThrows(
                BusinessException.class,
                guard::requireWritableReadCommitted
        );

        System.out.println(
                "PARTIAL WRITE TX: REPEATABLE READ rifiutato"
        );
    }

    @Test
    void acceptsWritableReadCommittedState() {
        TransactionSynchronizationManager
                .setActualTransactionActive(true);
        TransactionSynchronizationManager
                .setCurrentTransactionReadOnly(false);
        TransactionSynchronizationManager
                .setCurrentTransactionIsolationLevel(
                        TransactionDefinition.ISOLATION_READ_COMMITTED
                );

        assertDoesNotThrow(
                guard::requireWritableReadCommitted
        );

        System.out.println(
                "PARTIAL WRITE TX: stato transazionale valido"
        );
    }
}
