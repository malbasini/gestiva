package com.gestiva.warehouse.item.web;

import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemMovementWebServiceCompanyIsolationTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;
    private static final Long ITEM_ID = 77L;

    @Mock
    private CompanyContext companyContext;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @InjectMocks
    private ItemMovementWebService service;

    @Test
    void shouldRejectMissingActiveCompanyBeforeQuery() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new IllegalStateException(
                        "Nessuna società attiva selezionata"
                ));

        assertThrows(
                IllegalStateException.class,
                () -> service.findRecentMovements(TENANT_ID, ITEM_ID)
        );

        verifyNoInteractions(inventoryMovementRepository);
    }

    @Test
    void shouldReadOnlyRecentMovementsOfActiveCompany() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(inventoryMovementRepository
                .findTop10ByTenantIdAndCompanyIdAndItemIdOrderByMovementDateDescIdDesc(
                        TENANT_ID, COMPANY_ID, ITEM_ID
                ))
                .thenReturn(List.of());

        var movements = service.findRecentMovements(TENANT_ID, ITEM_ID);

        assertTrue(movements.isEmpty());

        verify(inventoryMovementRepository)
                .findTop10ByTenantIdAndCompanyIdAndItemIdOrderByMovementDateDescIdDesc(
                        TENANT_ID, COMPANY_ID, ITEM_ID
                );

        verifyNoMoreInteractions(inventoryMovementRepository);
    }
}
