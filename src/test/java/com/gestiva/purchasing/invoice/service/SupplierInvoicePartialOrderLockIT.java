package com.gestiva.purchasing.invoice.service;

import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SupplierInvoicePartialOrderLockIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private PurchaseOrderRepository orders;

    @Autowired
    private PlatformTransactionManager manager;

    @Autowired
    private JdbcTemplate jdbc;

    private TransactionTemplate transaction() {
        TransactionTemplate tx = new TransactionTemplate(manager);
        tx.setReadOnly(false);
        tx.setIsolationLevel(
                TransactionDefinition.ISOLATION_READ_COMMITTED
        );
        return tx;
    }

    private void verifyDatabase() throws Exception {
        try (Connection connection =
                     jdbc.getDataSource().getConnection()) {

            assertEquals("gestiva_test", connection.getCatalog());

            String username =
                    connection.getMetaData().getUserName();

            assertTrue(
                    username.equals("gestiva_test_runner")
                    || username.startsWith("gestiva_test_runner@")
            );
        }
    }

    private Long connectionId() {
        return jdbc.queryForObject(
                "SELECT CONNECTION_ID()", Long.class
        );
    }

    @Test
    @Timeout(30)
    void secondTransactionWaitsForRealJpaOrderLock()
            throws Exception {

        verifyDatabase();

        Map<String, Object> row = jdbc.queryForMap("""
                SELECT id, tenant_id, company_id
                FROM purchase_order
                ORDER BY id ASC
                LIMIT 1
                """);

        Long orderId =
                ((Number) row.get("id")).longValue();
        Long tenantId =
                ((Number) row.get("tenant_id")).longValue();
        Long companyId =
                ((Number) row.get("company_id")).longValue();

        CountDownLatch firstLocked =
                new CountDownLatch(1);
        CountDownLatch releaseFirst =
                new CountDownLatch(1);
        CountDownLatch secondReady =
                new CountDownLatch(1);
        CountDownLatch secondLocked =
                new CountDownLatch(1);

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        try {
            Future<Long> first = executor.submit(() ->
                    transaction().execute(status -> {

                        Long firstConnection = connectionId();

                        var locked = orders
                                .findLockedByTenantIdAndCompanyIdAndId(
                                        tenantId, companyId, orderId
                                );

                        assertTrue(locked.isPresent());
                        assertEquals(
                                orderId, locked.get().getId()
                        );

                        firstLocked.countDown();

                        try {
                            if (!releaseFirst.await(
                                    10, TimeUnit.SECONDS
                            )) {
                                throw new IllegalStateException(
                                        "Timeout rilascio transazione A"
                                );
                            }
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException(ex);
                        }

                        status.setRollbackOnly();
                        return firstConnection;
                    })
            );

            if (!firstLocked.await(10, TimeUnit.SECONDS)) {
                if (first.isDone()) {
                    first.get(2, TimeUnit.SECONDS);
                }
                fail("Transazione A: lock non acquisito");
            }

            System.out.println(
                    "MYSQL JPA: transazione A ha acquisito il lock"
            );

            Future<Long> second = executor.submit(() ->
                    transaction().execute(status -> {

                        Long secondConnection = connectionId();

                        secondReady.countDown();

                        var locked = orders
                                .findLockedByTenantIdAndCompanyIdAndId(
                                        tenantId, companyId, orderId
                                );

                        assertTrue(locked.isPresent());
                        assertEquals(
                                orderId, locked.get().getId()
                        );

                        secondLocked.countDown();

                        status.setRollbackOnly();
                        return secondConnection;
                    })
            );

            assertTrue(
                    secondReady.await(10, TimeUnit.SECONDS),
                    "Transazione B non avviata"
            );

            assertFalse(
                    secondLocked.await(
                            500, TimeUnit.MILLISECONDS
                    ),
                    "La transazione B ha acquisito il lock "
                    + "prima del rilascio da parte di A"
            );

            assertFalse(
                    second.isDone(),
                    "La transazione B si è conclusa troppo presto"
            );

            System.out.println(
                    "MYSQL JPA: transazione B in attesa del lock"
            );

            releaseFirst.countDown();

            Long firstConnection =
                    first.get(10, TimeUnit.SECONDS);
            Long secondConnection =
                    second.get(10, TimeUnit.SECONDS);

            assertNotNull(firstConnection);
            assertNotNull(secondConnection);

            assertNotEquals(
                    firstConnection,
                    secondConnection,
                    "Le transazioni devono usare "
                    + "connessioni MySQL differenti"
            );

            assertEquals(0, secondLocked.getCount());

            System.out.println(
                    "MYSQL JPA: transazione A conclusa"
            );
            System.out.println(
                    "MYSQL JPA: transazione B ha acquisito "
                    + "il lock dopo il rilascio"
            );
            System.out.println(
                    "MYSQL JPA: due connessioni distinte"
            );
            System.out.println(
                    "MYSQL JPA: nessuna scrittura eseguita"
            );

        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();

            assertTrue(
                    executor.awaitTermination(
                            10, TimeUnit.SECONDS
                    ),
                    "Thread di test non terminati"
            );
        }
    }
}
