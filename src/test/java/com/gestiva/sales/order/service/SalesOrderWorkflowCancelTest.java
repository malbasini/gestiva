package com.gestiva.sales.order.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.sales.order.entity.SalesOrder;
import com.gestiva.sales.order.repository.SalesOrderRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesOrderWorkflowCancelTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;
    private static final Long ORDER_ID = 77L;

    @Mock CompanyContext companyContext;
    @Mock SalesOrderRepository salesOrderRepository;

    @InjectMocks SalesOrderWorkflowService service;

    private SalesOrder prepareOrder(String status) {
        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        SalesOrder order = new SalesOrder();
        order.setTenantId(TENANT_ID);
        order.setCompanyId(COMPANY_ID);
        order.setOrderNumber("ORD-TEST-001");
        order.setStatus(status);

        when(salesOrderRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID, COMPANY_ID, ORDER_ID))
                .thenReturn(Optional.of(order));

        return order;
    }

    @Test
    void shouldCancelConfirmedOrderWithoutDeletingIt() {
        SalesOrder order = prepareOrder("CONFIRMED");

        service.cancel(TENANT_ID, ORDER_ID);

        assertEquals("CANCELLED", order.getStatus());
        assertEquals("ORD-TEST-001", order.getOrderNumber());
        assertEquals(TENANT_ID, order.getTenantId());
        assertEquals(COMPANY_ID, order.getCompanyId());

        verify(salesOrderRepository).save(same(order));
        verify(salesOrderRepository, never()).delete(any(SalesOrder.class));
        verify(salesOrderRepository, never()).deleteById(anyLong());
        verify(salesOrderRepository, never()).deleteAll();
    }

    @Test
    void shouldRejectAlreadyCancelledOrder() {
        SalesOrder order = prepareOrder("CANCELLED");

        assertThrows(BusinessException.class,
                () -> service.cancel(TENANT_ID, ORDER_ID));

        assertEquals("CANCELLED", order.getStatus());
        verify(salesOrderRepository, never()).save(any());
    }

    @Test
    void shouldRejectFulfilledOrder() {
        SalesOrder order = prepareOrder("FULFILLED");

        assertThrows(BusinessException.class,
                () -> service.cancel(TENANT_ID, ORDER_ID));

        assertEquals("FULFILLED", order.getStatus());
        verify(salesOrderRepository, never()).save(any());
    }

    @Test
    void shouldRejectOrderFromAnotherCompany() {
        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(salesOrderRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID, COMPANY_ID, ORDER_ID))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.cancel(TENANT_ID, ORDER_ID));

        verify(salesOrderRepository, never()).save(any());
        verify(salesOrderRepository, never()).delete(any(SalesOrder.class));
    }

    @Test
    void shouldRejectCancellationWithoutActiveCompany() {
        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(null);

        assertThrows(BusinessException.class,
                () -> service.cancel(TENANT_ID, ORDER_ID));

        verifyNoInteractions(salesOrderRepository);
    }
}
