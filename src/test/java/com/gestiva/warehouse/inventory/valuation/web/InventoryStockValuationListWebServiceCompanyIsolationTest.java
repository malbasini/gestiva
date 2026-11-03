package com.gestiva.warehouse.inventory.valuation.web;

import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryAverageBalanceRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.item.entity.Item;
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
class InventoryStockValuationListWebServiceCompanyIsolationTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;

    @Mock
    private CompanyContext companyContext;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private InventoryLayerRepository inventoryLayerRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private InventoryAverageBalanceRepository inventoryAverageBalanceRepository;

    @InjectMocks
    private InventoryStockValuationListWebService service;

    @Test
    void shouldRejectMissingActiveCompanyBeforeReadingRepositories() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new IllegalStateException(
                        "Nessuna società attiva selezionata"
                ));

        assertThrows(
                IllegalStateException.class,
                () -> service.findAll(TENANT_ID, null)
        );

        verifyNoInteractions(
                itemRepository,
                inventoryMovementRepository,
                inventoryLayerRepository,
                tenantRepository,
                inventoryAverageBalanceRepository
        );
    }

    @Test
    void shouldUseCompanyScopedFifoQuery() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(itemRepository.findByTenantIdOrderByCodeAsc(TENANT_ID))
                .thenReturn(List.of());

        var tenant = mock(
                com.gestiva.security.tenant.entity.Tenant.class
        );

        when(tenantRepository.findById(TENANT_ID))
                .thenReturn(Optional.of(tenant));

        when(inventoryLayerRepository
                .findByTenantIdAndCompanyIdAndClosedFalseOrderByItemIdAscLayerDateAscIdAsc(
                        TENANT_ID, COMPANY_ID
                ))
                .thenReturn(List.of());

        service.findAll(TENANT_ID, null);

        verify(inventoryLayerRepository)
                .findByTenantIdAndCompanyIdAndClosedFalseOrderByItemIdAscLayerDateAscIdAsc(
                        TENANT_ID, COMPANY_ID
                );

        verify(inventoryLayerRepository, times(1))
                .findByTenantIdAndCompanyIdAndClosedFalseOrderByItemIdAscLayerDateAscIdAsc(
                        anyLong(), anyLong()
                );

        verifyNoInteractions(inventoryAverageBalanceRepository);
    }

    @Test
    void shouldHideSharedItemWithoutCompanyInventoryActivity() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        Item item = mock(Item.class);

        when(item.isTrackStock()).thenReturn(true);
        when(item.getId()).thenReturn(77L);

        when(itemRepository.findByTenantIdOrderByCodeAsc(TENANT_ID))
                .thenReturn(List.of(item));

        var tenant = mock(
                com.gestiva.security.tenant.entity.Tenant.class
        );

        when(tenantRepository.findById(TENANT_ID))
                .thenReturn(Optional.of(tenant));

        when(inventoryLayerRepository
                .findByTenantIdAndCompanyIdAndClosedFalseOrderByItemIdAscLayerDateAscIdAsc(
                        TENANT_ID, COMPANY_ID
                ))
                .thenReturn(List.of());

        var rows = service.findAll(TENANT_ID, null);

        assertTrue(rows.isEmpty());

        verify(inventoryMovementRepository)
                .findDistinctItemIdsByTenantIdAndCompanyId(
                        TENANT_ID, COMPANY_ID);

        verify(inventoryLayerRepository)
                .findDistinctItemIdsByTenantIdAndCompanyId(
                        TENANT_ID, COMPANY_ID);

        verify(inventoryLayerRepository)
                .findByTenantIdAndCompanyIdAndClosedFalseOrderByItemIdAscLayerDateAscIdAsc(
                        TENANT_ID, COMPANY_ID
                );

        verifyNoInteractions(inventoryAverageBalanceRepository);
    }

    @Test
    void shouldKeepCompanyItemWithZeroResidualStock() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        var tenant = mock(
                com.gestiva.security.tenant.entity.Tenant.class
        );

        when(tenantRepository.findById(TENANT_ID))
                .thenReturn(Optional.of(tenant));

        Item item = mock(Item.class);

        when(item.isTrackStock()).thenReturn(true);
        when(item.getId()).thenReturn(77L);
        when(item.getCode()).thenReturn("TEST-ART-001");

        when(itemRepository.findByTenantIdOrderByCodeAsc(TENANT_ID))
                .thenReturn(List.of(item));

        // L'articolo ha una storia inventariale nella società.
        when(inventoryMovementRepository
                .findDistinctItemIdsByTenantIdAndCompanyId(
                        TENANT_ID, COMPANY_ID))
                .thenReturn(List.of(77L));

        // Nessun layer FIFO aperto: giacenza residua zero.
        when(inventoryLayerRepository
                .findByTenantIdAndCompanyIdAndClosedFalseOrderByItemIdAscLayerDateAscIdAsc(
                        TENANT_ID, COMPANY_ID))
                .thenReturn(List.of());

        var rows = service.findAll(TENANT_ID, null);

        assertEquals(1, rows.size());
        assertEquals(77L, rows.get(0).getItemId());

        verify(inventoryMovementRepository)
                .findDistinctItemIdsByTenantIdAndCompanyId(
                        TENANT_ID, COMPANY_ID);

        verify(inventoryLayerRepository)
                .findDistinctItemIdsByTenantIdAndCompanyId(
                        TENANT_ID, COMPANY_ID);

        verify(inventoryLayerRepository)
                .findByTenantIdAndCompanyIdAndClosedFalseOrderByItemIdAscLayerDateAscIdAsc(
                        TENANT_ID, COMPANY_ID);

        verifyNoInteractions(inventoryAverageBalanceRepository);
    }

}
