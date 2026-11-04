package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoodsReceiptPartialSnapshotServiceTest {

    private PurchaseOrderRepository orders;
    private PurchaseOrderLineRepository lines;
    private GoodsReceiptPartialHistoryReader history;
    private CompanyContext companies;
    private GoodsReceiptIsolationGuard guard;
    private GoodsReceiptPartialSnapshotService service;

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    @BeforeEach
    void setUp() {
        orders = mock(PurchaseOrderRepository.class);
        lines = mock(PurchaseOrderLineRepository.class);
        history = mock(GoodsReceiptPartialHistoryReader.class);
        companies = mock(CompanyContext.class);
        guard = mock(GoodsReceiptIsolationGuard.class);

        service = new GoodsReceiptPartialSnapshotService(
                orders, lines, history, companies, guard
        );
    }

    private PurchaseOrder confirmedOrder() {
        PurchaseOrder order = mock(PurchaseOrder.class);
        when(order.getStatus()).thenReturn("CONFIRMED");
        return order;
    }

    private PurchaseOrderLine orderLine() {
        PurchaseOrderLine line = mock(PurchaseOrderLine.class);
        when(line.getId()).thenReturn(101L);
        when(line.getQuantity()).thenReturn(bd("2.000"));
        when(line.getConversionFactor())
                .thenReturn(bd("10.000000"));
        return line;
    }

    @Test
    void readsHistoryAfterLockAndValidatesRemaining() {

        when(companies.requireCurrentCompanyId())
                .thenReturn(1L);

        var order = confirmedOrder();
        var line = orderLine();
        var orderLines = List.of(line);

        when(orders.findLockedByTenantIdAndCompanyIdAndId(
                2L, 1L, 31L
        )).thenReturn(Optional.of(order));

        when(lines.findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                2L, 31L
        )).thenReturn(orderLines);

        when(history.read(2L, 1L, 31L, orderLines))
                .thenReturn(Map.of(101L, bd("8.000")));

        var snapshot = service.prepare(
                2L, 31L, Map.of(101L, bd("12.000"))
        );

        assertSame(order, snapshot.purchaseOrder());
        assertEquals(2L, snapshot.tenantId());
        assertEquals(1L, snapshot.companyId());

        assertEquals(
                0,
                bd("8.000").compareTo(
                        snapshot.alreadyReceived().get(101L)
                )
        );

        assertEquals(
                0,
                bd("12.000").compareTo(
                        snapshot.validatedQuantities().get(101L)
                )
        );

        var calls = inOrder(
                guard, companies, orders, lines, history
        );

        calls.verify(guard).requireReadCommitted();
        calls.verify(companies).requireCurrentCompanyId();
        calls.verify(orders)
                .findLockedByTenantIdAndCompanyIdAndId(
                        2L, 1L, 31L
                );
        calls.verify(lines)
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        2L, 31L
                );
        calls.verify(history)
                .read(2L, 1L, 31L, orderLines);

        verifyNoMoreInteractions(history);

        assertThrows(
                UnsupportedOperationException.class,
                () -> snapshot.alreadyReceived()
                        .put(101L, bd("99.000"))
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> snapshot.validatedQuantities()
                        .put(101L, bd("99.000"))
        );

        System.out.println(
                "PARTIAL SNAPSHOT: lock prima dello storico"
        );
        System.out.println(
                "PARTIAL SNAPSHOT: storico unico e immutabile"
        );
    }

    @Test
    void rejectsWrongIsolationBeforeReadingOrder() {

        doThrow(new BusinessException(
                "READ COMMITTED obbligatorio."
        )).when(guard).requireReadCommitted();

        assertThrows(
                BusinessException.class,
                () -> service.prepare(
                        2L, 31L, Map.of(101L, bd("8.000"))
                )
        );

        verifyNoInteractions(
                companies, orders, lines, history
        );

        System.out.println(
                "PARTIAL SNAPSHOT: isolamento incompatibile rifiutato"
        );
    }

    @Test
    void rejectsUnconfirmedOrderBeforeHistory() {

        when(companies.requireCurrentCompanyId())
                .thenReturn(1L);

        PurchaseOrder order = mock(PurchaseOrder.class);
        when(order.getStatus()).thenReturn("DRAFT");

        when(orders.findLockedByTenantIdAndCompanyIdAndId(
                2L, 1L, 31L
        )).thenReturn(Optional.of(order));

        assertThrows(
                BusinessException.class,
                () -> service.prepare(
                        2L, 31L, Map.of(101L, bd("8.000"))
                )
        );

        verifyNoInteractions(lines, history);

        System.out.println(
                "PARTIAL SNAPSHOT: ordine non confermato rifiutato"
        );
    }

    @Test
    void rejectsOverReceiptUsingSingleSnapshot() {

        when(companies.requireCurrentCompanyId())
                .thenReturn(1L);

        var orderLines = List.of(orderLine());

        PurchaseOrder confirmed = confirmedOrder();

        when(orders.findLockedByTenantIdAndCompanyIdAndId(
                2L, 1L, 31L
        )).thenReturn(Optional.of(confirmed));

        when(lines.findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                2L, 31L
        )).thenReturn(orderLines);

        when(history.read(2L, 1L, 31L, orderLines))
                .thenReturn(Map.of(101L, bd("8.000")));

        assertThrows(
                BusinessException.class,
                () -> service.prepare(
                        2L, 31L, Map.of(101L, bd("13.000"))
                )
        );

        verify(history, times(1))
                .read(2L, 1L, 31L, orderLines);

        System.out.println(
                "PARTIAL SNAPSHOT: sovraricevimento rifiutato"
        );
    }

    @Test
    void rejectsMissingOrderBeforeHistory() {

        when(companies.requireCurrentCompanyId())
                .thenReturn(1L);

        when(orders.findLockedByTenantIdAndCompanyIdAndId(
                2L, 1L, 999L
        )).thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> service.prepare(
                        2L, 999L, Map.of(101L, bd("8.000"))
                )
        );

        verifyNoInteractions(lines, history);

        System.out.println(
                "PARTIAL SNAPSHOT: ordine inesistente rifiutato"
        );
    }
}
