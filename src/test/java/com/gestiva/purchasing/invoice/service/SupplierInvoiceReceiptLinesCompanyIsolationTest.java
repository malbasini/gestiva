package com.gestiva.purchasing.invoice.service;

import com.gestiva.accounting.due.service.PaymentDueService;
import com.gestiva.accounting.v2.journal.service.JournalAutoPostingService;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceLineRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.settings.sequence.service.DocumentSequenceService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierInvoiceReceiptLinesCompanyIsolationTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;
    private static final Long RECEIPT_ID = 77L;
    private static final Long ORDER_ID = 88L;
    private static final Long SUPPLIER_ID = 33L;

    @Mock SupplierInvoiceRepository supplierInvoiceRepository;
    @Mock SupplierInvoiceLineRepository supplierInvoiceLineRepository;
    @Mock GoodsReceiptRepository goodsReceiptRepository;
    @Mock GoodsReceiptLineRepository goodsReceiptLineRepository;
    @Mock PurchaseOrderRepository purchaseOrderRepository;
    @Mock PurchaseOrderLineRepository purchaseOrderLineRepository;
    @Mock PaymentDueService paymentDueService;
    @Mock JournalAutoPostingService journalAutoPostingService;
    @Mock DocumentSequenceService documentSequenceService;
    @Mock CompanyContext companyContext;
    @Mock Environment environment;

    @InjectMocks
    SupplierInvoiceService service;

    @Test
    void shouldUseCompanyScopedReceiptLinesAndStopBeforePersistence() {
        ReflectionTestUtils.setField(
                service, "environment", environment
        );

        // Simulazione Mockito: nessun flag reale viene modificato.
        when(environment.getActiveProfiles())
                .thenReturn(new String[]{"test"});

        when(environment.getProperty(
                "gestiva.purchasing.supplier-invoice.enabled",
                Boolean.class,
                false
        )).thenReturn(true);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setTenantId(TENANT_ID);
        receipt.setCompanyId(COMPANY_ID);
        receipt.setPurchaseOrderId(ORDER_ID);
        receipt.setSupplierId(SUPPLIER_ID);

        when(goodsReceiptRepository
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, RECEIPT_ID
                ))
                .thenReturn(Optional.of(receipt));

        // Il valore di default di exists(...) è false.
        PurchaseOrder order = new PurchaseOrder();
        order.setStatus("CONFIRMED");
        order.setSupplierId(SUPPLIER_ID);

        when(purchaseOrderRepository
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, ORDER_ID
                ))
                .thenReturn(Optional.of(order));

        when(goodsReceiptLineRepository
                .findByTenantIdAndCompanyIdAndGoodsReceiptIdOrderByLineNoAsc(
                        TENANT_ID, COMPANY_ID, RECEIPT_ID
                ))
                .thenReturn(List.of());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createFromGoodsReceipt(
                        TENANT_ID, RECEIPT_ID
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "non contiene righe"
                )
        );

        verify(goodsReceiptRepository)
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, RECEIPT_ID
                );

        verify(purchaseOrderRepository)
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, ORDER_ID
                );

        verify(goodsReceiptLineRepository)
                .findByTenantIdAndCompanyIdAndGoodsReceiptIdOrderByLineNoAsc(
                        TENANT_ID, COMPANY_ID, RECEIPT_ID
                );


        verify(supplierInvoiceRepository, never())
                .save(any());

        verifyNoInteractions(
                supplierInvoiceLineRepository,
                purchaseOrderLineRepository,
                paymentDueService,
                journalAutoPostingService,
                documentSequenceService
        );

        verifyNoMoreInteractions(goodsReceiptLineRepository);
    }
}
