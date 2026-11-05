package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoodsReceiptPartialApplicationServiceTest {

    private static final Long TENANT_ID = 2L;
    private static final Long COMPANY_ID = 1L;
    private static final Long ORDER_ID = 100L;
    private static final Long WAREHOUSE_ID = 50L;

    @Mock
    GoodsReceiptPartialPersistenceGate gate;

    @Mock
    GoodsReceiptPartialCoordinator coordinator;

    @Mock
    PurchaseOrderRepository orders;

    @Mock
    CompanyContext companyContext;

    @InjectMocks
    GoodsReceiptPartialApplicationService service;

    private Map<Long, BigDecimal> quantities() {
        return Map.of(200L, new BigDecimal("8.000"));
    }

    @Test
    void disabledGateMustPreventAllDocumentAccess() {
        doThrow(new BusinessException(
                "Ricevimenti parziali non abilitati."
        )).when(gate).requireEnabled();

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID, ORDER_ID, WAREHOUSE_ID,
                        quantities()
                )
        );

        verify(gate).requireEnabled();
        verifyNoInteractions(
                companyContext, orders, coordinator
        );
    }

    @Test
    void invalidInputMustNotAccessRepositories() {
        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID, ORDER_ID, WAREHOUSE_ID,
                        Map.of()
                )
        );

        verify(gate).requireEnabled();
        verifyNoInteractions(
                companyContext, orders, coordinator
        );
    }

    @Test
    void missingCompanyMustPreventOrderLookup() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new BusinessException(
                        "Selezionare una società attiva."
                ));

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID, ORDER_ID, WAREHOUSE_ID,
                        quantities()
                )
        );

        verify(gate).requireEnabled();
        verify(companyContext).requireCurrentCompanyId();
        verifyNoInteractions(orders, coordinator);
    }

    @Test
    void foreignOrMissingOrderMustBeRejected() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(orders.findByTenantIdAndCompanyIdAndId(
                TENANT_ID, COMPANY_ID, ORDER_ID
        )).thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT_ID, ORDER_ID, WAREHOUSE_ID,
                        quantities()
                )
        );

        verify(orders).findByTenantIdAndCompanyIdAndId(
                TENANT_ID, COMPANY_ID, ORDER_ID
        );

        verifyNoInteractions(coordinator);
    }

    @Test
    void validRequestMustBuildServerControlledHeader() {
        PurchaseOrder order = mock(PurchaseOrder.class);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(orders.findByTenantIdAndCompanyIdAndId(
                TENANT_ID, COMPANY_ID, ORDER_ID
        )).thenReturn(Optional.of(order));

        when(order.getSupplierId()).thenReturn(700L);

        when(coordinator.create(
                any(GoodsReceipt.class),
                eq(quantities())
        )).thenReturn(301L);

        Long result = service.create(
                TENANT_ID, ORDER_ID, WAREHOUSE_ID,
                quantities()
        );

        assertEquals(301L, result);

        ArgumentCaptor<GoodsReceipt> headerCaptor =
                ArgumentCaptor.forClass(GoodsReceipt.class);

        verify(coordinator).create(
                headerCaptor.capture(),
                eq(quantities())
        );

        GoodsReceipt header = headerCaptor.getValue();

        assertNull(header.getId());
        assertEquals(TENANT_ID, header.getTenantId());
        assertEquals(COMPANY_ID, header.getCompanyId());
        assertEquals(ORDER_ID, header.getPurchaseOrderId());
        assertEquals(700L, header.getSupplierId());
        assertEquals(WAREHOUSE_ID, header.getWarehouseId());
        assertNull(header.getLocationId());
        assertNotNull(header.getReceiptDate());

        verify(gate).requireEnabled();

        verify(orders).findByTenantIdAndCompanyIdAndId(
                TENANT_ID, COMPANY_ID, ORDER_ID
        );
    }
}
