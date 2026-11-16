package com.gestiva.warehouse.location.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.entity.WarehouseLocation;
import com.gestiva.warehouse.location.entity.WarehouseLocationType;
import com.gestiva.warehouse.location.service.WarehouseLocationService;
import com.gestiva.warehouse.location.service.WarehouseService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseLocationEditControllerTest {

    private static final Long TENANT_ID = 2L;
    private static final Long COMPANY_ID = 1L;
    private static final Long WAREHOUSE_ID = 10L;
    private static final Long LOCATION_ID = 30L;

    @Mock
    private WarehouseLocationService locationService;

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private TenantContext tenantContext;

    @Mock
    private CompanyContext companyContext;

    private WarehouseLocationPageController controller() {
        return new WarehouseLocationPageController(
                locationService,
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

    private void configureWarehouse() {
        Warehouse warehouse = mock(Warehouse.class);
        when(warehouse.getId()).thenReturn(WAREHOUSE_ID);

        when(warehouseService.findActive(TENANT_ID, COMPANY_ID))
                .thenReturn(List.of(warehouse));
    }

    private WarehouseLocation location() {
        WarehouseLocation location = new WarehouseLocation();
        location.setCode("A-01");
        location.setName("Area A");
        location.setLocationType(WarehouseLocationType.STORAGE);
        location.setBarcode("OLD-123");
        location.setActive(true);
        return location;
    }

    private WarehouseLocationForm form() {
        WarehouseLocationForm form = new WarehouseLocationForm();
        form.setWarehouseId(WAREHOUSE_ID);
        form.setCode("A-01");
        form.setName("Area aggiornata");
        form.setLocationType(WarehouseLocationType.QUARANTINE);
        form.setBarcode("NEW-123");
        return form;
    }

    private void configureExistingLocation(WarehouseLocation location) {
        when(locationService.getById(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID, LOCATION_ID
        )).thenReturn(location);

        when(locationService.findAll(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID
        )).thenReturn(List.of());
    }

    @Test
    void editGetPrepopulatesExistingLocation() {
        configureCompany();
        configureWarehouse();

        configureExistingLocation(location());

        var model = new ExtendedModelMap();

        String view = controller().editForm(
                LOCATION_ID,
                WAREHOUSE_ID,
                model,
                new RedirectAttributesModelMap()
        );

        assertEquals("warehouse/location/location-form", view);
        assertEquals(Boolean.TRUE, model.get("editMode"));
        assertEquals(LOCATION_ID, model.get("editingLocationId"));

        WarehouseLocationForm result =
                (WarehouseLocationForm) model.get("locationForm");

        assertNotNull(result);
        assertEquals(WAREHOUSE_ID, result.getWarehouseId());
        assertEquals("A-01", result.getCode());
        assertEquals("Area A", result.getName());
        assertEquals(WarehouseLocationType.STORAGE,
                result.getLocationType());
        assertEquals("OLD-123", result.getBarcode());
        assertNull(result.getParentLocationId());
    }

    @Test
    void validPostUpdatesMetadataAndRedirects() {
        configureCompany();
        configureWarehouse();
        configureExistingLocation(location());

        WarehouseLocationForm form = form();
        var redirects = new RedirectAttributesModelMap();

        String view = controller().update(
                LOCATION_ID,
                form,
                new BeanPropertyBindingResult(form, "locationForm"),
                new ExtendedModelMap(),
                redirects
        );

        assertEquals("redirect:/warehouse/locations", view);
        assertEquals(
                "Ubicazione modificata correttamente.",
                redirects.getFlashAttributes().get("successMessage")
        );
        assertEquals(WAREHOUSE_ID.toString(),
                redirects.get("warehouseId"));

        verify(locationService).updateMetadata(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID, LOCATION_ID,
                "Area aggiornata",
                WarehouseLocationType.QUARANTINE,
                "NEW-123"
        );
    }

    @Test
    void postIgnoresTamperedCodeAndParent() {
        configureCompany();
        configureWarehouse();
        configureExistingLocation(location());

        WarehouseLocationForm form = form();
        form.setCode("CODICE-MANOMESSO");
        form.setParentLocationId(999L);

        controller().update(
                LOCATION_ID,
                form,
                new BeanPropertyBindingResult(form, "locationForm"),
                new ExtendedModelMap(),
                new RedirectAttributesModelMap()
        );

        assertEquals("A-01", form.getCode());
        assertNull(form.getParentLocationId());

        verify(locationService).updateMetadata(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID, LOCATION_ID,
                "Area aggiornata",
                WarehouseLocationType.QUARANTINE,
                "NEW-123"
        );
    }

    @Test
    void foreignWarehouseIsRejectedBeforeLocationLookup() {
        configureCompany();

        when(warehouseService.findActive(TENANT_ID, COMPANY_ID))
                .thenReturn(List.of());

        WarehouseLocationForm form = form();
        var redirects = new RedirectAttributesModelMap();

        String view = controller().update(
                LOCATION_ID,
                form,
                new BeanPropertyBindingResult(form, "locationForm"),
                new ExtendedModelMap(),
                redirects
        );

        assertEquals("redirect:/warehouse/locations", view);
        assertNotNull(
                redirects.getFlashAttributes().get("errorMessage")
        );
        verifyNoInteractions(locationService);
    }

    @Test
    void bindingErrorsPreventMetadataUpdate() {
        configureCompany();
        configureWarehouse();
        configureExistingLocation(location());

        WarehouseLocationForm form = form();
        form.setName("");

        var binding = new BeanPropertyBindingResult(
                form, "locationForm"
        );
        binding.rejectValue(
                "name", "NotBlank", "Descrizione obbligatoria"
        );

        var model = new ExtendedModelMap();

        String view = controller().update(
                LOCATION_ID,
                form,
                binding,
                model,
                new RedirectAttributesModelMap()
        );

        assertEquals("warehouse/location/location-form", view);
        assertEquals(Boolean.TRUE, model.get("editMode"));
        assertTrue(binding.hasErrors());

        verify(locationService, never()).updateMetadata(
                any(), any(), any(), any(),
                any(), any(), any()
        );
    }

    @Test
    void businessErrorReturnsEditFormWithoutSuccess() {
        configureCompany();
        configureWarehouse();
        configureExistingLocation(location());

        WarehouseLocationForm form = form();

        when(locationService.updateMetadata(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID, LOCATION_ID,
                "Area aggiornata",
                WarehouseLocationType.QUARANTINE,
                "NEW-123"
        )).thenThrow(new BusinessException(
                "Modifica ubicazione non consentita."
        ));

        var model = new ExtendedModelMap();
        var redirects = new RedirectAttributesModelMap();

        String view = controller().update(
                LOCATION_ID,
                form,
                new BeanPropertyBindingResult(form, "locationForm"),
                model,
                redirects
        );

        assertEquals("warehouse/location/location-form", view);
        assertEquals(
                "Modifica ubicazione non consentita.",
                model.get("errorMessage")
        );
        assertFalse(
                redirects.getFlashAttributes()
                        .containsKey("successMessage")
        );
    }
}
