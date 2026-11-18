package com.gestiva.warehouse.location.repository;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.support.TestDatabaseSafetyInitializer;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.movement.service.InventoryLocationValidationService;
import com.gestiva.warehouse.inventory.movement.service.InventoryMovementService;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.location.entity.WarehouseLocation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestDatabaseSafetyInitializer.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=never"
})
class WarehouseLocationMovementLockIntegrationTest {

    @Autowired
    WarehouseRepository warehouseRepository;

    @Autowired
    WarehouseLocationRepository locationRepository;

    @Autowired
    PlatformTransactionManager transactionManager;

    record Target(Long tenantId, Long companyId,
                  Long warehouseId, Long locationId) {}

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void movementWaitsForLockThenRejectsInactiveLocation() throws Exception {

        TransactionTemplate selectionTx =
                new TransactionTemplate(transactionManager);

        Target target = selectionTx.execute(status ->
                locationRepository.findAll().stream()
                        .filter(location -> !location.isActive())
                        .filter(location -> location.getTenantId() != null)
                        .filter(location -> location.getWarehouse() != null)
                        .filter(location -> location.getWarehouse().isActive())
                        .filter(location ->
                                location.getWarehouse().getCompany() != null)
                        .filter(location ->
                                location.getWarehouse().getCompany().isActive())
                        .map(location -> new Target(
                                location.getTenantId(),
                                location.getWarehouse().getCompany().getId(),
                                location.getWarehouse().getId(),
                                location.getId()
                        ))
                        .findFirst()
                        .orElseThrow(() -> new AssertionError(
                                "Nessuna ubicazione inattiva disponibile nel database TEST."
                        ))
        );

        assertNotNull(target);

        InventoryMovementRepository movementRepository =
                mock(InventoryMovementRepository.class);
        ItemRepository itemRepository = mock(ItemRepository.class);
        InventoryValuationService valuationService =
                mock(InventoryValuationService.class);

        InventoryLocationValidationService validationService =
                new InventoryLocationValidationService(
                        warehouseRepository, locationRepository
                );

        InventoryMovementService movementService =
                new InventoryMovementService(
                        movementRepository,
                        itemRepository,
                        validationService,
                        valuationService,
                        warehouseRepository
                );

        CountDownLatch firstLocked = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch secondAttempting = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<?> first = executor.submit(() -> {
                TransactionTemplate tx =
                        new TransactionTemplate(transactionManager);

                tx.executeWithoutResult(status -> {
                    warehouseRepository
                            .findByTenantAndCompanyAndIdForUpdate(
                                    target.tenantId(),
                                    target.companyId(),
                                    target.warehouseId()
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

            assertTrue(firstLocked.await(5, TimeUnit.SECONDS),
                    "La transazione A non ha acquisito il lock.");

            Future<BusinessException> second = executor.submit(() -> {
                TransactionTemplate tx =
                        new TransactionTemplate(transactionManager);

                return tx.execute(status -> {
                    secondAttempting.countDown();

                    return assertThrows(
                            BusinessException.class,
                            () -> movementService.registerMovement(
                                    target.tenantId(),
                                    target.companyId(),
                                    target.warehouseId(),
                                    target.locationId(),
                                    -1L,
                                    LocalDate.of(2026, 1, 1),
                                    "IN",
                                    "TEST_LOCK",
                                    BigDecimal.ONE,
                                    BigDecimal.ONE,
                                    "TEST",
                                    -1L,
                                    "Test senza salvataggio"
                            )
                    );
                });
            });

            assertTrue(secondAttempting.await(5, TimeUnit.SECONDS),
                    "La transazione B non è partita.");

            assertFalse(second.isDone(),
                    "La transazione B è terminata prima del rilascio del lock.");

            assertFalse(
                    second.isDone() || waitForCompletion(second, 500),
                    "La transazione B non ha atteso il lock."
            );

            releaseFirst.countDown();

            first.get(10, TimeUnit.SECONDS);

            BusinessException exception =
                    second.get(10, TimeUnit.SECONDS);

            assertTrue(
                    exception.getMessage().contains(
                            "ubicazione selezionata non è attiva"
                    ),
                    "La registrazione deve essere rifiutata dal validatore."
            );

            verifyNoInteractions(
                    movementRepository,
                    itemRepository,
                    valuationService
            );

        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();

            assertTrue(
                    executor.awaitTermination(15, TimeUnit.SECONDS),
                    "Le transazioni concorrenti non sono terminate."
            );
        }
    }

    private static boolean waitForCompletion(
            Future<?> future, long milliseconds) throws Exception {
        try {
            future.get(milliseconds, TimeUnit.MILLISECONDS);
            return true;
        } catch (TimeoutException expected) {
            return false;
        }
    }
}
