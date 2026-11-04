package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Connection;

@Component
public class GoodsReceiptIsolationGuard {

    private final JdbcTemplate jdbc;

    public GoodsReceiptIsolationGuard(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Verifica la connessione effettivamente associata
     * alla transazione Spring corrente.
     *
     * Non modifica l'isolamento e non esegue scritture.
     */
    public void requireReadCommitted() {

        if (!TransactionSynchronizationManager
                .isActualTransactionActive()) {
            throw new BusinessException(
                    "Ricevimento parziale: transazione obbligatoria."
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
                    "Ricevimento parziale: isolamento "
                    + "READ COMMITTED obbligatorio."
            );
        }
    }
}
