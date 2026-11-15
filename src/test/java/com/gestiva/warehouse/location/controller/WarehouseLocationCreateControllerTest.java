package com.gestiva.warehouse.location.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.warehouse.location.entity.Warehouse;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseLocationCreateControllerTest {

    private static final Long TENANT_ID = 2L;
    private static final Long COMPANY_ID = 1L;
    private static final Long WAREHOUSE_ID = 10L;

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

    private void configureAvailableWarehouse() {
        Warehouse warehouse = mock(Warehouse.class);

        when(warehouse.getId()).thenReturn(WAREHOUSE_ID);

        when(warehouseService.findActive(TENANT_ID, COMPANY_ID))
                .thenReturn(List.of(warehouse));
    }

    private WarehouseLocationForm validForm() {
        WarehouseLocationForm form = new WarehouseLocationForm();

        form.setWarehouseId(WAREHOUSE_ID);
        form.setCode("A-01");
        form.setName("Area A");
        form.setLocationType(WarehouseLocationType.STORAGE);

        return form;
    }

    @Test
    void validCreateCallsServiceAndRedirects() {
        configureCompany();
        configureAvailableWarehouse();

        WarehouseLocationForm form = validForm();

        var binding = new BeanPropertyBindingResult(
                form, "locationForm"
        );
        var model = new ExtendedModelMap();
        var redirects = new RedirectAttributesModelMap();

        String view = controller().create(
                form, binding, model, redirects
        );

        assertEquals("redirect:/warehouse/locations", view);
        assertEquals(
                WAREHOUSE_ID.toString(),
                redirects.get("warehouseId")
        );
        assertEquals(
                "Ubicazione creata correttamente.",
                redirects.getFlashAttributes().get("successMessage")
        );

        verify(locationService).create(
                TENANT_ID,
                COMPANY_ID,
                WAREHOUSE_ID,
                null,
                "A-01",
                "Area A",
                WarehouseLocationType.STORAGE,
                null
        );
    }

    @Test
    void foreignWarehouseCannotCreateLocation() {
        configureCompany();

        when(warehouseService.findActive(TENANT_ID, COMPANY_ID))
                .thenReturn(List.of());

        WarehouseLocationForm form = validForm();

        var binding = new BeanPropertyBindingResult(
                form, "locationForm"
        );
        var redirects = new RedirectAttributesModelMap();

        String view = controller().create(
                form,
                binding,
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
    void missingCompanyCannotCreateLocation() {
        when(tenantContext.getCurrentTenantId())
                .thenReturn(TENANT_ID);

        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new IllegalStateException(
                        "Nessuna società selezionata"
                ));

        WarehouseLocationForm form = validForm();

        var redirects = new RedirectAttributesModelMap();

        String view = controller().create(
                form,
                new BeanPropertyBindingResult(
                        form, "locationForm"
                ),
                new ExtendedModelMap(),
                redirects
        );

        assertEquals("redirect:/company/select", view);
        assertNotNull(
                redirects.getFlashAttributes().get("errorMessage")
        );

        verifyNoInteractions(warehouseService, locationService);
    }

    @Test
    void bindingErrorsDoNotCreateLocation() {
        configureCompany();
        configureAvailableWarehouse();

        WarehouseLocationForm form = validForm();
        form.setCode("");

        var binding = new BeanPropertyBindingResult(
                form, "locationForm"
        );
        binding.rejectValue(
                "code", "NotBlank", "Codice obbligatorio"
        );

        when(locationService.findAll(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID
        )).thenReturn(List.of());

        var model = new ExtendedModelMap();

        String view = controller().create(
                form,
                binding,
                model,
                new RedirectAttributesModelMap()
        );

        assertEquals(
                "warehouse/location/location-form", view
        );
        assertEquals("", form.getCode());
        assertTrue(binding.hasErrors());

        verify(locationService, never()).create(
                anyLong(),
                anyLong(),
                anyLong(),
                nullable(Long.class),
                anyString(),
                anyString(),
                any(WarehouseLocationType.class),
                nullable(String.class)
        );
    }

    @Test
    void businessErrorReturnsFormWithoutSuccess() {
        configureCompany();
        configureAvailableWarehouse();

        WarehouseLocationForm form = validForm();

        when(locationService.create(
                TENANT_ID,
                COMPANY_ID,
                WAREHOUSE_ID,
                null,
                "A-01",
                "Area A",
                WarehouseLocationType.STORAGE,
                null
        )).thenThrow(new BusinessException(
                "Codice ubicazione duplicato."
        ));

        when(locationService.findAll(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID
        )).thenReturn(List.of());

        var model = new ExtendedModelMap();
        var redirects = new RedirectAttributesModelMap();

        String view = controller().create(
                form,
                new BeanPropertyBindingResult(
                        form, "locationForm"
                ),
                model,
                redirects
        );

        assertEquals(
                "warehouse/location/location-form", view
        );
        assertEquals(
                "Codice ubicazione duplicato.",
                model.get("errorMessage")
        );
        assertFalse(
                redirects.getFlashAttributes()
                        .containsKey("successMessage")
        );
    }
}
