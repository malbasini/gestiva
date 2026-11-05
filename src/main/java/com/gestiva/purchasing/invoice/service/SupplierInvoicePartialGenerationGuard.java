package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Protezione esplicita del nuovo percorso di fatturazione
 * dei ricevimenti parziali.
 *
 * Non abilita alcuna funzionalità autonomamente.
 */
@Component
public class SupplierInvoicePartialGenerationGuard {

    private final Environment environment;

    public SupplierInvoicePartialGenerationGuard(
            Environment environment) {
        this.environment = environment;
    }

    public void requireEnabled() {
        boolean testProfile =
                environment != null
                && Arrays.asList(
                        environment.getActiveProfiles()
                ).contains("test");

        if (!testProfile) {
            throw new BusinessException(
                    "Fatturazione parziale non disponibile."
            );
        }

        boolean generalEnabled = environment.getProperty(
                "gestiva.purchasing.supplier-invoice.enabled",
                Boolean.class,
                false
        );

        if (!generalEnabled) {
            throw new BusinessException(
                    "Fatturazione parziale non disponibile."
            );
        }

        boolean partialEnabled = environment.getProperty(
                "gestiva.purchasing.supplier-invoice.partial.enabled",
                Boolean.class,
                false
        );

        if (!partialEnabled) {
            throw new BusinessException(
                    "Fatturazione parziale non disponibile."
            );
        }
    }
}
