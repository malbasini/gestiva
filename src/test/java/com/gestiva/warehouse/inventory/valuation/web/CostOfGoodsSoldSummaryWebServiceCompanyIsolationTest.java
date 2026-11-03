package com.gestiva.warehouse.inventory.valuation.web;

import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.item.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CostOfGoodsSoldSummaryWebServiceCompanyIsolationTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;

    @Mock
    CompanyContext companyContext;

    @Mock
    InventoryMovementRepository inventoryMovementRepository;

    @Mock
    ItemRepository itemRepository;

    @InjectMocks
    CostOfGoodsSoldSummaryWebService service;

    @Test
    void shouldRejectMissingCompanyBeforeRepositoryAccess() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new IllegalStateException(
                        "Nessuna società attiva"
                ));

        assertThrows(
                IllegalStateException.class,
                () -> service.summarize(TENANT_ID, null, null, null)
        );

        verifyNoInteractions(
                inventoryMovementRepository,
                itemRepository
        );
    }

    @Test
    void shouldReadCogsOnlyForActiveCompany() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(inventoryMovementRepository
                .summarizeCostOfGoodsSoldByCompanyAndItem(
                        TENANT_ID, COMPANY_ID, null, null, null
                ))
                .thenReturn(List.of());

        when(itemRepository.findByTenantIdOrderByCodeAsc(TENANT_ID))
                .thenReturn(List.of());

        var rows = service.summarize(
                TENANT_ID, null, null, null
        );

        assertTrue(rows.isEmpty());

        verify(inventoryMovementRepository)
                .summarizeCostOfGoodsSoldByCompanyAndItem(
                        TENANT_ID, COMPANY_ID, null, null, null
                );

        verify(inventoryMovementRepository, never())
                .summarizeCostOfGoodsSoldByItem(
                        any(), any(), any(), any()
                );

        verifyNoMoreInteractions(inventoryMovementRepository);
    }

    @Test
    void shouldPreserveItemAndDateFilters() {
        Long itemId = 77L;
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(inventoryMovementRepository
                .summarizeCostOfGoodsSoldByCompanyAndItem(
                        TENANT_ID, COMPANY_ID, itemId, from, to
                ))
                .thenReturn(List.of());

        when(itemRepository.findByTenantIdOrderByCodeAsc(TENANT_ID))
                .thenReturn(List.of());

        var rows = service.summarize(
                TENANT_ID, itemId, from, to
        );

        assertTrue(rows.isEmpty());

        verify(inventoryMovementRepository)
                .summarizeCostOfGoodsSoldByCompanyAndItem(
                        TENANT_ID, COMPANY_ID, itemId, from, to
                );

        verifyNoMoreInteractions(inventoryMovementRepository);
    }
}
