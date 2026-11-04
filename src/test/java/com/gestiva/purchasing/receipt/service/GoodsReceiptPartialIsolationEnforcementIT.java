package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GoodsReceiptPartialIsolationEnforcementIT
        extends AbstractMySqlIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long MISSING_ORDER = 999999999L;

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
                    "Utente MySQL non autorizzato."
            );
        }
    }

    private TransactionTemplate transaction(int isolation) {
        TransactionTemplate tx = new TransactionTemplate(manager);
        tx.setReadOnly(false);
        tx.setIsolationLevel(isolation);
        return tx;
    }

    private void prepareMissingOrder() {
        service.prepare(
                TENANT,
                MISSING_ORDER,
                Map.of(1L, new BigDecimal("8.000"))
        );
    }

    @Test
    void repeatableReadIsRejectedBeforeOrderAccess()
            throws Exception {

        verifyDatabase();

        transaction(
                TransactionDefinition.ISOLATION_REPEATABLE_READ
        ).execute(status -> {

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    this::prepareMissingOrder
            );

            assertTrue(
                    exception.getMessage().contains("READ COMMITTED"),
                    "La guardia deve rifiutare l'isolamento."
            );

            // Il servizio deve fermarsi prima della ricerca
            // dell'ordine e prima di consultare la company.
            verifyNoInteractions(orders, companies);

            status.setRollbackOnly();
            return null;
        });

        System.out.println(
                "ENFORCEMENT: REPEATABLE READ rifiutato"
        );

        System.out.println(
                "ENFORCEMENT: nessun accesso all'ordine"
        );
    }

    @Test
    void readCommittedPassesGuardAndReachesRepository()
            throws Exception {

        verifyDatabase();

        AtomicInteger observedIsolation =
                new AtomicInteger(-1);

        when(companies.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        when(orders.findLockedByTenantIdAndCompanyIdAndId(
                TENANT,
                COMPANY,
                MISSING_ORDER
        )).thenAnswer(invocation -> {

            Integer isolation = jdbc.execute(
                    (ConnectionCallback<Integer>)
                    Connection::getTransactionIsolation
            );

            observedIsolation.set(isolation);

            return Optional.empty();
        });

        transaction(
                TransactionDefinition.ISOLATION_READ_COMMITTED
        ).execute(status -> {

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    this::prepareMissingOrder
            );

            assertTrue(
                    exception.getMessage()
                            .contains("Ordine fornitore non trovato"),
                    "Il servizio deve superare la guardia "
                    + "e arrivare alla ricerca dell'ordine."
            );

            assertEquals(
                    Connection.TRANSACTION_READ_COMMITTED,
                    observedIsolation.get()
            );

            status.setRollbackOnly();
            return null;
        });

        verify(companies, times(1))
                .requireCurrentCompanyId();

        verify(orders, times(1))
                .findLockedByTenantIdAndCompanyIdAndId(
                        TENANT,
                        COMPANY,
                        MISSING_ORDER
                );

        System.out.println(
                "ENFORCEMENT: READ COMMITTED accettato"
        );

        System.out.println(
                "ENFORCEMENT: repository raggiunto correttamente"
        );
    }
}
