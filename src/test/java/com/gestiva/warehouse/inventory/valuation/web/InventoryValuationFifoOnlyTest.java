package com.gestiva.warehouse.inventory.valuation.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.tenant.entity.Tenant;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.warehouse.inventory.valuation.model.InventoryValuationMethod;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryAverageBalanceRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.item.entity.Item;
import com.gestiva.warehouse.item.repository.ItemRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryValuationFifoOnlyTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;
    private static final Long ITEM_ID = 77L;

    @Mock CompanyContext companyContext;
    @Mock ItemRepository itemRepository;
    @Mock TenantRepository tenantRepository;
    @Mock InventoryLayerRepository inventoryLayerRepository;
    @Mock InventoryAverageBalanceRepository inventoryAverageBalanceRepository;

    @InjectMocks
    InventoryValuationWebService service;

    private void configureItemAndTenant(
            InventoryValuationMethod method
    ) {
        Item item = mock(Item.class);

        when(item.isTrackStock()).thenReturn(true);

        if (method == InventoryValuationMethod.FIFO) {
            when(item.getId()).thenReturn(ITEM_ID);
            when(item.getCode()).thenReturn("TEST-ART-001");
        }

        when(itemRepository.findByTenantIdAndId(TENANT_ID, ITEM_ID))
                .thenReturn(Optional.of(item));

        Tenant tenant = mock(Tenant.class);

        when(tenant.getInventoryValuationMethod())
                .thenReturn(method);

        when(tenantRepository.findById(TENANT_ID))
                .thenReturn(Optional.of(tenant));
    }

    @Test
    void shouldRejectMissingCompanyBeforeRepositoryAccess() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new IllegalStateException(
                        "Nessuna società attiva"
                ));

        assertThrows(
                IllegalStateException.class,
                () -> service.getItemValuation(TENANT_ID, ITEM_ID)
        );

        verifyNoInteractions(
                itemRepository,
                tenantRepository,
                inventoryLayerRepository,
                inventoryAverageBalanceRepository
        );
    }

    @Test
    void shouldRejectAverageWithoutReadingLegacyBalances() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        configureItemAndTenant(
                InventoryValuationMethod.AVERAGE
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getItemValuation(TENANT_ID, ITEM_ID)
        );

        assertTrue(
                exception.getMessage().contains(
                        "Gestiva V2 supporta esclusivamente FIFO"
                )
        );

        verifyNoInteractions(
                inventoryAverageBalanceRepository,
                inventoryLayerRepository
        );
    }

    @Test
    void shouldUseCompanyScopedLayersWithFifo() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        configureItemAndTenant(
                InventoryValuationMethod.FIFO
        );

        when(inventoryLayerRepository
                .findByTenantIdAndCompanyIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
                        TENANT_ID, COMPANY_ID, ITEM_ID
                ))
                .thenReturn(List.of());

        var result = service.getItemValuation(
                TENANT_ID, ITEM_ID
        );

        assertNotNull(result);

        verify(inventoryLayerRepository)
                .findByTenantIdAndCompanyIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
                        TENANT_ID, COMPANY_ID, ITEM_ID
                );

        verifyNoInteractions(
                inventoryAverageBalanceRepository
        );
    }
}
