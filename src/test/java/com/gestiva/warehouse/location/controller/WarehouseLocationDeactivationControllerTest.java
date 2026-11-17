package com.gestiva.warehouse.location.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.warehouse.location.entity.Warehouse;
import com.gestiva.warehouse.location.service.WarehouseLocationDeactivationService;
import com.gestiva.warehouse.location.service.WarehouseLocationService;
import com.gestiva.warehouse.location.service.WarehouseService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseLocationDeactivationControllerTest {

    private static final Long TENANT_ID = 2L;
    private static final Long COMPANY_ID = 1L;
    private static final Long WAREHOUSE_ID = 10L;
    private static final Long LOCATION_ID = 30L;

    @Mock
    private WarehouseLocationService locationService;

    @Mock
    private WarehouseLocationDeactivationService deactivationService;

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

    private void activeCompanyAndWarehouse() {
        when(tenantContext.getCurrentTenantId()).thenReturn(TENANT_ID);
        when(companyContext.requireCurrentCompanyId()).thenReturn(COMPANY_ID);

        Warehouse warehouse = mock(Warehouse.class);
        when(warehouse.getId()).thenReturn(WAREHOUSE_ID);

        when(warehouseService.findActive(TENANT_ID, COMPANY_ID))
                .thenReturn(List.of(warehouse));
    }

    @Test
    void successfulDeactivationRedirectsWithSuccessMessage() {
        activeCompanyAndWarehouse();
        var redirect = new RedirectAttributesModelMap();

        String result = controller().deactivate(
                LOCATION_ID, WAREHOUSE_ID, redirect
        );

        assertEquals("redirect:/warehouse/locations", result);
        assertEquals(
                "Ubicazione disattivata correttamente.",
                redirect.getFlashAttributes().get("successMessage")
        );
        assertEquals(
                WAREHOUSE_ID.toString(),
                redirect.get("warehouseId")
        );

        verify(deactivationService).deactivate(
                TENANT_ID, COMPANY_ID, WAREHOUSE_ID, LOCATION_ID
        );
    }

    @Test
    void unauthorizedWarehouseIsRejected() {
        when(tenantContext.getCurrentTenantId()).thenReturn(TENANT_ID);
        when(companyContext.requireCurrentCompanyId()).thenReturn(COMPANY_ID);
        when(warehouseService.findActive(TENANT_ID, COMPANY_ID))
                .thenReturn(List.of());

        var redirect = new RedirectAttributesModelMap();

        String result = controller().deactivate(
                LOCATION_ID, WAREHOUSE_ID, redirect
        );

        assertEquals("redirect:/warehouse/locations", result);
        assertEquals(
                "Magazzino non disponibile per la società corrente.",
                redirect.getFlashAttributes().get("errorMessage")
        );
        verifyNoInteractions(deactivationService);
    }

    @Test
    void missingCompanyRedirectsToCompanySelection() {
        when(tenantContext.getCurrentTenantId()).thenReturn(TENANT_ID);
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new IllegalStateException("Società assente."));

        var redirect = new RedirectAttributesModelMap();

        String result = controller().deactivate(
                LOCATION_ID, WAREHOUSE_ID, redirect
        );

        assertEquals("redirect:/company/select", result);
        assertNotNull(
                redirect.getFlashAttributes().get("errorMessage")
        );
        verifyNoInteractions(warehouseService, deactivationService);
    }

    @Test
    void businessFailureDoesNotProduceSuccessMessage() {
        activeCompanyAndWarehouse();

        doThrow(new BusinessException("Ubicazione con giacenze residue."))
                .when(deactivationService)
                .deactivate(
                        TENANT_ID, COMPANY_ID,
                        WAREHOUSE_ID, LOCATION_ID
                );

        var redirect = new RedirectAttributesModelMap();

        String result = controller().deactivate(
                LOCATION_ID, WAREHOUSE_ID, redirect
        );

        assertEquals("redirect:/warehouse/locations", result);
        assertEquals(
                "Ubicazione con giacenze residue.",
                redirect.getFlashAttributes().get("errorMessage")
        );
        assertFalse(
                redirect.getFlashAttributes()
                        .containsKey("successMessage")
        );
    }
}
