package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.entity.GoodsReceiptLine;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierInvoicePartialPreparationServiceTest {

    private static final Long TENANT = 2L;
    private static final Long COMPANY = 1L;
    private static final Long RECEIPT = 301L;
    private static final Long ORDER = 100L;
    private static final Long SUPPLIER = 40L;

    private SupplierInvoicePartialGenerationGuard guard;
    private SupplierInvoicePartialIsolationGuard isolationGuard;
    private CompanyContext companyContext;
    private GoodsReceiptRepository receipts;
    private GoodsReceiptLineRepository receiptLines;
    private PurchaseOrderRepository orders;
    private SupplierInvoiceRepository invoices;
    private SupplierInvoicePartialPreparationService service;

    @BeforeEach
    void setup() {
        guard = mock(SupplierInvoicePartialGenerationGuard.class);
        isolationGuard = mock(SupplierInvoicePartialIsolationGuard.class);
        companyContext = mock(CompanyContext.class);
        receipts = mock(GoodsReceiptRepository.class);
        receiptLines = mock(GoodsReceiptLineRepository.class);
        orders = mock(PurchaseOrderRepository.class);
        invoices = mock(SupplierInvoiceRepository.class);

        service = new SupplierInvoicePartialPreparationService(
                guard,
                isolationGuard,
                companyContext,
                receipts,
                receiptLines,
                orders,
                invoices
        );
    }

    private void companyAvailable() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY);
    }

    private GoodsReceipt availableReceipt() {
        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setTenantId(TENANT);
        receipt.setCompanyId(COMPANY);
        receipt.setPurchaseOrderId(ORDER);
        receipt.setSupplierId(SUPPLIER);

        when(receipts.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, RECEIPT
        )).thenReturn(Optional.of(receipt));

        return receipt;
    }

    private PurchaseOrder availableOrder() {
        PurchaseOrder order = new PurchaseOrder();
        order.setStatus("CONFIRMED");
        order.setSupplierId(SUPPLIER);

        when(orders.findLockedByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, ORDER
        )).thenReturn(Optional.of(order));

        return order;
    }

    private void verifyNoWrites() {
        verify(receipts, never()).save(any());
        verify(receiptLines, never()).save(any());
        verify(orders, never()).save(any());
        verify(invoices, never()).save(any());
    }

    @Test
    void disabledGateStopsBeforeAnyDataAccess() {
        doThrow(new BusinessException("Funzione disabilitata"))
                .when(guard).requireEnabled();

        assertThrows(
                BusinessException.class,
                () -> service.prepare(TENANT, RECEIPT)
        );

        verify(guard).requireEnabled();
        verifyNoInteractions(isolationGuard);

        verifyNoInteractions(
                companyContext,
                receipts,
                receiptLines,
                orders,
                invoices
        );
    }

    @Test
    void invalidIdentifiersStopAfterGate() {
        assertThrows(
                BusinessException.class,
                () -> service.prepare(null, RECEIPT)
        );

        assertThrows(
                BusinessException.class,
                () -> service.prepare(TENANT, 0L)
        );

        verify(guard, times(2)).requireEnabled();

        verifyNoInteractions(
                companyContext,
                receipts,
                receiptLines,
                orders,
                invoices
        );
    }

    @Test
    void missingCompanyStopsBeforeRepositoryAccess() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(
                        new BusinessException(
                                "Società attiva non disponibile"
                        )
                );

        assertThrows(
                BusinessException.class,
                () -> service.prepare(TENANT, RECEIPT)
        );

        InOrder order = inOrder(guard, isolationGuard, companyContext);
        order.verify(guard).requireEnabled();
        order.verify(isolationGuard).requireReadCommittedWriteTransaction();
        order.verify(companyContext).requireCurrentCompanyId();

        verifyNoInteractions(
                receipts, receiptLines, orders, invoices
        );
    }

    @Test
    void missingReceiptNeverAttemptsOrderLock() {
        companyAvailable();

        when(receipts.findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, RECEIPT
        )).thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> service.prepare(TENANT, RECEIPT)
        );

        verify(receipts).findByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, RECEIPT
        );

        verifyNoInteractions(
                orders, invoices, receiptLines
        );
        verifyNoWrites();
    }

    @Test
    void missingOrderStopsBeforeDuplicateCheck() {
        companyAvailable();
        availableReceipt();

        when(orders.findLockedByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, ORDER
        )).thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> service.prepare(TENANT, RECEIPT)
        );

        verify(orders).findLockedByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, ORDER
        );

        verifyNoInteractions(invoices, receiptLines);
        verifyNoWrites();
    }

    @Test
    void duplicateInvoiceIsCheckedAfterOrderLock() {
        companyAvailable();
        availableReceipt();
        availableOrder();

        when(invoices.existsByTenantIdAndCompanyIdAndGoodsReceiptId(
                TENANT, COMPANY, RECEIPT
        )).thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> service.prepare(TENANT, RECEIPT)
        );

        InOrder order = inOrder(
                guard, isolationGuard, companyContext, receipts, orders, invoices
        );

        order.verify(guard).requireEnabled();
        order.verify(isolationGuard).requireReadCommittedWriteTransaction();
        order.verify(companyContext).requireCurrentCompanyId();
        order.verify(receipts)
                .findByTenantIdAndCompanyIdAndId(
                        TENANT, COMPANY, RECEIPT
                );
        order.verify(orders)
                .findLockedByTenantIdAndCompanyIdAndId(
                        TENANT, COMPANY, ORDER
                );
        order.verify(invoices)
                .existsByTenantIdAndCompanyIdAndGoodsReceiptId(
                        TENANT, COMPANY, RECEIPT
                );

        verifyNoInteractions(receiptLines);
        verifyNoWrites();
    }

    @Test
    void differentSupplierIsRejectedBeforeInvoiceCheck() {
        companyAvailable();
        availableReceipt();

        PurchaseOrder order = new PurchaseOrder();
        order.setStatus("CONFIRMED");
        order.setSupplierId(999L);

        when(orders.findLockedByTenantIdAndCompanyIdAndId(
                TENANT, COMPANY, ORDER
        )).thenReturn(Optional.of(order));

        assertThrows(
                BusinessException.class,
                () -> service.prepare(TENANT, RECEIPT)
        );

        verifyNoInteractions(invoices, receiptLines);
        verifyNoWrites();
    }

    @Test
    void emptyReceiptLinesAreRejected() {
        companyAvailable();
        availableReceipt();
        availableOrder();

        when(receiptLines
                .findByTenantIdAndCompanyIdAndGoodsReceiptIdOrderByLineNoAsc(
                        TENANT, COMPANY, RECEIPT
                )).thenReturn(List.of());

        assertThrows(
                BusinessException.class,
                () -> service.prepare(TENANT, RECEIPT)
        );

        verifyNoWrites();
    }

    @Test
    void validPreparationReturnsScopedDataWithoutWrites() {
        companyAvailable();

        GoodsReceipt receipt = availableReceipt();
        PurchaseOrder order = availableOrder();

        GoodsReceiptLine line = new GoodsReceiptLine();

        when(receiptLines
                .findByTenantIdAndCompanyIdAndGoodsReceiptIdOrderByLineNoAsc(
                        TENANT, COMPANY, RECEIPT
                )).thenReturn(List.of(line));

        var result = service.prepare(TENANT, RECEIPT);

        assertEquals(TENANT, result.tenantId());
        assertEquals(COMPANY, result.companyId());
        assertSame(receipt, result.receipt());
        assertSame(order, result.purchaseOrder());
        assertEquals(1, result.receiptLines().size());
        assertSame(line, result.receiptLines().get(0));

        assertThrows(
                UnsupportedOperationException.class,
                () -> result.receiptLines().clear()
        );

        verifyNoWrites();
    }
}
