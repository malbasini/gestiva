package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class GoodsReceiptPartialWriteTransactionGuard {

    public void requireWritableReadCommitted() {

        if (!TransactionSynchronizationManager
                .isActualTransactionActive()) {
            throw new BusinessException(
                    "Transazione attiva obbligatoria per il ricevimento."
            );
        }

        if (TransactionSynchronizationManager
                .isCurrentTransactionReadOnly()) {
            throw new BusinessException(
                    "Transazione di sola lettura non consentita."
            );
        }

        Integer isolation = TransactionSynchronizationManager
                .getCurrentTransactionIsolationLevel();

        if (isolation == null
                || isolation != TransactionDefinition
                        .ISOLATION_READ_COMMITTED) {
            throw new BusinessException(
                    "Isolamento READ COMMITTED obbligatorio."
            );
        }
    }
}
