package com.gestiva.warehouse.inventory.valuation.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.tenant.entity.Tenant;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.warehouse.inventory.movement.web.InventoryLedgerWebService;
import com.gestiva.warehouse.inventory.valuation.model.InventoryValuationMethod;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryAverageBalanceRepository;
import com.gestiva.warehouse.inventory.valuation.repository.InventoryLayerRepository;
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
class InventoryConsistencyCheckFifoOnlyTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;

    @Mock CompanyContext companyContext;
    @Mock InventoryLedgerWebService inventoryLedgerWebService;
    @Mock ItemRepository itemRepository;
    @Mock InventoryMovementRepository inventoryMovementRepository;
    @Mock InventoryLayerRepository inventoryLayerRepository;
    @Mock TenantRepository tenantRepository;
    @Mock InventoryAverageBalanceRepository inventoryAverageBalanceRepository;

    @InjectMocks
    InventoryConsistencyCheckWebService service;

    private void configureValuationMethod(
            InventoryValuationMethod method
    ) {
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
                () -> service.findAll(TENANT_ID, null, false)
        );

        verifyNoInteractions(
                tenantRepository,
                itemRepository,
                inventoryLedgerWebService,
                inventoryMovementRepository,
                inventoryLayerRepository,
                inventoryAverageBalanceRepository
        );
    }

    @Test
    void shouldRejectAverageWithoutReadingLegacyBalances() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        configureValuationMethod(
                InventoryValuationMethod.AVERAGE
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.findAll(TENANT_ID, null, false)
        );

        assertTrue(
                exception.getMessage().contains(
                        "Gestiva V2 supporta esclusivamente FIFO"
                )
        );

        verifyNoInteractions(
                inventoryLedgerWebService,
                inventoryMovementRepository,
                inventoryLayerRepository,
                inventoryAverageBalanceRepository
        );
    }

    @Test
    void shouldAllowFifoWithoutReadingAverageBalances() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        configureValuationMethod(
                InventoryValuationMethod.FIFO
        );

        when(itemRepository.findByTenantIdOrderByCodeAsc(TENANT_ID))
                .thenReturn(List.of());

        var rows = service.findAll(TENANT_ID, null, false);

        assertTrue(rows.isEmpty());

        verify(companyContext).requireCurrentCompanyId();

        verify(inventoryMovementRepository)
                .findDistinctItemIdsByTenantIdAndCompanyId(
                        TENANT_ID, COMPANY_ID);

        verify(inventoryLayerRepository)
                .findDistinctItemIdsByTenantIdAndCompanyId(
                        TENANT_ID, COMPANY_ID);

        verifyNoInteractions(inventoryAverageBalanceRepository);
    }
}
