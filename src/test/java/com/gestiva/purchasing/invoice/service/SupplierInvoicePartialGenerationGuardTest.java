package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

class SupplierInvoicePartialGenerationGuardTest {

    private static final String GENERAL =
            "gestiva.purchasing.supplier-invoice.enabled";

    private static final String PARTIAL =
            "gestiva.purchasing.supplier-invoice.partial.enabled";

    private MockEnvironment environment(
            String profile,
            boolean general,
            boolean partial) {

        MockEnvironment env = new MockEnvironment();

        if (profile != null) {
            env.setActiveProfiles(profile);
        }

        env.setProperty(GENERAL, Boolean.toString(general));
        env.setProperty(PARTIAL, Boolean.toString(partial));

        return env;
    }

    @Test
    void allowsOnlyTestWithBothFlagsEnabled() {
        var env = environment("test", true, true);

        assertDoesNotThrow(
                new SupplierInvoicePartialGenerationGuard(env)
                        ::requireEnabled
        );
    }

    @Test
    void rejectsMissingPartialFlag() {
        var env = environment("test", true, false);

        assertThrows(
                BusinessException.class,
                new SupplierInvoicePartialGenerationGuard(env)
                        ::requireEnabled
        );
    }

    @Test
    void rejectsDisabledGeneralFlag() {
        var env = environment("test", false, true);

        assertThrows(
                BusinessException.class,
                new SupplierInvoicePartialGenerationGuard(env)
                        ::requireEnabled
        );
    }

    @Test
    void rejectsProductionEvenWithBothFlagsEnabled() {
        var env = environment("prod", true, true);

        assertThrows(
                BusinessException.class,
                new SupplierInvoicePartialGenerationGuard(env)
                        ::requireEnabled
        );
    }

    @Test
    void rejectsMissingActiveProfile() {
        var env = environment(null, true, true);

        assertThrows(
                BusinessException.class,
                new SupplierInvoicePartialGenerationGuard(env)
                        ::requireEnabled
        );
    }

    @Test
    void rejectsMissingEnvironment() {
        assertThrows(
                BusinessException.class,
                new SupplierInvoicePartialGenerationGuard(null)
                        ::requireEnabled
        );
    }

    @Test
    void rejectsPartialFlagWhenNotConfigured() {
        var env = new MockEnvironment();

        env.setActiveProfiles("test");
        env.setProperty(GENERAL, "true");

        assertThrows(
                BusinessException.class,
                new SupplierInvoicePartialGenerationGuard(env)
                        ::requireEnabled
        );
    }
}
