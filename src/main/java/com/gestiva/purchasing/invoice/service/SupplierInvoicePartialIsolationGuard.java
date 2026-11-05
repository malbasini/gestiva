package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Connection;

/**
 * Protezione dell'isolamento transazionale
 * per la fatturazione dei ricevimenti parziali.
 *
 * Verifica la connessione JDBC effettiva associata
 * alla transazione Spring corrente.
 *
 * Non modifica l'isolamento e non esegue scritture.
 */
@Component
public class SupplierInvoicePartialIsolationGuard {

    private final JdbcTemplate jdbc;

    public SupplierInvoicePartialIsolationGuard(
            JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void requireReadCommittedWriteTransaction() {

        if (!TransactionSynchronizationManager
                .isActualTransactionActive()) {
            throw new BusinessException(
                    "Fatturazione parziale: "
                    + "transazione obbligatoria."
            );
        }

        if (TransactionSynchronizationManager
                .isCurrentTransactionReadOnly()) {
            throw new BusinessException(
                    "Fatturazione parziale: "
                    + "transazione di sola lettura non consentita."
            );
        }

        Integer actualIsolation = jdbc.execute(
                (ConnectionCallback<Integer>)
                        Connection::getTransactionIsolation
        );

        if (actualIsolation == null
                || actualIsolation
                != Connection.TRANSACTION_READ_COMMITTED) {
            throw new BusinessException(
                    "Fatturazione parziale: "
                    + "isolamento READ COMMITTED obbligatorio."
            );
        }
    }
}
