package com.gestiva.warehouse.location.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.platform.company.entity.Company;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.entity.WarehouseLocation;
import com.gestiva.warehouse.location.entity.WarehouseLocationType;
import com.gestiva.warehouse.location.repository.WarehouseLocationRepository;
import com.gestiva.warehouse.location.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseLocationServiceTest {

    private static final Long TENANT_ID = 2L;
    private static final Long COMPANY_ID = 5L;
    private static final Long WAREHOUSE_ID = 10L;
    private static final Long PARENT_ID = 20L;
    private static final Long LOCATION_ID = 30L;

    @Mock
    private WarehouseLocationRepository locationRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseLocationService service;

    private Company company;
    private Warehouse warehouse;
    private WarehouseLocation parent;

    @BeforeEach
    void setUp() {

        company = new Company();
        company.setTenantId(TENANT_ID);
        company.setActive(true);

        warehouse = new Warehouse();
        warehouse.setTenantId(TENANT_ID);
        warehouse.setCompany(company);
        warehouse.setActive(true);

        parent = new WarehouseLocation();
        parent.setTenantId(TENANT_ID);
        parent.setWarehouse(warehouse);
        parent.setCode("A");
        parent.setActive(true);
    }

    private void configureWarehouse() {
        when(warehouseRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID
        )).thenReturn(Optional.of(warehouse));
    }

    private void configureParent() {
        when(locationRepository.findByTenantIdAndWarehouseIdAndId(
                TENANT_ID, WAREHOUSE_ID, PARENT_ID
        )).thenReturn(Optional.of(parent));
    }

    private void configureSave() {
        when(locationRepository.save(any(WarehouseLocation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private WarehouseLocation createLocationFixture() {
        WarehouseLocation location = new WarehouseLocation();
        location.setTenantId(TENANT_ID);
        location.setWarehouse(warehouse);
        location.setCode("A1");
        location.setName("Scaffale A1");
        location.setLocationType(WarehouseLocationType.STORAGE);
        location.setActive(true);
        return location;
    }

    // =========================================================
    // CREAZIONE
    // =========================================================

    @Test
    void create_shouldSaveRootLocation() {

        configureWarehouse();
        configureSave();

        WarehouseLocation result = service.create(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID,
                null,
                " a ",
                " Corsia A ",
                null,
                " 12345 "
        );

        assertNotNull(result);
        assertEquals(TENANT_ID, result.getTenantId());
        assertSame(warehouse, result.getWarehouse());
        assertNull(result.getParentLocation());
        assertEquals("A", result.getCode());
        assertEquals("Corsia A", result.getName());
        assertEquals(WarehouseLocationType.STORAGE, result.getLocationType());
        assertEquals("12345", result.getBarcode());
        assertTrue(result.isActive());
    }

    @Test
    void create_shouldSaveChildLocation() {

        configureWarehouse();
        configureParent();
        configureSave();

        WarehouseLocation result = service.create(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID,
                PARENT_ID,
                "A1",
                "Scaffale A1",
                WarehouseLocationType.STORAGE,
                null
        );

        assertSame(parent, result.getParentLocation());
        assertEquals("A1", result.getCode());
    }

    @Test
    void create_shouldRejectMissingTenant() {

        assertThrows(BusinessException.class, () ->
                service.create(
                        null, COMPANY_ID, WAREHOUSE_ID,
                        null, "A", "Corsia A", null, null
                )
        );

        verifyNoInteractions(warehouseRepository, locationRepository);
    }

    @Test
    void create_shouldRejectMissingCompany() {

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID, null, WAREHOUSE_ID,
                        null, "A", "Corsia A", null, null
                )
        );

        verifyNoInteractions(warehouseRepository, locationRepository);
    }

    @Test
    void create_shouldRejectMissingWarehouseId() {

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID, COMPANY_ID, null,
                        null, "A", "Corsia A", null, null
                )
        );

        verifyNoInteractions(warehouseRepository, locationRepository);
    }

    @Test
    void create_shouldRejectWarehouseFromAnotherCompany() {

        when(warehouseRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID
        )).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID,
                        null, "A", "Corsia A", null, null
                )
        );

        verify(locationRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectInactiveWarehouse() {

        warehouse.setActive(false);
        configureWarehouse();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID,
                        null, "A", "Corsia A", null, null
                )
        );

        verify(locationRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectInactiveCompany() {

        company.setActive(false);
        configureWarehouse();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID,
                        null, "A", "Corsia A", null, null
                )
        );

        verify(locationRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectBlankCode() {

        configureWarehouse();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID,
                        null, " ", "Corsia A", null, null
                )
        );
    }

    @Test
    void create_shouldRejectBlankName() {

        configureWarehouse();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID,
                        null, "A", " ", null, null
                )
        );
    }

    @Test
    void create_shouldRejectLongCode() {

        configureWarehouse();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID,
                        null, "A".repeat(51), "Corsia A", null, null
                )
        );
    }

    @Test
    void create_shouldRejectLongName() {

        configureWarehouse();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID,
                        null, "A", "X".repeat(181), null, null
                )
        );
    }

    @Test
    void create_shouldRejectLongBarcode() {

        configureWarehouse();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID,
                        null, "A", "Corsia A", null, "1".repeat(101)
                )
        );
    }

    @Test
    void create_shouldRejectDuplicateCode() {

        configureWarehouse();

        when(locationRepository.existsByTenantIdAndWarehouseIdAndCode(
                TENANT_ID, WAREHOUSE_ID, "A"
        )).thenReturn(true);

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID,
                        null, "a", "Corsia A", null, null
                )
        );

        verify(locationRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectParentFromAnotherWarehouse() {

        configureWarehouse();

        when(locationRepository.findByTenantIdAndWarehouseIdAndId(
                TENANT_ID, WAREHOUSE_ID, PARENT_ID
        )).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID,
                        PARENT_ID, "A1", "Scaffale", null, null
                )
        );

        verify(locationRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectInactiveParent() {

        parent.setActive(false);
        configureWarehouse();
        configureParent();

        assertThrows(BusinessException.class, () ->
                service.create(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID,
                        PARENT_ID, "A1", "Scaffale", null, null
                )
        );

        verify(locationRepository, never()).save(any());
    }

    // =========================================================
    // CONSULTAZIONE
    // =========================================================

    @Test
    void getById_shouldReturnLocation() {

        configureWarehouse();

        WarehouseLocation location = createLocationFixture();

        when(locationRepository.findByTenantIdAndWarehouseIdAndId(
                TENANT_ID, WAREHOUSE_ID, LOCATION_ID
        )).thenReturn(Optional.of(location));

        assertSame(
                location,
                service.getById(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID, LOCATION_ID
                )
        );
    }

    @Test
    void getById_shouldRejectMissingLocation() {

        configureWarehouse();

        when(locationRepository.findByTenantIdAndWarehouseIdAndId(
                TENANT_ID, WAREHOUSE_ID, LOCATION_ID
        )).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                service.getById(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID, LOCATION_ID
                )
        );
    }

    @Test
    void findAll_shouldReturnLocations() {

        configureWarehouse();

        WarehouseLocation location = createLocationFixture();

        when(locationRepository.findByTenantIdAndWarehouseIdOrderByCodeAsc(
                TENANT_ID, WAREHOUSE_ID
        )).thenReturn(List.of(location));

        List<WarehouseLocation> result =
                service.findAll(TENANT_ID, COMPANY_ID, WAREHOUSE_ID);

        assertEquals(1, result.size());
        assertSame(location, result.get(0));
    }

    @Test
    void findRootLocations_shouldReturnRootLocations() {

        configureWarehouse();

        WarehouseLocation location = createLocationFixture();

        when(locationRepository
                .findByTenantIdAndWarehouseIdAndParentLocationIsNull(
                        TENANT_ID, WAREHOUSE_ID
                )).thenReturn(List.of(location));

        List<WarehouseLocation> result =
                service.findRootLocations(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID
                );

        assertEquals(1, result.size());
    }

    @Test
    void findChildren_shouldReturnChildLocations() {

        configureWarehouse();
        configureParent();

        WarehouseLocation child = createLocationFixture();

        when(locationRepository
                .findByTenantIdAndWarehouseIdAndParentLocationId(
                        TENANT_ID, WAREHOUSE_ID, PARENT_ID
                )).thenReturn(List.of(child));

        List<WarehouseLocation> result =
                service.findChildren(
                        TENANT_ID, COMPANY_ID, WAREHOUSE_ID, PARENT_ID
                );

        assertEquals(1, result.size());
        assertSame(child, result.get(0));
    }
}