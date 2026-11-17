package com.gestiva.warehouse.location.repository;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.support.TestDatabaseSafetyInitializer;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.movement.service.InventoryLocationValidationService;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.location.entity.Warehouse;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
@Transactional
class WarehouseLocationDeactivationJpaRollbackTest {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private WarehouseLocationRepository locationRepository;

    @Autowired
    private InventoryMovementRepository movementRepository;

    @Autowired
    private InventoryLayerRepository layerRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void emptyLocationIsDeactivatedAndRejectedByInventoryValidation() {
        Warehouse warehouse = warehouseRepository.findAll()
                .stream()
                .filter(w -> w.getTenantId() != null
                        && w.getCompany() != null
                        && w.isActive()
                        && w.getCompany().isActive())
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Nessun magazzino attivo disponibile nel TEST."
                ));

        Long tenantId = warehouse.getTenantId();
        Long companyId = warehouse.getCompany().getId();
        Long warehouseId = warehouse.getId();

        WarehouseLocation location = new WarehouseLocation();
        location.setTenantId(tenantId);
        location.setWarehouse(warehouse);
        location.setCode(
                "TX-" + UUID.randomUUID().toString().substring(0, 12)
        );
        location.setName("Ubicazione temporanea test rollback");
        location.setActive(true);

        location = locationRepository.saveAndFlush(location);

        Long locationId = location.getId();
        assertNotNull(locationId);

        WarehouseLocationService locationService =
                mock(WarehouseLocationService.class);

        when(locationService.getById(
                tenantId, companyId, warehouseId, locationId
        )).thenReturn(location);

        WarehouseLocationDeactivationService deactivationService =
                new WarehouseLocationDeactivationService(
                        locationService,
                        locationRepository,
                        warehouseRepository,
                        movementRepository,
                        layerRepository
                );

        deactivationService.deactivate(
                tenantId, companyId, warehouseId, locationId
        );

        entityManager.flush();
        entityManager.clear();

        WarehouseLocation persisted = locationRepository
                .findByTenantIdAndWarehouseIdAndId(
                        tenantId, warehouseId, locationId
                )
                .orElseThrow();

        assertFalse(persisted.isActive());

        InventoryLocationValidationService validationService =
                new InventoryLocationValidationService(
                        warehouseRepository,
                        locationRepository
                );

        assertThrows(
                BusinessException.class,
                () -> validationService.validateLocation(
                        tenantId, warehouseId, locationId
                )
        );
    }
}
