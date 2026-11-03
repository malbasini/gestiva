package com.gestiva.warehouse.inventory.valuation.web;

import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.movement.web.InventoryLedgerWebService;
import com.gestiva.warehouse.inventory.valuation.entity.InventoryLayer;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryAverageBalanceRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
import com.gestiva.warehouse.item.entity.Item;
import com.gestiva.warehouse.item.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryConsistencyCheckWebServiceCompanyIsolationTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;
    private static final Long ITEM_ID = 77L;

    @Mock CompanyContext companyContext;
    @Mock InventoryLedgerWebService inventoryLedgerWebService;
    @Mock ItemRepository itemRepository;
    @Mock InventoryMovementRepository inventoryMovementRepository;
    @Mock InventoryLayerRepository inventoryLayerRepository;
    @Mock TenantRepository tenantRepository;
    @Mock InventoryAverageBalanceRepository inventoryAverageBalanceRepository;

    @InjectMocks
    InventoryConsistencyCheckWebService service;

    @Test
    void shouldRejectMissingActiveCompanyBeforeReadingRepositories() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new IllegalStateException(
                        "Nessuna società attiva selezionata"
                ));

        assertThrows(
                IllegalStateException.class,
                () -> service.findAll(TENANT_ID, null, false)
        );

        verifyNoInteractions(
                inventoryLedgerWebService,
                itemRepository,
                inventoryMovementRepository,
                inventoryLayerRepository,
                tenantRepository,
                inventoryAverageBalanceRepository
        );
    }

    private void configureFifoItem() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        var tenant = mock(com.gestiva.security.tenant.entity.Tenant.class);

        when(tenantRepository.findById(TENANT_ID))
                .thenReturn(Optional.of(tenant));

        Item item = mock(Item.class);
        when(item.isTrackStock()).thenReturn(true);
        when(item.getId()).thenReturn(ITEM_ID);
        when(item.getCode()).thenReturn("TEST-ART-001");

        when(itemRepository.findByTenantIdOrderByCodeAsc(TENANT_ID))
                .thenReturn(List.of(item));

        when(inventoryMovementRepository
                .findDistinctItemIdsByTenantIdAndCompanyId(
                        TENANT_ID, COMPANY_ID))
                .thenReturn(List.of(ITEM_ID));
    }

    @Test
    void shouldCompareOnlyCompanyLedgerAndFifoLayers() {
        configureFifoItem();

        when(inventoryLedgerWebService.calculateCurrentStock(TENANT_ID, ITEM_ID))
                .thenReturn(new BigDecimal("13.000"));

        InventoryLayer layer = mock(InventoryLayer.class);
        when(layer.getRemainingQty())
                .thenReturn(new BigDecimal("13.000"));

        when(inventoryLayerRepository
                .findByTenantIdAndCompanyIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
                        TENANT_ID, COMPANY_ID, ITEM_ID
                ))
                .thenReturn(List.of(layer));

        var rows = service.findAll(TENANT_ID, null, false);

        assertEquals(1, rows.size());
        assertTrue(rows.get(0).isAligned());

        verify(inventoryLedgerWebService)
                .calculateCurrentStock(TENANT_ID, ITEM_ID);

        verify(inventoryLayerRepository)
                .findByTenantIdAndCompanyIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
                        TENANT_ID, COMPANY_ID, ITEM_ID
                );

        verify(inventoryMovementRepository)
                .findDistinctItemIdsByTenantIdAndCompanyId(
                        TENANT_ID, COMPANY_ID);

        verifyNoInteractions(inventoryAverageBalanceRepository);
    }

    @Test
    void shouldDetectCompanyStockDifference() {
        configureFifoItem();

        when(inventoryLedgerWebService.calculateCurrentStock(TENANT_ID, ITEM_ID))
                .thenReturn(new BigDecimal("20.000"));

        InventoryLayer layer = mock(InventoryLayer.class);
        when(layer.getRemainingQty())
                .thenReturn(new BigDecimal("13.000"));

        when(inventoryLayerRepository
                .findByTenantIdAndCompanyIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
                        TENANT_ID, COMPANY_ID, ITEM_ID
                ))
                .thenReturn(List.of(layer));

        var rows = service.findAll(TENANT_ID, null, true);

        assertEquals(1, rows.size());
        assertFalse(rows.get(0).isAligned());

        verify(inventoryLayerRepository)
                .findByTenantIdAndCompanyIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
                        TENANT_ID, COMPANY_ID, ITEM_ID
                );
    }

    @Test
    void shouldHideSharedItemWithoutCompanyInventoryActivity() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        var tenant = mock(
                com.gestiva.security.tenant.entity.Tenant.class
        );

        when(tenantRepository.findById(TENANT_ID))
                .thenReturn(Optional.of(tenant));

        Item item = mock(Item.class);

        when(item.isTrackStock()).thenReturn(true);
        when(item.getId()).thenReturn(ITEM_ID);

        when(itemRepository.findByTenantIdOrderByCodeAsc(TENANT_ID))
                .thenReturn(List.of(item));

        when(inventoryMovementRepository
                .findDistinctItemIdsByTenantIdAndCompanyId(
                        TENANT_ID, COMPANY_ID))
                .thenReturn(List.of());

        when(inventoryLayerRepository
                .findDistinctItemIdsByTenantIdAndCompanyId(
                        TENANT_ID, COMPANY_ID))
                .thenReturn(List.of());

        var rows = service.findAll(TENANT_ID, null, false);

        assertTrue(
                rows.isEmpty(),
                "Articoli senza attività nella società attiva "
                + "non devono comparire nel controllo coerenza."
        );

        verifyNoInteractions(inventoryLedgerWebService);

        verify(inventoryMovementRepository)
                .findDistinctItemIdsByTenantIdAndCompanyId(
                        TENANT_ID, COMPANY_ID);

        verify(inventoryLayerRepository)
                .findDistinctItemIdsByTenantIdAndCompanyId(
                        TENANT_ID, COMPANY_ID);

        verify(inventoryLayerRepository, never())
                .findByTenantIdAndCompanyIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
                        anyLong(), anyLong(), anyLong());
    }


    @Test
    void shouldKeepCompanyItemWithZeroFinalStock() {
        // Articolo già movimentato nella società corrente.
        configureFifoItem();

        // La storia inventariale esiste anche se la giacenza è zero.
        when(inventoryLedgerWebService.calculateCurrentStock(
                TENANT_ID, ITEM_ID))
                .thenReturn(BigDecimal.ZERO);

        // Nessun layer FIFO aperto dopo il completo consumo.
        when(inventoryLayerRepository
                .findByTenantIdAndCompanyIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
                        TENANT_ID, COMPANY_ID, ITEM_ID))
                .thenReturn(List.of());

        var rows = service.findAll(TENANT_ID, null, false);

        assertEquals(1, rows.size());
        assertEquals(ITEM_ID, rows.get(0).getItemId());
        assertTrue(rows.get(0).isAligned());

        verify(inventoryMovementRepository)
                .findDistinctItemIdsByTenantIdAndCompanyId(
                        TENANT_ID, COMPANY_ID);

        verify(inventoryLayerRepository)
                .findByTenantIdAndCompanyIdAndItemIdAndClosedFalseOrderByLayerDateAscIdAsc(
                        TENANT_ID, COMPANY_ID, ITEM_ID);

        verifyNoInteractions(inventoryAverageBalanceRepository);
    }

}
