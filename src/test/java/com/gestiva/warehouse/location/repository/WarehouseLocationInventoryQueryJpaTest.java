package com.gestiva.warehouse.location.repository;

import com.gestiva.support.TestDatabaseSafetyInitializer;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestDatabaseSafetyInitializer.class)
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none"
})
@Transactional(readOnly = true)
class WarehouseLocationInventoryQueryJpaTest {

    private static final Long TENANT_ID = 2L;
    private static final Long COMPANY_ID = 1L;
    private static final Long WAREHOUSE_ID = 2L;
    private static final Long LOCATION_ID = 1L;
    private static final Long MISSING_LOCATION_ID = Long.MAX_VALUE;

    @Autowired
    private InventoryMovementRepository movementRepository;

    @Autowired
    private InventoryLayerRepository layerRepository;

    @Test
    void inventoryBalanceQueryExecutesSuccessfully() {
        var itemIds = movementRepository
                .findItemIdsWithNonZeroBalanceByLocation(
                        TENANT_ID,
                        COMPANY_ID,
                        WAREHOUSE_ID,
                        LOCATION_ID
                );

        assertNotNull(itemIds);
        assertFalse(itemIds.contains(null));
    }

    @Test
    void nonexistentLocationHasNoInventoryBalance() {
        var itemIds = movementRepository
                .findItemIdsWithNonZeroBalanceByLocation(
                        TENANT_ID,
                        COMPANY_ID,
                        WAREHOUSE_ID,
                        MISSING_LOCATION_ID
                );

        assertTrue(itemIds.isEmpty());
    }

    @Test
    void remainingFifoLayersQueryExecutesSuccessfully() {
        long count = layerRepository
                .countRemainingLayersByLocation(
                        TENANT_ID,
                        COMPANY_ID,
                        WAREHOUSE_ID,
                        LOCATION_ID
                );

        assertTrue(count >= 0L);
    }

    @Test
    void nonexistentLocationHasNoRemainingFifoLayers() {
        long count = layerRepository
                .countRemainingLayersByLocation(
                        TENANT_ID,
                        COMPANY_ID,
                        WAREHOUSE_ID,
                        MISSING_LOCATION_ID
                );

        assertEquals(0L, count);
    }
}
