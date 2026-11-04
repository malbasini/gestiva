package com.gestiva.purchasing.receipt.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.entity.PurchaseOrderLine;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoodsReceiptPartialPreparationServiceTest {

    private PurchaseOrderRepository orders;
    private PurchaseOrderLineRepository lines;
    private GoodsReceiptService receipts;
    private CompanyContext companies;
    private GoodsReceiptIsolationGuard guard;
    private GoodsReceiptPartialPreparationService service;

    @BeforeEach
    void setup() {
        orders = mock(PurchaseOrderRepository.class);
        lines = mock(PurchaseOrderLineRepository.class);
        receipts = mock(GoodsReceiptService.class);
        companies = mock(CompanyContext.class);
        guard = mock(GoodsReceiptIsolationGuard.class);

        service = new GoodsReceiptPartialPreparationService(
                orders, lines, receipts, companies, guard
        );
    }

    @Test
    void publicMethodDeclaresReadCommittedTransaction()
            throws Exception {

        var method = GoodsReceiptPartialPreparationService.class
                .getMethod(
                        "prepare",
                        Long.class,
                        Long.class,
                        Map.class
                );

        Transactional tx =
                method.getAnnotation(Transactional.class);

        assertNotNull(tx);
        assertEquals(Isolation.READ_COMMITTED, tx.isolation());
        assertEquals(Propagation.REQUIRED, tx.propagation());
    }

    @Test
    void locksOrderBeforeValidatingQuantities() {

        var order = mock(PurchaseOrder.class);
        var line = mock(PurchaseOrderLine.class);

        Map<Long, BigDecimal> requested =
                Map.of(12L, new BigDecimal("8.000"));

        when(companies.requireCurrentCompanyId()).thenReturn(1L);
        when(orders.findLockedByTenantIdAndCompanyIdAndId(
                2L, 1L, 3L
        )).thenReturn(Optional.of(order));

        when(order.getStatus()).thenReturn("CONFIRMED");

        when(lines.findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                2L, 3L
        )).thenReturn(List.of(line));

        when(receipts.validatePartialQuantitiesForLockedOrder(
                2L, 1L, 3L, List.of(line), requested
        )).thenReturn(requested);

        assertEquals(
                requested,
                service.prepare(2L, 3L, requested)
        );

        var orderOfCalls = inOrder(guard, orders, lines, receipts);

        orderOfCalls.verify(guard).requireReadCommitted();

        orderOfCalls.verify(orders)
                .findLockedByTenantIdAndCompanyIdAndId(
                        2L, 1L, 3L
                );

        orderOfCalls.verify(lines)
                .findByTenantIdAndPurchaseOrderIdOrderByLineNoAsc(
                        2L, 3L
                );

        orderOfCalls.verify(receipts)
                .validatePartialQuantitiesForLockedOrder(
                        2L, 1L, 3L,
                        List.of(line),
                        requested
                );
    }

    @Test
    void rejectsMissingOrderBeforeReadingLines() {

        when(companies.requireCurrentCompanyId()).thenReturn(1L);

        when(orders.findLockedByTenantIdAndCompanyIdAndId(
                2L, 1L, 3L
        )).thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> service.prepare(
                        2L, 3L,
                        Map.of(12L, new BigDecimal("8.000"))
                )
        );

        verifyNoInteractions(lines, receipts);
    }

    @Test
    void rejectsUnconfirmedOrder() {

        var order = mock(PurchaseOrder.class);

        when(companies.requireCurrentCompanyId()).thenReturn(1L);

        when(orders.findLockedByTenantIdAndCompanyIdAndId(
                2L, 1L, 3L
        )).thenReturn(Optional.of(order));

        when(order.getStatus()).thenReturn("DRAFT");

        assertThrows(
                BusinessException.class,
                () -> service.prepare(
                        2L, 3L,
                        Map.of(12L, new BigDecimal("8.000"))
                )
        );

        verifyNoInteractions(lines, receipts);
    }

    @Test
    void rejectsMissingCompanyBeforeLockingOrder() {

        when(companies.requireCurrentCompanyId()).thenReturn(null);

        assertThrows(
                BusinessException.class,
                () -> service.prepare(
                        2L, 3L,
                        Map.of(12L, new BigDecimal("8.000"))
                )
        );

        verifyNoInteractions(orders, lines, receipts);
    }

    @Test
    void rejectsWrongIsolationBeforeAccessingOrder() {

        doThrow(new BusinessException(
                "Ricevimento parziale: isolamento non valido."
        )).when(guard).requireReadCommitted();

        assertThrows(
                BusinessException.class,
                () -> service.prepare(
                        2L,
                        3L,
                        Map.of(12L, new BigDecimal("8.000"))
                )
        );

        verify(guard, times(1)).requireReadCommitted();
        verifyNoInteractions(companies, orders, lines, receipts);
    }
}
