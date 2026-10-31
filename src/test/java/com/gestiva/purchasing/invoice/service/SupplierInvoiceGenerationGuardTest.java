package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.accounting.due.service.PaymentDueService;
import com.gestiva.accounting.v2.journal.service.JournalAutoPostingService;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierInvoiceGenerationGuardTest {

    private static final Long TENANT_ID = 2L;
    private static final Long RECEIPT_ID = 1L;

    @Mock
    private SupplierInvoiceRepository supplierInvoiceRepository;

    @Mock
    private SupplierInvoiceLineRepository supplierInvoiceLineRepository;

    @Mock
    private GoodsReceiptRepository goodsReceiptRepository;

    @Mock
    private GoodsReceiptLineRepository goodsReceiptLineRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private PurchaseOrderLineRepository purchaseOrderLineRepository;

    @Mock
    private PaymentDueService paymentDueService;

    @Mock
    private JournalAutoPostingService journalAutoPostingService;

    @Mock
    private DocumentSequenceService documentSequenceService;

    @Mock
    private CompanyContext companyContext;

    @Mock
    private Environment environment;

    @InjectMocks
    private SupplierInvoiceService service;

    @BeforeEach
    void configureEnvironment() {
        ReflectionTestUtils.setField(
                service,
                "environment",
                environment
        );
    }

    private void assertGenerationBlocked() {
        assertThrows(
                BusinessException.class,
                () -> service.createFromGoodsReceipt(
                        TENANT_ID,
                        RECEIPT_ID
                )
        );

        verifyNoInteractions(
                companyContext,
                goodsReceiptRepository,
                goodsReceiptLineRepository,
                purchaseOrderRepository,
                purchaseOrderLineRepository,
                supplierInvoiceRepository,
                supplierInvoiceLineRepository,
                documentSequenceService,
                journalAutoPostingService,
                paymentDueService
        );
    }

    @Test
    void shouldRejectWhenPropertyDisabledInTestProfile() {
        when(environment.getActiveProfiles())
                .thenReturn(new String[]{"test"});

        when(environment.getProperty(
                "gestiva.purchasing.supplier-invoice.enabled",
                Boolean.class,
                false
        )).thenReturn(false);

        assertGenerationBlocked();
    }

    @Test
    void shouldRejectWhenProfileIsNotTest() {
        when(environment.getActiveProfiles())
                .thenReturn(new String[]{"dev"});

        when(environment.getProperty(
                "gestiva.purchasing.supplier-invoice.enabled",
                Boolean.class,
                false
        )).thenReturn(true);

        assertGenerationBlocked();
    }

    @Test
    void shouldRejectWhenNoActiveProfile() {
        when(environment.getActiveProfiles())
                .thenReturn(new String[0]);

        when(environment.getProperty(
                "gestiva.purchasing.supplier-invoice.enabled",
                Boolean.class,
                false
        )).thenReturn(true);

        assertGenerationBlocked();
    }

    @Test
    void shouldRejectWhenEnvironmentIsMissing() {
        ReflectionTestUtils.setField(
                service,
                "environment",
                null
        );

        assertGenerationBlocked();

        verifyNoInteractions(environment);
    }
}
