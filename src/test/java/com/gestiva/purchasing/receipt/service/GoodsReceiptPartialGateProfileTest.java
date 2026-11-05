package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoodsReceiptPartialGateProfileTest {

    private static final String FLAG =
            GoodsReceiptPartialPersistenceGate.ENABLED_PROPERTY;

    private GoodsReceiptPartialPersistenceGate gate(
            MockEnvironment environment,
            GoodsReceiptIsolationGuard isolationGuard) {

        return new GoodsReceiptPartialPersistenceGate(
                environment,
                isolationGuard
        );
    }

    @Test
    void productionWithoutFlagMustBeRejected() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");

        GoodsReceiptIsolationGuard guard =
                mock(GoodsReceiptIsolationGuard.class);

        assertThrows(
                BusinessException.class,
                () -> gate(environment, guard).requireEnabled()
        );

        verifyNoInteractions(guard);
    }

    @Test
    void productionWithFlagMustStillBeRejected() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        environment.setProperty(FLAG, "true");

        GoodsReceiptIsolationGuard guard =
                mock(GoodsReceiptIsolationGuard.class);

        assertThrows(
                BusinessException.class,
                () -> gate(environment, guard).requireEnabled()
        );

        verifyNoInteractions(guard);
    }

    @Test
    void testProfileWithoutFlagMustBeRejected() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("test");

        GoodsReceiptIsolationGuard guard =
                mock(GoodsReceiptIsolationGuard.class);

        assertThrows(
                BusinessException.class,
                () -> gate(environment, guard).requireEnabled()
        );

        verifyNoInteractions(guard);
    }

    @Test
    void testProfileWithFlagMustCheckIsolation() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("test");
        environment.setProperty(FLAG, "true");

        GoodsReceiptIsolationGuard guard =
                mock(GoodsReceiptIsolationGuard.class);

        assertDoesNotThrow(
                () -> gate(environment, guard).requireEnabled()
        );

        verify(guard, times(1)).requireReadCommitted();
        verifyNoMoreInteractions(guard);
    }

    @Test
    void isolationFailureMustBlockEnabledTestProfile() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("test");
        environment.setProperty(FLAG, "true");

        GoodsReceiptIsolationGuard guard =
                mock(GoodsReceiptIsolationGuard.class);

        doThrow(
                new BusinessException(
                        "Isolamento transazionale non valido."
                )
        ).when(guard).requireReadCommitted();

        assertThrows(
                BusinessException.class,
                () -> gate(environment, guard).requireEnabled()
        );

        verify(guard).requireReadCommitted();
    }
}
