package com.gestiva.warehouse.location.repository;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.support.TestDatabaseSafetyInitializer;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.movement.service.InventoryLocationValidationService;
import com.gestiva.warehouse.inventory.movement.service.InventoryMovementService;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.inventory.valuation.service.InventoryValuationService;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.location.entity.WarehouseLocation;
import com.gestiva.warehouse.location.service.WarehouseLocationDeactivationService;
import com.gestiva.warehouse.location.service.WarehouseLocationService;

import jakarta.persistence.EntityManager;
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
class WarehouseLocationDeactivationMovementRaceTest {

    @Autowired WarehouseRepository warehouseRepository;
    @Autowired WarehouseLocationRepository locationRepository;
    @Autowired InventoryMovementRepository movementRepository;
    @Autowired InventoryLayerRepository layerRepository;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired EntityManager entityManager;

    record Target(Long tenant, Long company, Long warehouse, Long location) {}

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void rollbackOfDeactivationReleasesMovementAndPreservesLocation()
            throws Exception {

        TransactionTemplate selection = new TransactionTemplate(transactionManager);

        Target target = selection.execute(status ->
                locationRepository.findAll().stream()
                        .filter(l -> Long.valueOf(2L).equals(l.getTenantId()))
                        .filter(l -> "U1-TEST-RACE-01".equals(l.getCode()))
                        .filter(l -> l.getWarehouse() != null)
                        .filter(l -> Long.valueOf(2L).equals(l.getWarehouse().getId()))
                        .filter(l -> l.getWarehouse().getCompany() != null)
                        .filter(l -> Long.valueOf(3L).equals(
                                l.getWarehouse().getCompany().getId()))
                        .filter(WarehouseLocation::isActive)
                        .filter(l -> l.getWarehouse().isActive())
                        .filter(l -> l.getWarehouse().getCompany() != null)
                        .filter(l -> l.getWarehouse().getCompany().isActive())
                        .filter(l -> l.getTenantId() != null)
                        .filter(l -> locationRepository
                                .findByTenantIdAndWarehouseIdAndParentLocationId(
                                        l.getTenantId(),
                                        l.getWarehouse().getId(),
                                        l.getId()
                                ).stream().noneMatch(WarehouseLocation::isActive))
                        .filter(l -> movementRepository
                                .findItemIdsWithNonZeroBalanceByLocation(
                                        l.getTenantId(),
                                        l.getWarehouse().getCompany().getId(),
                                        l.getWarehouse().getId(),
                                        l.getId()
                                ).isEmpty())
                        .filter(l -> layerRepository.countRemainingLayersByLocation(
                                l.getTenantId(),
                                l.getWarehouse().getCompany().getId(),
                                l.getWarehouse().getId(),
                                l.getId()
                        ) == 0)
                        .map(l -> new Target(
                                l.getTenantId(),
                                l.getWarehouse().getCompany().getId(),
                                l.getWarehouse().getId(),
                                l.getId()
                        ))
                        .findFirst()
                        .orElseThrow(() -> new AssertionError(
                                "Nessuna ubicazione attiva e vuota adatta al collaudo TEST."
                        ))
        );

        WarehouseLocationService locationService =
                new WarehouseLocationService(locationRepository, warehouseRepository);

        WarehouseLocationDeactivationService deactivationService =
                new WarehouseLocationDeactivationService(
                        locationService, locationRepository, warehouseRepository,
                        movementRepository, layerRepository
                );

        InventoryMovementRepository mockMovementRepository =
                mock(InventoryMovementRepository.class);
        ItemRepository mockItemRepository = mock(ItemRepository.class);
        InventoryValuationService mockValuationService =
                mock(InventoryValuationService.class);

        InventoryMovementService movementService =
                new InventoryMovementService(
                        mockMovementRepository,
                        mockItemRepository,
                        new InventoryLocationValidationService(
                                warehouseRepository, locationRepository
                        ),
                        mockValuationService,
                        warehouseRepository
                );

        CountDownLatch deactivated = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch movementStarted = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<?> first = executor.submit(() -> {
                TransactionTemplate tx = new TransactionTemplate(transactionManager);

                tx.executeWithoutResult(status -> {
                    status.setRollbackOnly();

                    deactivationService.deactivate(
                            target.tenant(), target.company(),
                            target.warehouse(), target.location()
                    );

                    entityManager.flush();

                    WarehouseLocation changed = locationRepository
                            .findByTenantIdAndWarehouseIdAndId(
                                    target.tenant(), target.warehouse(),
                                    target.location()
                            ).orElseThrow();

                    assertFalse(changed.isActive(),
                            "La disattivazione temporanea non è avvenuta.");

                    deactivated.countDown();

                    try {
                        if (!release.await(10, TimeUnit.SECONDS)) {
                            throw new AssertionError("Timeout rilascio transazione A.");
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(ex);
                    }
                });
            });

            assertTrue(deactivated.await(5, TimeUnit.SECONDS),
                    "La transazione A non ha completato la disattivazione.");

            Future<BusinessException> second = executor.submit(() -> {
                TransactionTemplate tx = new TransactionTemplate(transactionManager);

                return tx.execute(status -> {
                    movementStarted.countDown();

                    return assertThrows(BusinessException.class,
                            () -> movementService.registerMovement(
                                    target.tenant(), target.company(),
                                    target.warehouse(), target.location(),
                                    -1L, LocalDate.of(2026, 1, 1),
                                    "IN", "RACE_TEST",
                                    BigDecimal.ONE, BigDecimal.ONE,
                                    "TEST", -1L, "Nessun movimento persistente"
                            ));
                });
            });

            assertTrue(movementStarted.await(5, TimeUnit.SECONDS),
                    "La transazione B non è partita.");

            assertThrows(TimeoutException.class,
                    () -> second.get(500, TimeUnit.MILLISECONDS),
                    "Il movimento non ha atteso il lock.");

            release.countDown();
            first.get(10, TimeUnit.SECONDS);

            BusinessException exception = second.get(10, TimeUnit.SECONDS);

            assertTrue(exception.getMessage().contains("Articolo non trovato"),
                    "Dopo il rollback il movimento deve superare la validazione "
                    + "dell'ubicazione e fermarsi sull'articolo inesistente.");

            verifyNoInteractions(
                    mockMovementRepository,
                    mockValuationService
            );

            Boolean stillActive = selection.execute(status ->
                    locationRepository.findByTenantIdAndWarehouseIdAndId(
                            target.tenant(), target.warehouse(),
                            target.location()
                    ).orElseThrow().isActive()
            );

            assertEquals(Boolean.TRUE, stillActive,
                    "Il rollback deve preservare lo stato attivo.");

        } finally {
            release.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(15, TimeUnit.SECONDS),
                    "Le transazioni di test non sono terminate.");
        }
    }
}
