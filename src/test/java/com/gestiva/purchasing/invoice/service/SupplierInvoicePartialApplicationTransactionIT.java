package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialApplicationTransactionIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long RECEIPT = 302L;

    @Autowired
    private SupplierInvoicePartialApplicationService service;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private SupplierInvoicePartialGenerationGuard guard;

    @MockitoBean
    private CompanyContext companyContext;

    @MockitoBean
    private SupplierInvoicePartialDraftCreationService creation;

    private void verifyDatabase() throws Exception {

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
                    ),
                    "Account MySQL non autorizzato."
            );
        }
    }

    private long verifyActiveTransaction() {

        assertTrue(
                TransactionSynchronizationManager
                        .isActualTransactionActive(),
                "Transazione Spring non attiva."
        );

        assertTrue(
                TransactionSynchronizationManager
                        .isSynchronizationActive(),
                "Sincronizzazione transazionale inattiva."
        );

        assertFalse(
                TransactionSynchronizationManager
                        .isCurrentTransactionReadOnly(),
                "Transazione configurata read-only."
        );

        String isolation = jdbc.queryForObject(
                "SELECT @@transaction_isolation",
                String.class
        );

        assertNotNull(isolation);

        assertEquals(
                "READ-COMMITTED",
                isolation.toUpperCase().replace('_', '-'),
                "Isolamento JDBC effettivo non corretto."
        );

        Long connectionId = jdbc.queryForObject(
                "SELECT CONNECTION_ID()",
                Long.class
        );

        assertNotNull(connectionId);
        assertTrue(connectionId > 0);

        return connectionId;
    }

    @Test
    void successfulApplicationCallCommitsTransaction()
            throws Exception {

        verifyDatabase();

        assertFalse(
                TransactionSynchronizationManager
                        .isActualTransactionActive()
        );

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        AtomicInteger completion = new AtomicInteger(-1);
        AtomicLong connectionId = new AtomicLong(-1);

        when(creation.create(TENANT, RECEIPT))
                .thenAnswer(invocation -> {

                    connectionId.set(
                            verifyActiveTransaction()
                    );

                    TransactionSynchronizationManager
                            .registerSynchronization(
                                    new TransactionSynchronization() {
                                        @Override
                                        public void afterCompletion(
                                                int status) {
                                            completion.set(status);
                                        }
                                    }
                            );

                    return 501L;
                });

        Long result = service.create(TENANT, RECEIPT);

        assertEquals(501L, result);

        assertEquals(
                TransactionSynchronization.STATUS_COMMITTED,
                completion.get(),
                "Spring non ha effettuato il commit."
        );

        assertTrue(connectionId.get() > 0);

        assertFalse(
                TransactionSynchronizationManager
                        .isActualTransactionActive()
        );

        var order = inOrder(
                guard,
                companyContext,
                creation
        );

        order.verify(guard).requireEnabled();

        order.verify(companyContext)
                .requireCurrentCompanyId();

        order.verify(creation)
                .create(TENANT, RECEIPT);

        order.verifyNoMoreInteractions();

        System.out.println(
                "APPLICATION TX: READ_COMMITTED verificato"
        );

        System.out.println(
                "APPLICATION TX: transazione modificabile"
        );

        System.out.println(
                "APPLICATION TX: commit Spring confermato"
        );

        System.out.println(
                "APPLICATION TX: connessione MySQL="
                + connectionId.get()
        );
    }

    @Test
    void coordinatorFailureRollsBackApplicationTransaction()
            throws Exception {

        verifyDatabase();

        assertFalse(
                TransactionSynchronizationManager
                        .isActualTransactionActive()
        );

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        AtomicInteger completion = new AtomicInteger(-1);
        AtomicLong connectionId = new AtomicLong(-1);

        when(creation.create(TENANT, RECEIPT))
                .thenAnswer(invocation -> {

                    connectionId.set(
                            verifyActiveTransaction()
                    );

                    TransactionSynchronizationManager
                            .registerSynchronization(
                                    new TransactionSynchronization() {
                                        @Override
                                        public void afterCompletion(
                                                int status) {
                                            completion.set(status);
                                        }
                                    }
                            );

                    throw new BusinessException(
                            "IT2074: errore coordinatore simulato"
                    );
                });

        BusinessException error = assertThrows(
                BusinessException.class,
                () -> service.create(TENANT, RECEIPT)
        );

        assertEquals(
                "IT2074: errore coordinatore simulato",
                error.getMessage()
        );

        assertEquals(
                TransactionSynchronization.STATUS_ROLLED_BACK,
                completion.get(),
                "Spring non ha effettuato il rollback."
        );

        assertTrue(connectionId.get() > 0);

        assertFalse(
                TransactionSynchronizationManager
                        .isActualTransactionActive()
        );

        verify(guard, times(1)).requireEnabled();

        verify(companyContext, times(1))
                .requireCurrentCompanyId();

        verify(creation, times(1))
                .create(TENANT, RECEIPT);

        System.out.println(
                "APPLICATION TX: eccezione propagata"
        );

        System.out.println(
                "APPLICATION TX: rollback Spring confermato"
        );

        System.out.println(
                "APPLICATION TX: nessuna INSERT applicativa"
        );
    }
}
