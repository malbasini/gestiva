package com.gestiva.warehouse.inventory.movement.controller;

import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.tenant.entity.Tenant;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.entity.WarehouseLocation;
import com.gestiva.warehouse.location.service.WarehouseService;
import com.gestiva.warehouse.location.service.WarehouseLocationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

class InventoryLocationTransferAvailabilityControllerTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 3L;
    private static final Long WAREHOUSE = 2L;
    private static final Long ITEM = 1L;

    private static final Long ACTIVE_LOCATION = 1L;
    private static final Long INACTIVE_LOCATION = 4L;

    private JdbcTemplate jdbc;
    private TenantContext tenantContext;
    private CompanyContext companyContext;
    private TenantRepository tenantRepository;
    private WarehouseService warehouseService;
    private WarehouseLocationService locationService;

    private Tenant tenant;

    private InventoryLocationTransferAvailabilityController
            controller;

    @BeforeEach
    void setup() {

        jdbc = mock(JdbcTemplate.class);

        tenantContext = mock(TenantContext.class);
        companyContext = mock(CompanyContext.class);
        tenantRepository = mock(TenantRepository.class);
        warehouseService = mock(WarehouseService.class);
        locationService = mock(WarehouseLocationService.class);

        tenant = mock(Tenant.class);

        when(tenantContext.getCurrentTenantId())
                .thenReturn(TENANT);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        when(tenantRepository.findById(TENANT))
                .thenReturn(Optional.of(tenant));

        when(tenant.isSubscriptionActive())
                .thenReturn(true);

        when(tenant.getSubscriptionPlan())
                .thenReturn("PROFESSIONAL");

        Warehouse warehouse = mock(Warehouse.class);

        when(warehouse.getId()).thenReturn(WAREHOUSE);

        when(warehouseService.findActive(TENANT, COMPANY))
                .thenReturn(List.of(warehouse));

        // Preparazione separata dei mock:
        // nessuno stubbing annidato nel thenReturn.

        WarehouseLocation active =
                mock(WarehouseLocation.class);

        when(active.getId()).thenReturn(ACTIVE_LOCATION);
        when(active.isActive()).thenReturn(true);

        WarehouseLocation inactive =
                mock(WarehouseLocation.class);

        when(inactive.getId()).thenReturn(INACTIVE_LOCATION);
        when(inactive.isActive()).thenReturn(false);

        when(locationService.findAll(
                TENANT, COMPANY, WAREHOUSE))
                .thenReturn(List.of(active, inactive));

        controller =
                new InventoryLocationTransferAvailabilityController(
                        jdbc,
                        tenantContext,
                        companyContext,
                        tenantRepository,
                        warehouseService,
                        locationService
                );
    }

    private static String normalizeSql(String sql) {

        return sql.toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private void stubUnlocated(BigDecimal quantity) {

        when(jdbc.queryForObject(
                anyString(),
                eq(BigDecimal.class),
                eq(TENANT),
                eq(COMPANY),
                eq(WAREHOUSE),
                eq(ITEM)
        )).thenReturn(quantity);
    }

    private String verifyUnlocatedQuery() {

        ArgumentCaptor<String> captor =
                ArgumentCaptor.forClass(String.class);

        verify(jdbc).queryForObject(
                captor.capture(),
                eq(BigDecimal.class),
                eq(TENANT),
                eq(COMPANY),
                eq(WAREHOUSE),
                eq(ITEM)
        );

        verifyNoMoreInteractions(jdbc);
        verifyNoInteractions(locationService);

        String sql = normalizeSql(captor.getValue());

        assertTrue(sql.contains(
                "coalesce(sum(l.remaining_qty), 0)"));

        assertTrue(sql.contains("from inventory_layer l"));

        assertTrue(sql.contains("join warehouse w"));

        assertTrue(sql.contains(
                "w.tenant_id = l.tenant_id"));

        assertTrue(sql.contains(
                "w.company_id = l.company_id"));

        assertTrue(sql.contains("join item i"));

        assertTrue(sql.contains(
                "i.tenant_id = l.tenant_id"));

        assertTrue(sql.contains(
                "where l.tenant_id = ?"
                + " and l.company_id = ?"
                + " and l.warehouse_id = ?"
                + " and l.item_id = ?"
                + " and l.location_id is null"
                + " and l.closed = b'0'"
                + " and l.remaining_qty > 0"));

        assertFalse(sql.contains("join warehouse_location"));

        assertFalse(sql.contains(" or "));

        assertEquals(
                4L,
                sql.chars().filter(c -> c == '?').count());

        return sql;
    }

    private void assertBadRequest(
            Long warehouse,
            Long item,
            String selection) {

        ResponseStatusException failure = assertThrows(
                ResponseStatusException.class,
                () -> controller.availability(
                        warehouse, item, selection)
        );

        assertEquals(BAD_REQUEST, failure.getStatusCode());

        verifyNoInteractions(jdbc);
    }

    @Test
    void unlocatedAvailabilityMustUseExactNullQuery() {

        stubUnlocated(new BigDecimal("48.000"));

        assertEquals(
                Map.of("availableQty", "48.000"),
                controller.availability(
                        WAREHOUSE, ITEM, "UNLOCATED")
        );

        verifyUnlocatedQuery();

        verify(warehouseService)
                .findActive(TENANT, COMPANY);
    }

    @Test
    void physicalAvailabilityMustPreserveScopedQuery() {

        when(jdbc.queryForObject(
                anyString(),
                eq(BigDecimal.class),
                eq(TENANT),
                eq(COMPANY),
                eq(WAREHOUSE),
                eq(ITEM),
                eq(ACTIVE_LOCATION)
        )).thenReturn(new BigDecimal("4.000"));

        assertEquals(
                Map.of("availableQty", "4.000"),
                controller.availability(
                        WAREHOUSE, ITEM, "1")
        );

        ArgumentCaptor<String> captor =
                ArgumentCaptor.forClass(String.class);

        verify(jdbc).queryForObject(
                captor.capture(),
                eq(BigDecimal.class),
                eq(TENANT),
                eq(COMPANY),
                eq(WAREHOUSE),
                eq(ITEM),
                eq(ACTIVE_LOCATION)
        );

        String sql = normalizeSql(captor.getValue());

        assertTrue(sql.contains(
                "coalesce(sum(l.remaining_qty), 0)"));

        assertTrue(sql.contains(
                "join warehouse_location loc"));

        assertTrue(sql.contains(
                "loc.id = l.location_id"));

        assertTrue(sql.contains(
                "loc.warehouse_id = w.id"));

        assertTrue(sql.contains(
                "loc.tenant_id = l.tenant_id"));

        assertTrue(sql.contains(
                "where l.tenant_id = ?"
                + " and l.company_id = ?"
                + " and l.warehouse_id = ?"
                + " and l.item_id = ?"
                + " and l.location_id = ?"
                + " and l.remaining_qty > 0"));

        assertFalse(sql.contains(
                "l.location_id is null"));

        assertEquals(
                5L,
                sql.chars().filter(c -> c == '?').count());

        verify(locationService)
                .findAll(TENANT, COMPANY, WAREHOUSE);

        verifyNoMoreInteractions(jdbc);
    }

    @Test
    void emptyUnlocatedStockMustReturnZero() {

        stubUnlocated(new BigDecimal("0.000"));

        var result = controller.availability(
                WAREHOUSE, ITEM, "UNLOCATED");

        assertEquals(
                "0.000",
                result.get("availableQty"));

        verifyUnlocatedQuery();
    }

    @Test
    void nullJdbcResultMustReturnZero() {

        stubUnlocated(null);

        assertEquals(
                Map.of("availableQty", "0"),
                controller.availability(
                        WAREHOUSE, ITEM, "UNLOCATED")
        );

        verifyUnlocatedQuery();
    }

    @Test
    void missingOriginMustReturnBadRequest() {

        assertBadRequest(WAREHOUSE, ITEM, null);
    }

    @Test
    void emptyOriginMustReturnBadRequest() {

        assertBadRequest(WAREHOUSE, ITEM, "");
    }

    @Test
    void invalidOriginMustReturnBadRequest() {

        assertBadRequest(WAREHOUSE, ITEM, "null");
    }

    @Test
    void zeroOriginMustReturnBadRequest() {

        assertBadRequest(WAREHOUSE, ITEM, "0");
    }

    @Test
    void missingWarehouseMustReturnBadRequest() {

        assertBadRequest(null, ITEM, "UNLOCATED");
    }

    @Test
    void missingItemMustReturnBadRequest() {

        assertBadRequest(WAREHOUSE, null, "UNLOCATED");
    }

    @Test
    void foreignWarehouseMustNotQueryStock() {

        assertBadRequest(999L, ITEM, "UNLOCATED");

        verifyNoInteractions(locationService);
    }

    @Test
    void foreignPhysicalLocationMustNotQueryStock() {

        assertBadRequest(WAREHOUSE, ITEM, "999");
    }

    @Test
    void inactivePhysicalLocationMustNotQueryStock() {

        assertBadRequest(
                WAREHOUSE,
                ITEM,
                INACTIVE_LOCATION.toString()
        );
    }

    @Test
    void inactiveSubscriptionMustNotQueryStock() {

        when(tenant.isSubscriptionActive())
                .thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> controller.availability(
                        WAREHOUSE, ITEM, "UNLOCATED")
        );

        verifyNoInteractions(
                jdbc,
                warehouseService,
                locationService,
                companyContext
        );
    }

    @Test
    void nonProfessionalPlanMustNotQueryStock() {

        when(tenant.getSubscriptionPlan())
                .thenReturn("STANDARD");

        assertThrows(
                AccessDeniedException.class,
                () -> controller.availability(
                        WAREHOUSE, ITEM, "UNLOCATED")
        );

        verifyNoInteractions(
                jdbc,
                warehouseService,
                locationService,
                companyContext
        );
    }
}
