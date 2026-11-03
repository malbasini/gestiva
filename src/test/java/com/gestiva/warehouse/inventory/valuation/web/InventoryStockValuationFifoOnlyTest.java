package com.gestiva.warehouse.inventory.valuation.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
import com.gestiva.security.tenant.entity.Tenant;
import com.gestiva.security.tenant.repository.TenantRepository;
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
class InventoryStockValuationFifoOnlyTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;

    @Mock CompanyContext companyContext;
    @Mock ItemRepository itemRepository;
    @Mock InventoryMovementRepository inventoryMovementRepository;
    @Mock InventoryLayerRepository inventoryLayerRepository;
    @Mock TenantRepository tenantRepository;
    @Mock InventoryAverageBalanceRepository inventoryAverageBalanceRepository;

    @InjectMocks
    InventoryStockValuationListWebService service;

    @Test
    void shouldRejectMissingCompanyBeforeReadingData() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new IllegalStateException(
                        "Nessuna società attiva"
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
    void shouldRejectAverageWithoutReadingLegacyBalances() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        Tenant tenant = mock(Tenant.class);

        when(tenant.getInventoryValuationMethod())
                .thenReturn(InventoryValuationMethod.AVERAGE);

        when(tenantRepository.findById(TENANT_ID))
                .thenReturn(Optional.of(tenant));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.findAll(TENANT_ID, null)
        );

        assertTrue(
                ex.getMessage().contains(
                        "Gestiva V2 supporta esclusivamente FIFO"
                )
        );

        verifyNoInteractions(
                inventoryAverageBalanceRepository,
                inventoryMovementRepository,
                inventoryLayerRepository
        );
    }

    @Test
    void shouldAllowFifoWithCompanyContext() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(itemRepository.findByTenantIdOrderByCodeAsc(TENANT_ID))
                .thenReturn(List.of());

        Tenant tenant = mock(Tenant.class);

        when(tenant.getInventoryValuationMethod())
                .thenReturn(InventoryValuationMethod.FIFO);

        when(tenantRepository.findById(TENANT_ID))
                .thenReturn(Optional.of(tenant));

        var rows = service.findAll(TENANT_ID, null);

        assertTrue(rows.isEmpty());

        verify(companyContext).requireCurrentCompanyId();

        verifyNoInteractions(inventoryAverageBalanceRepository);
    }
}
