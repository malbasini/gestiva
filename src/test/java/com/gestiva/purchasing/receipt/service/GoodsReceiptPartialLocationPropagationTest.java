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
class GoodsReceiptPartialLocationPropagationTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 3L;
    private static final Long ORDER = 100L;
    private static final Long WAREHOUSE = 50L;
    private static final Long LOCATION = 101L;

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
        return Map.of(200L, new BigDecimal("1.000"));
    }

    private void prepareValidOrder() {
        PurchaseOrder order = mock(PurchaseOrder.class);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);

        when(orders.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, ORDER
        )).thenReturn(Optional.of(order));

        when(order.getSupplierId()).thenReturn(700L);

        when(coordinator.create(
                any(GoodsReceipt.class),
                eq(quantities())
        )).thenReturn(301L);
    }

    private GoodsReceipt capturedHeader() {
        ArgumentCaptor<GoodsReceipt> captor =
                ArgumentCaptor.forClass(GoodsReceipt.class);

        verify(coordinator).create(
                captor.capture(),
                eq(quantities())
        );

        return captor.getValue();
    }

    @Test
    void explicitLocationIsPreservedInReceiptHeader() {
        prepareValidOrder();

        Long result = service.create(
                TENANT, ORDER, WAREHOUSE, LOCATION, quantities()
        );

        assertEquals(301L, result);

        GoodsReceipt header = capturedHeader();

        assertEquals(TENANT, header.getTenantId());
        assertEquals(COMPANY, header.getCompanyId());
        assertEquals(ORDER, header.getPurchaseOrderId());
        assertEquals(WAREHOUSE, header.getWarehouseId());
        assertEquals(LOCATION, header.getLocationId());

        verify(gate).requireEnabled();
    }

    @Test
    void legacyOverloadPreservesNullLocation() {
        prepareValidOrder();

        Long result = service.create(
                TENANT, ORDER, WAREHOUSE, quantities()
        );

        assertEquals(301L, result);

        GoodsReceipt header = capturedHeader();

        assertEquals(WAREHOUSE, header.getWarehouseId());
        assertNull(header.getLocationId());

        verify(gate).requireEnabled();
    }

    @Test
    void invalidLocationIsRejectedBeforeRepositoryAccess() {
        assertThrows(
                BusinessException.class,
                () -> service.create(
                        TENANT, ORDER, WAREHOUSE, -1L, quantities()
                )
        );

        verify(gate).requireEnabled();

        verifyNoInteractions(
                companyContext, orders, coordinator
        );
    }
}
