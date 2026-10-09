package com.gestiva.warehouse.location.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.platform.company.entity.Company;
import com.gestiva.platform.company.repository.CompanyRepository;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.entity.WarehouseType;
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
class WarehouseServiceTest {

    private static final Long TENANT_ID = 2L;
    private static final Long COMPANY_ID = 5L;
    private static final Long WAREHOUSE_ID = 10L;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private WarehouseService service;

    private Company company;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setTenantId(TENANT_ID);
        company.setActive(true);
    }

    private void configureValidCompany() {
        when(companyRepository.findByTenantIdAndId(
                TENANT_ID, COMPANY_ID
        )).thenReturn(Optional.of(company));
    }

    private Warehouse createWarehouseFixture() {
        Warehouse warehouse = new Warehouse();
        warehouse.setTenantId(TENANT_ID);
        warehouse.setCompany(company);
        warehouse.setCode("MAG01");
        warehouse.setName("Magazzino principale");
        warehouse.setWarehouseType(WarehouseType.PHYSICAL);
        warehouse.setAllowNegativeStock(false);
        warehouse.setActive(true);
        return warehouse;
    }

    // =========================================================
    // CREAZIONE MAGAZZINI
    // =========================================================

    @Test
    void create_shouldSaveValidWarehouse() {

        configureValidCompany();

        when(warehouseRepository.save(any(Warehouse.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Warehouse result = service.create(
                TENANT_ID,
                COMPANY_ID,
                "mag01",
                " Magazzino principale ",
                " Deposito centrale ",
                WarehouseType.PHYSICAL
        );

        assertNotNull(result);
        assertEquals(TENANT_ID, result.getTenantId());
        assertSame(company, result.getCompany());
        assertEquals("MAG01", result.getCode());
        assertEquals("Magazzino principale", result.getName());
        assertEquals("Deposito centrale", result.getDescription());
        assertEquals(WarehouseType.PHYSICAL, result.getWarehouseType());
        assertFalse(result.isAllowNegativeStock());
        assertTrue(result.isActive());

        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    void create_shouldUsePhysicalAsDefaultType() {

        configureValidCompany();

        when(warehouseRepository.save(any(Warehouse.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Warehouse result = service.create(
                TENANT_ID,
                COMPANY_ID,
                "MAG01",
                "Magazzino principale",
                null,
                null
        );

        assertEquals(WarehouseType.PHYSICAL, result.getWarehouseType());
    }

    @Test
    void create_shouldRejectMissingTenant() {

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        null,
                        COMPANY_ID,
                        "MAG01",
                        "Magazzino",
                        null,
                        WarehouseType.PHYSICAL
                )
        );

        verifyNoInteractions(companyRepository, warehouseRepository);
    }

    @Test
    void create_shouldRejectMissingCompanyId() {

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID,
                        null,
                        "MAG01",
                        "Magazzino",
                        null,
                        WarehouseType.PHYSICAL
                )
        );

        verifyNoInteractions(companyRepository, warehouseRepository);
    }

    @Test
    void create_shouldRejectCompanyFromAnotherTenant() {

        when(companyRepository.findByTenantIdAndId(
                TENANT_ID, COMPANY_ID
        )).thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID,
                        COMPANY_ID,
                        "MAG01",
                        "Magazzino",
                        null,
                        WarehouseType.PHYSICAL
                )
        );

        verify(warehouseRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectInactiveCompany() {

        company.setActive(false);
        configureValidCompany();

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID,
                        COMPANY_ID,
                        "MAG01",
                        "Magazzino",
                        null,
                        WarehouseType.PHYSICAL
                )
        );

        verify(warehouseRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectBlankCode() {

        configureValidCompany();

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID,
                        COMPANY_ID,
                        "   ",
                        "Magazzino",
                        null,
                        WarehouseType.PHYSICAL
                )
        );

        verify(warehouseRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectBlankName() {

        configureValidCompany();

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID,
                        COMPANY_ID,
                        "MAG01",
                        "   ",
                        null,
                        WarehouseType.PHYSICAL
                )
        );

        verify(warehouseRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectCodeLongerThan30Characters() {

        configureValidCompany();

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID,
                        COMPANY_ID,
                        "A".repeat(31),
                        "Magazzino",
                        null,
                        WarehouseType.PHYSICAL
                )
        );

        verify(warehouseRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectNameLongerThan180Characters() {

        configureValidCompany();

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID,
                        COMPANY_ID,
                        "MAG01",
                        "A".repeat(181),
                        null,
                        WarehouseType.PHYSICAL
                )
        );

        verify(warehouseRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectDescriptionLongerThan500Characters() {

        configureValidCompany();

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID,
                        COMPANY_ID,
                        "MAG01",
                        "Magazzino",
                        "A".repeat(501),
                        WarehouseType.PHYSICAL
                )
        );

        verify(warehouseRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectDuplicateCode() {

        configureValidCompany();

        when(warehouseRepository.existsByTenantIdAndCompanyIdAndCode(
                TENANT_ID,
                COMPANY_ID,
                "MAG01"
        )).thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID,
                        COMPANY_ID,
                        "mag01",
                        "Magazzino",
                        null,
                        WarehouseType.PHYSICAL
                )
        );

        verify(warehouseRepository, never()).save(any());
    }

    // =========================================================
    // RICERCA MAGAZZINI
    // =========================================================

    @Test
    void getById_shouldReturnWarehouse() {

        Warehouse warehouse = createWarehouseFixture();

        when(warehouseRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID,
                COMPANY_ID,
                WAREHOUSE_ID
        )).thenReturn(Optional.of(warehouse));

        Warehouse result = service.getById(
                TENANT_ID,
                COMPANY_ID,
                WAREHOUSE_ID
        );

        assertSame(warehouse, result);
    }

    @Test
    void getById_shouldRejectWarehouseNotBelongingToCompany() {

        when(warehouseRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID,
                COMPANY_ID,
                WAREHOUSE_ID
        )).thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> service.getById(
                        TENANT_ID,
                        COMPANY_ID,
                        WAREHOUSE_ID
                )
        );
    }

    @Test
    void findAll_shouldReturnCompanyWarehouses() {

        Warehouse warehouse = createWarehouseFixture();

        when(warehouseRepository.findByTenantIdAndCompanyIdOrderByCodeAsc(
                TENANT_ID,
                COMPANY_ID
        )).thenReturn(List.of(warehouse));

        List<Warehouse> result = service.findAll(
                TENANT_ID,
                COMPANY_ID
        );

        assertEquals(1, result.size());
        assertSame(warehouse, result.get(0));
    }

    @Test
    void findActive_shouldReturnOnlyActiveWarehouses() {

        Warehouse warehouse = createWarehouseFixture();

        when(warehouseRepository
                .findByTenantIdAndCompanyIdAndActiveTrueOrderByCodeAsc(
                        TENANT_ID,
                        COMPANY_ID
                )).thenReturn(List.of(warehouse));

        List<Warehouse> result = service.findActive(
                TENANT_ID,
                COMPANY_ID
        );

        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
    }
}