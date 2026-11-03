package com.gestiva.warehouse.inventory.valuation.web;

import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.tenant.repository.TenantRepository;
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
class InventoryValuationWebServiceCompanyIsolationTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;
    private static final Long ITEM_ID = 77L;

    @Mock
    private CompanyContext companyContext;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private InventoryLayerRepository inventoryLayerRepository;

    @Mock
    private InventoryAverageBalanceRepository inventoryAverageBalanceRepository;

    @InjectMocks
    private InventoryValuationWebService service;

    @Test
    void shouldRejectMissingCompanyBeforeReadingRepositories() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new IllegalStateException(
                        "Nessuna società attiva selezionata"
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
    void shouldReadOnlyFifoLayersOfActiveCompanyAndItem() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        Item item = mock(Item.class);
        when(item.isTrackStock()).thenReturn(true);
        when(item.getId()).thenReturn(ITEM_ID);

        when(itemRepository.findByTenantIdAndId(TENANT_ID, ITEM_ID))
                .thenReturn(Optional.of(item));

        var tenant = mock(
                com.gestiva.security.tenant.entity.Tenant.class
        );

        when(tenantRepository.findById(TENANT_ID))
                .thenReturn(Optional.of(tenant));

        when(inventoryLayerRepository
                .findByTenantIdAndCompanyIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
                        TENANT_ID, COMPANY_ID, ITEM_ID
                ))
                .thenReturn(List.of());

        var view = service.getItemValuation(TENANT_ID, ITEM_ID);

        assertEquals(ITEM_ID, view.getItemId());

        verify(inventoryLayerRepository)
                .findByTenantIdAndCompanyIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
                        TENANT_ID, COMPANY_ID, ITEM_ID
                );

        verifyNoInteractions(inventoryAverageBalanceRepository);
    }
}
