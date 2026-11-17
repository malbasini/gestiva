package com.gestiva.warehouse.location.repository;

import com.gestiva.support.TestDatabaseSafetyInitializer;
import com.gestiva.warehouse.location.entity.Warehouse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestDatabaseSafetyInitializer.class)
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=never"
})
class WarehouseLocationConcurrencyLockJpaTest {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void secondTransactionWaitsForWarehouseLock() throws Exception {

        Warehouse warehouse = warehouseRepository.findAll()
                .stream()
                .filter(w -> w.getTenantId() != null
                        && w.getCompany() != null)
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Nessun magazzino disponibile nel database TEST."
                ));

        Long tenantId = warehouse.getTenantId();
        Long companyId = warehouse.getCompany().getId();
        Long warehouseId = warehouse.getId();

        CountDownLatch firstLocked = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch secondAttempting = new CountDownLatch(1);
        CountDownLatch secondLocked = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<?> first = executor.submit(() -> {
                TransactionTemplate tx = new TransactionTemplate(
                        transactionManager
                );
                tx.setPropagationBehavior(
                        TransactionDefinition.PROPAGATION_REQUIRES_NEW
                );

                tx.executeWithoutResult(status -> {
                    warehouseRepository
                            .findByTenantAndCompanyAndIdForUpdate(
                                    tenantId, companyId, warehouseId
                            )
                            .orElseThrow();

                    firstLocked.countDown();

                    try {
                        if (!releaseFirst.await(10, TimeUnit.SECONDS)) {
                            throw new AssertionError(
                                    "Timeout rilascio primo lock."
                            );
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(ex);
                    }
                });
            });

            assertTrue(
                    firstLocked.await(5, TimeUnit.SECONDS),
                    "La transazione A non ha acquisito il lock."
            );

            Future<?> second = executor.submit(() -> {
                TransactionTemplate tx = new TransactionTemplate(
                        transactionManager
                );
                tx.setPropagationBehavior(
                        TransactionDefinition.PROPAGATION_REQUIRES_NEW
                );

                tx.executeWithoutResult(status -> {
                    secondAttempting.countDown();

                    warehouseRepository
                            .findByTenantAndCompanyAndIdForUpdate(
                                    tenantId, companyId, warehouseId
                            )
                            .orElseThrow();

                    secondLocked.countDown();
                });
            });

            assertTrue(
                    secondAttempting.await(5, TimeUnit.SECONDS),
                    "La transazione B non è partita."
            );

            assertFalse(
                    secondLocked.await(500, TimeUnit.MILLISECONDS),
                    "La transazione B ha acquisito il lock prima del rilascio."
            );

            releaseFirst.countDown();

            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);

            assertEquals(0L, secondLocked.getCount(),
                    "La transazione B non ha acquisito il lock.");

        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();
            assertTrue(
                    executor.awaitTermination(15, TimeUnit.SECONDS),
                    "Le transazioni di test non sono terminate."
            );
        }
    }
}
