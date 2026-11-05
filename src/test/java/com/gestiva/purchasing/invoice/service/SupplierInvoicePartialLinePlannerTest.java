package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialLinePlannerTest {

    private GoodsReceiptLineRepository repository;
    private SupplierInvoicePartialLinePlanner planner;

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long ORDER = 100L;
    private static final Long ORDER_LINE = 200L;

    @BeforeEach
    void setUp() {
        repository = mock(GoodsReceiptLineRepository.class);
        planner = new SupplierInvoicePartialLinePlanner(repository);
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private SupplierInvoicePartialBillabilityValidator.BillableLine
    calculate(Long receiptId, String received, String previous) {

        when(repository.sumReceivedBeforeReceiptByOrderLine(
                TENANT, COMPANY, ORDER, ORDER_LINE, receiptId
        )).thenReturn(previous == null ? null : bd(previous));

        return planner.plan(
                TENANT,
                COMPANY,
                ORDER,
                ORDER_LINE,
                receiptId,
                bd(received),
                bd("20.000"),
                bd("10.000000"),
                bd("144.00"),
                bd("31.68"),
                bd("175.68")
        );
    }

    private static void assertAmounts(
            SupplierInvoicePartialBillabilityValidator.BillableLine line,
            String quantity,
            String subtotal,
            String tax,
            String total) {

        assertEquals(0,
                line.commercialQuantity().compareTo(bd(quantity)));

        assertEquals(0,
                line.subtotal().compareTo(bd(subtotal)));

        assertEquals(0,
                line.tax().compareTo(bd(tax)));

        assertEquals(0,
                line.total().compareTo(bd(total)));
    }

    @Test
    void firstReceiptUsesZeroPreviousQuantity() {
        var result = calculate(301L, "8.000", null);

        assertAmounts(
                result, "0.800", "57.60", "12.67", "70.27"
        );

        verify(repository).sumReceivedBeforeReceiptByOrderLine(
                TENANT, COMPANY, ORDER, ORDER_LINE, 301L
        );
    }

    @Test
    void secondReceiptUsesPreviousEightPieces() {
        var result = calculate(302L, "7.000", "8.000");

        assertAmounts(
                result, "0.700", "50.40", "11.09", "61.49"
        );
    }

    @Test
    void finalReceiptUsesPreviousFifteenPieces() {
        var result = calculate(303L, "5.000", "15.000");

        assertAmounts(
                result, "0.500", "36.00", "7.92", "43.92"
        );
    }

    @Test
    void outOfOrderBillingPreservesReceiptAmounts() {
        // Fatturiamo prima GR3, poi GR1, infine GR2.
        // Lo storico resta riferito agli ID dei ricevimenti.

        var third = calculate(303L, "5.000", "15.000");
        var first = calculate(301L, "8.000", null);
        var second = calculate(302L, "7.000", "8.000");

        assertAmounts(
                third, "0.500", "36.00", "7.92", "43.92"
        );

        assertAmounts(
                first, "0.800", "57.60", "12.67", "70.27"
        );

        assertAmounts(
                second, "0.700", "50.40", "11.09", "61.49"
        );

        assertEquals(
                0,
                first.subtotal()
                        .add(second.subtotal())
                        .add(third.subtotal())
                        .compareTo(bd("144.00"))
        );

        assertEquals(
                0,
                first.tax()
                        .add(second.tax())
                        .add(third.tax())
                        .compareTo(bd("31.68"))
        );

        assertEquals(
                0,
                first.total()
                        .add(second.total())
                        .add(third.total())
                        .compareTo(bd("175.68"))
        );
    }

    @Test
    void invalidIdentifiersAreRejectedBeforeRepositoryAccess() {
        assertThrows(
                BusinessException.class,
                () -> planner.plan(
                        TENANT,
                        COMPANY,
                        ORDER,
                        ORDER_LINE,
                        null,
                        bd("8.000"),
                        bd("20.000"),
                        bd("10"),
                        bd("144.00"),
                        bd("31.68"),
                        bd("175.68")
                )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void negativeHistoryIsRejected() {
        assertThrows(
                BusinessException.class,
                () -> calculate(302L, "7.000", "-1.000")
        );
    }

    @Test
    void overReceivedQuantityIsRejected() {
        assertThrows(
                BusinessException.class,
                () -> calculate(303L, "6.000", "15.000")
        );
    }

    @Test
    void companyAndTenantArePassedToHistoryQuery() {
        calculate(302L, "7.000", "8.000");

        verify(repository).sumReceivedBeforeReceiptByOrderLine(
                2L, 1L, 100L, 200L, 302L
        );

        verifyNoMoreInteractions(repository);
    }
}
