package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GoodsReceiptPartialTransactionIsolationIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private GoodsReceiptPartialPreparationService service;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager manager;

    @MockitoBean
    private PurchaseOrderRepository orders;

    @MockitoBean
    private CompanyContext companies;

    @BeforeEach
    void verifyDatabase() throws Exception {
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

    private int currentIsolation() {
        return jdbc.execute(
                (ConnectionCallback<Integer>)
                connection -> connection.getTransactionIsolation()
        );
    }

    private void simulateMissingOrder(AtomicInteger isolation) {

        when(companies.requireCurrentCompanyId())
                .thenReturn(1L);

        when(orders.findLockedByTenantIdAndCompanyIdAndId(
                eq(2L), eq(1L), eq(999999999L)
        )).thenAnswer(invocation -> {

            assertTrue(
                    TransactionSynchronizationManager
                            .isActualTransactionActive(),
                    "Transazione Spring non attiva."
            );

            isolation.set(currentIsolation());

            return Optional.empty();
        });
    }

    private void invokePreparation() {

        assertThrows(
                BusinessException.class,
                () -> service.prepare(
                        2L,
                        999999999L,
                        Map.of(
                                1L,
                                new BigDecimal("8.000")
                        )
                )
        );
    }

    @Test
    void directProxyInvocationUsesReadCommitted() {

        assertFalse(
                TransactionSynchronizationManager
                        .isActualTransactionActive(),
                "Il test non deve partire in una transazione."
        );

        AtomicInteger observed = new AtomicInteger(-1);

        simulateMissingOrder(observed);

        invokePreparation();

        assertEquals(
                Connection.TRANSACTION_READ_COMMITTED,
                observed.get()
        );

        verify(orders, times(1))
                .findLockedByTenantIdAndCompanyIdAndId(
                        2L, 1L, 999999999L
                );

        System.out.println(
                "SPRING ISOLATION: chiamata diretta = READ COMMITTED"
        );
    }

    @Test
    void requiredPropagationInheritsExistingIsolation() {

        AtomicInteger observed = new AtomicInteger(-1);

        simulateMissingOrder(observed);

        TransactionTemplate outer =
                new TransactionTemplate(manager);

        outer.setReadOnly(false);

        outer.setIsolationLevel(
                TransactionDefinition.ISOLATION_REPEATABLE_READ
        );

        outer.execute(status -> {

            assertEquals(
                    Connection.TRANSACTION_REPEATABLE_READ,
                    currentIsolation()
            );

            invokePreparation();

            assertEquals(
                    Connection.TRANSACTION_REPEATABLE_READ,
                    observed.get()
            );

            status.setRollbackOnly();
            return null;
        });

        System.out.println(
                "SPRING ISOLATION: transazione esterna = REPEATABLE READ"
        );

        System.out.println(
                "SPRING ISOLATION: REQUIRED eredita l'isolamento esterno"
        );
    }
}
