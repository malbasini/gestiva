package com.gestiva.warehouse.location.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.entity.WarehouseLocation;
import com.gestiva.warehouse.location.service.WarehouseLocationService;
import com.gestiva.warehouse.location.service.WarehouseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseLocationPageControllerTest {

    private static final Long TENANT_ID = 2L;
    private static final Long COMPANY_ID = 1L;
    private static final Long WAREHOUSE_ID = 10L;

    @Mock
    private WarehouseLocationService locationService;

    @Mock
    private com.gestiva.warehouse.location.service.WarehouseLocationDeactivationService deactivationService;

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private TenantContext tenantContext;

    @Mock
    private CompanyContext companyContext;

    private WarehouseLocationPageController controller() {
        return new WarehouseLocationPageController(
                locationService,
                deactivationService,
                warehouseService,
                tenantContext,
                companyContext
        );
    }

    private void configureCompany() {
        when(tenantContext.getCurrentTenantId())
                .thenReturn(TENANT_ID);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);
    }

    private Warehouse warehouse(Long id) {
        Warehouse warehouse = mock(Warehouse.class);
        when(warehouse.getId()).thenReturn(id);
        return warehouse;
    }

    @Test
    void missingCompanyRedirectsWithoutReadingWarehouses() {
        when(tenantContext.getCurrentTenantId())
                .thenReturn(TENANT_ID);

        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new IllegalStateException(
                        "Nessuna società selezionata"
                ));

        var model = new ExtendedModelMap();
        var redirects = new RedirectAttributesModelMap();

        String view = controller().list(
                WAREHOUSE_ID,
                model,
                redirects
        );

        assertEquals("redirect:/company/select", view);
        assertNotNull(
                redirects.getFlashAttributes().get("errorMessage")
        );

        verifyNoInteractions(warehouseService, locationService);
    }

    @Test
    void noWarehouseSelectedReturnsEmptyLocations() {
        configureCompany();

        when(warehouseService.findActive(TENANT_ID, COMPANY_ID))
                .thenReturn(List.of());

        var model = new ExtendedModelMap();

        String view = controller().list(
                null,
                model,
                new RedirectAttributesModelMap()
        );

        assertEquals("warehouse/location/location-list", view);
        assertEquals(List.of(), model.get("locations"));
        assertNull(model.get("selectedWarehouseId"));

        verifyNoInteractions(locationService);
    }

    @Test
    void validWarehouseLoadsScopedLocations() {
        configureCompany();

        Warehouse selected = warehouse(WAREHOUSE_ID);
        WarehouseLocation location = mock(WarehouseLocation.class);

        when(warehouseService.findActive(TENANT_ID, COMPANY_ID))
                .thenReturn(List.of(selected));

        when(locationService.findAll(
                TENANT_ID,
                COMPANY_ID,
                WAREHOUSE_ID
        )).thenReturn(List.of(location));

        var model = new ExtendedModelMap();

        String view = controller().list(
                WAREHOUSE_ID,
                model,
                new RedirectAttributesModelMap()
        );

        assertEquals("warehouse/location/location-list", view);
        assertEquals(WAREHOUSE_ID, model.get("selectedWarehouseId"));
        assertEquals(List.of(location), model.get("locations"));

        verify(locationService).findAll(
                TENANT_ID,
                COMPANY_ID,
                WAREHOUSE_ID
        );
    }

    @Test
    void foreignWarehouseIsRejectedBeforeLocationLookup() {
        configureCompany();

        when(warehouseService.findActive(TENANT_ID, COMPANY_ID))
                .thenReturn(List.of());

        var model = new ExtendedModelMap();

        String view = controller().list(
                999L,
                model,
                new RedirectAttributesModelMap()
        );

        assertEquals("warehouse/location/location-list", view);
        assertNotNull(model.get("errorMessage"));
        assertNull(model.get("selectedWarehouseId"));
        assertEquals(List.of(), model.get("locations"));

        verifyNoInteractions(locationService);
    }

    @Test
    void unavailableWarehouseIsRejected() {
        configureCompany();

        Warehouse available = warehouse(WAREHOUSE_ID);

        when(warehouseService.findActive(TENANT_ID, COMPANY_ID))
                .thenReturn(List.of(available));

        var model = new ExtendedModelMap();

        controller().list(
                20L,
                model,
                new RedirectAttributesModelMap()
        );

        assertNotNull(model.get("errorMessage"));
        assertEquals(List.of(), model.get("locations"));

        verifyNoInteractions(locationService);
    }

    @Test
    void locationServiceErrorIsDisplayed() {
        configureCompany();

        Warehouse selected = warehouse(WAREHOUSE_ID);

        when(warehouseService.findActive(TENANT_ID, COMPANY_ID))
                .thenReturn(List.of(selected));

        when(locationService.findAll(
                TENANT_ID,
                COMPANY_ID,
                WAREHOUSE_ID
        )).thenThrow(new BusinessException(
                "Errore nel recupero delle ubicazioni."
        ));

        var model = new ExtendedModelMap();

        String view = controller().list(
                WAREHOUSE_ID,
                model,
                new RedirectAttributesModelMap()
        );

        assertEquals("warehouse/location/location-list", view);
        assertEquals(
                "Errore nel recupero delle ubicazioni.",
                model.get("errorMessage")
        );
        assertNull(model.get("selectedWarehouseId"));
        assertEquals(List.of(), model.get("locations"));
    }
}
