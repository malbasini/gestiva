package com.gestiva.warehouse.inventory.valuation.controller;

import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.valuation.web.OutboundValuationListWebService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryValuationPageControllerCompanyCausalsTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;

    @Mock TenantContext tenantContext;
    @Mock CompanyContext companyContext;
    @Mock InventoryMovementRepository inventoryMovementRepository;
    @Mock OutboundValuationListWebService outboundValuationListWebService;

    private InventoryValuationPageController service() {
        var controller = mock(
                InventoryValuationPageController.class,
                CALLS_REAL_METHODS
        );

        org.springframework.test.util.ReflectionTestUtils.setField(
                controller, "tenantContext", tenantContext
        );

        org.springframework.test.util.ReflectionTestUtils.setField(
                controller, "companyContext", companyContext
        );

        org.springframework.test.util.ReflectionTestUtils.setField(
                controller,
                "inventoryMovementRepository",
                inventoryMovementRepository
        );

        org.springframework.test.util.ReflectionTestUtils.setField(
                controller,
                "outboundValuationListWebService",
                outboundValuationListWebService
        );

        return controller;
    }

    @Test
    void shouldUseOnlyCompanyScopedOutboundCausals() {
        when(tenantContext.getCurrentTenantId())
                .thenReturn(TENANT_ID);
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(inventoryMovementRepository
                .findDistinctOutboundCausalCodesByTenantIdAndCompanyId(
                        TENANT_ID, COMPANY_ID
                ))
                .thenReturn(List.of("DDT_OUT"));

        var model = new ExtendedModelMap();

        String view = service().outboundValuations(
                0, 10, null, null, null, null, model
        );

        assertEquals(
                "warehouse/inventory/inventory-outbound-valuations",
                view
        );
        assertEquals(
                List.of("DDT_OUT"),
                model.get("causalOptions")
        );

        verify(inventoryMovementRepository)
                .findDistinctOutboundCausalCodesByTenantIdAndCompanyId(
                        TENANT_ID, COMPANY_ID
                );

        verify(inventoryMovementRepository, never())
                .findDistinctOutboundCausalCodesByTenantId(anyLong());
    }

    @Test
    void shouldRejectMissingCompanyBeforeReadingCausals() {
        when(tenantContext.getCurrentTenantId())
                .thenReturn(TENANT_ID);

        when(companyContext.requireCurrentCompanyId())
                .thenThrow(
                        new IllegalStateException(
                                "Nessuna società attiva"
                        )
                );

        var model = new ExtendedModelMap();

        assertThrows(
                IllegalStateException.class,
                () -> service().outboundValuations(
                        0, 10, null, null, null, null, model
                )
        );

        verifyNoInteractions(inventoryMovementRepository);
    }
}
