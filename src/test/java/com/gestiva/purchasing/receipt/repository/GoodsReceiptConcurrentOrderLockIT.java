package com.gestiva.purchasing.receipt.repository;

import com.gestiva.support.AbstractMySqlIntegrationTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class GoodsReceiptConcurrentOrderLockIT
        extends AbstractMySqlIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager transactionManager;

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

    private Long lockOrder(
            Long tenantId,
            Long companyId,
            Long orderId) {

        return jdbc.queryForObject("""
            SELECT id
            FROM purchase_order
            WHERE tenant_id = ?
              AND company_id = ?
              AND id = ?
            FOR UPDATE
            """,
            Long.class,
            tenantId,
            companyId,
            orderId
        );
    }

    @Test
    @Timeout(25)
    void secondTransactionWaitsForFirstOrderLock()
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

        assertNotNull(orderId);
        assertNotNull(tenantId);
        assertNotNull(companyId);

        CountDownLatch firstLocked =
                new CountDownLatch(1);

        CountDownLatch releaseFirst =
                new CountDownLatch(1);

        CountDownLatch secondAttemptStarted =
                new CountDownLatch(1);

        CountDownLatch secondLocked =
                new CountDownLatch(1);

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        try {
            Future<?> first = executor.submit(() -> {

                TransactionTemplate tx =
                        new TransactionTemplate(
                                transactionManager
                        );

                tx.setReadOnly(false);

                tx.execute(status -> {

                    assertEquals(
                            orderId,
                            lockOrder(
                                    tenantId,
                                    companyId,
                                    orderId
                            )
                    );

                    firstLocked.countDown();

                    try {
                        if (!releaseFirst.await(
                                10, TimeUnit.SECONDS)) {
                            throw new IllegalStateException(
                                    "Timeout rilascio primo blocco."
                            );
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(ex);
                    }

                    return null;
                });
            });

            boolean firstAcquired = firstLocked.await(
                    10, TimeUnit.SECONDS
            );

            if (!firstAcquired) {
                if (first.isDone()) {
                    // Propaga la vera eccezione del thread,
                    // anziché nasconderla dietro al timeout.
                    first.get(2, TimeUnit.SECONDS);
                }

                fail(
                        "La prima transazione non ha acquisito "
                        + "il blocco entro 10 secondi."
                );
            }

            System.out.println(
                    "MYSQL LOCK: transazione A ha il blocco"
            );

            Future<?> second = executor.submit(() -> {

                TransactionTemplate tx =
                        new TransactionTemplate(
                                transactionManager
                        );

                tx.setReadOnly(false);

                tx.execute(status -> {

                    secondAttemptStarted.countDown();

                    assertEquals(
                            orderId,
                            lockOrder(
                                    tenantId,
                                    companyId,
                                    orderId
                            )
                    );

                    secondLocked.countDown();

                    return null;
                });
            });

            assertTrue(
                    secondAttemptStarted.await(
                            10, TimeUnit.SECONDS
                    )
            );

            boolean acquiredTooEarly =
                    secondLocked.await(
                            400, TimeUnit.MILLISECONDS
                    );

            assertFalse(
                    acquiredTooEarly,
                    "La seconda transazione ha acquisito "
                    + "il blocco prima del rilascio."
            );

            System.out.println(
                    "MYSQL LOCK: transazione B in attesa"
            );

            releaseFirst.countDown();

            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);

            assertEquals(0, secondLocked.getCount());

            System.out.println(
                    "MYSQL LOCK: transazione A conclusa"
            );
            System.out.println(
                    "MYSQL LOCK: transazione B ha acquisito "
                    + "il blocco dopo A"
            );

        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();

            assertTrue(
                    executor.awaitTermination(
                            10, TimeUnit.SECONDS
                    ),
                    "Thread di test non terminati."
            );
        }
    }
}
