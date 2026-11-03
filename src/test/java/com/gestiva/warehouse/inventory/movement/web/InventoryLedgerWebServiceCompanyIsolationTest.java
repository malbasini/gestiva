package com.gestiva.warehouse.inventory.movement.web;

import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.inventory.movement.entity.InventoryMovement;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
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
class InventoryLedgerWebServiceCompanyIsolationTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;
    private static final Long ITEM_ID = 77L;

    @Mock
    private CompanyContext companyContext;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @InjectMocks
    private InventoryLedgerWebService service;

    @Test
    void getLedgerShouldRejectMissingActiveCompany() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new IllegalStateException(
                        "Nessuna società attiva selezionata"
                ));

        assertThrows(
                IllegalStateException.class,
                () -> service.getLedger(TENANT_ID, ITEM_ID)
        );

        verifyNoInteractions(itemRepository, inventoryMovementRepository);
    }

    @Test
    void calculateCurrentStockShouldRejectMissingActiveCompany() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new IllegalStateException(
                        "Nessuna società attiva selezionata"
                ));

        assertThrows(
                IllegalStateException.class,
                () -> service.calculateCurrentStock(TENANT_ID, ITEM_ID)
        );

        verifyNoInteractions(itemRepository, inventoryMovementRepository);
    }

    @Test
    void getLedgerShouldReadOnlyMovementsOfActiveCompany() {
        when(companyContext.requireCurrentCompanyId()).thenReturn(COMPANY_ID);

        Item item = mock(Item.class);
        when(item.isTrackStock()).thenReturn(true);
        when(item.getId()).thenReturn(ITEM_ID);
        when(item.getItemType()).thenReturn(
                com.gestiva.warehouse.item.entity.ItemType.PRODUCT
        );

        when(itemRepository.findByTenantIdAndId(TENANT_ID, ITEM_ID))
                .thenReturn(Optional.of(item));

        when(inventoryMovementRepository
                .findByTenantIdAndCompanyIdAndItemIdOrderByMovementDateAscIdAsc(
                        TENANT_ID, COMPANY_ID, ITEM_ID
                ))
                .thenReturn(List.of());

        var view = service.getLedger(TENANT_ID, ITEM_ID);

        assertEquals(ITEM_ID, view.getItemId());
        assertTrue(view.getLines().isEmpty());

        verify(inventoryMovementRepository)
                .findByTenantIdAndCompanyIdAndItemIdOrderByMovementDateAscIdAsc(
                        TENANT_ID, COMPANY_ID, ITEM_ID
                );
    }

    @Test
    void calculateCurrentStockShouldUseOnlyCompanyMovements() {
        when(companyContext.requireCurrentCompanyId()).thenReturn(COMPANY_ID);

        InventoryMovement inbound = mock(InventoryMovement.class);
        when(inbound.getMovementType()).thenReturn("IN");
        when(inbound.getQuantity()).thenReturn(new BigDecimal("20.000"));

        InventoryMovement outbound = mock(InventoryMovement.class);
        when(outbound.getMovementType()).thenReturn("OUT");
        when(outbound.getQuantity()).thenReturn(new BigDecimal("7.000"));

        when(inventoryMovementRepository
                .findByTenantIdAndCompanyIdAndItemIdOrderByMovementDateAscIdAsc(
                        TENANT_ID, COMPANY_ID, ITEM_ID
                ))
                .thenReturn(List.of(inbound, outbound));

        BigDecimal stock = service.calculateCurrentStock(TENANT_ID, ITEM_ID);

        assertEquals(0, new BigDecimal("13.000").compareTo(stock));

        verify(inventoryMovementRepository)
                .findByTenantIdAndCompanyIdAndItemIdOrderByMovementDateAscIdAsc(
                        TENANT_ID, COMPANY_ID, ITEM_ID
                );
    }
}
