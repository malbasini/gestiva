package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoodsReceiptPartialPersistenceGateTest {

    private Environment environment;
    private GoodsReceiptIsolationGuard guard;
    private GoodsReceiptPartialPersistenceGate gate;

    @BeforeEach
    void setup() {

        environment = mock(Environment.class);
        guard = mock(GoodsReceiptIsolationGuard.class);

        gate = new GoodsReceiptPartialPersistenceGate(
                environment, guard
        );
    }

    private void profile(String... profiles) {
        when(environment.getActiveProfiles())
                .thenReturn(profiles);
    }

    private void enabled(boolean value) {
        when(environment.getProperty(
                GoodsReceiptPartialPersistenceGate.ENABLED_PROPERTY,
                Boolean.class,
                false
        )).thenReturn(value);
    }

    @Test
    void defaultDisabledRejectsAccess() {

        profile("test");

        assertThrows(
                BusinessException.class,
                gate::requireEnabled
        );

        verifyNoInteractions(guard);

        System.out.println(
                "PARTIAL GATE: default disabilitato"
        );
    }

    @Test
    void productionRejectsEvenWithFlagEnabled() {

        profile("prod");
        enabled(true);

        assertThrows(
                BusinessException.class,
                gate::requireEnabled
        );

        verifyNoInteractions(guard);

        System.out.println(
                "PARTIAL GATE: produzione sempre bloccata"
        );
    }

    @Test
    void testProfileRequiresExplicitFlag() {

        profile("test");
        enabled(false);

        assertThrows(
                BusinessException.class,
                gate::requireEnabled
        );

        verifyNoInteractions(guard);

        System.out.println(
                "PARTIAL GATE: flag esplicito obbligatorio"
        );
    }

    @Test
    void enabledTestProfileChecksIsolation() {

        profile("test");
        enabled(true);

        assertDoesNotThrow(gate::requireEnabled);

        verify(guard, times(1))
                .requireReadCommitted();

        System.out.println(
                "PARTIAL GATE: test abilitato con guardia"
        );
    }

    @Test
    void wrongIsolationRejectsEnabledTestProfile() {

        profile("test");
        enabled(true);

        doThrow(new BusinessException(
                "Isolamento READ COMMITTED obbligatorio."
        )).when(guard).requireReadCommitted();

        assertThrows(
                BusinessException.class,
                gate::requireEnabled
        );

        verify(guard, times(1))
                .requireReadCommitted();

        System.out.println(
                "PARTIAL GATE: isolamento incompatibile rifiutato"
        );
    }
}
