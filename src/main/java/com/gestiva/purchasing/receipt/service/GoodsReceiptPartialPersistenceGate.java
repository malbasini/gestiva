package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class GoodsReceiptPartialPersistenceGate {

    public static final String ENABLED_PROPERTY =
            "gestiva.purchasing.partial-goods-receipt.enabled";

    private final Environment environment;
    private final GoodsReceiptIsolationGuard isolationGuard;

    public GoodsReceiptPartialPersistenceGate(
            Environment environment,
            GoodsReceiptIsolationGuard isolationGuard) {

        this.environment = environment;
        this.isolationGuard = isolationGuard;
    }

    /**
     * Autorizza l'accesso al futuro workflow di scrittura
     * soltanto nel profilo test con flag esplicito.
     *
     * Controlla anche la transazione READ COMMITTED.
     *
     * Non modifica dati né configurazioni.
     */
    public void requireEnabled() {

        boolean testProfile = Arrays.asList(
                environment.getActiveProfiles()
        ).contains("test");

        boolean enabled = Boolean.TRUE.equals(
                environment.getProperty(
                        ENABLED_PROPERTY,
                        Boolean.class,
                        false
                )
        );

        if (!testProfile || !enabled) {
            throw new BusinessException(
                    "Ricevimenti parziali temporaneamente "
                    + "non disponibili."
            );
        }

        isolationGuard.requireReadCommitted();
    }
}
