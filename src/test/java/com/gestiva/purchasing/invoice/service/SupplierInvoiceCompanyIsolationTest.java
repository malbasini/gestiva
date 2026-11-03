package com.gestiva.purchasing.invoice.service;

import com.gestiva.accounting.due.service.PaymentDueService;
import com.gestiva.accounting.v2.journal.service.JournalAutoPostingService;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceLineRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.settings.sequence.service.DocumentSequenceService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierInvoiceCompanyIsolationTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;
    private static final Long RECEIPT_ID = 77L;

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

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(
                service, "environment", environment
        );

        when(environment.getActiveProfiles())
                .thenReturn(new String[]{"test"});

        when(environment.getProperty(
                "gestiva.purchasing.supplier-invoice.enabled",
                Boolean.class,
                false
        )).thenReturn(true);
    }

    private void verifyNoPersistenceOrAccounting() {
        verifyNoInteractions(
                supplierInvoiceRepository,
                supplierInvoiceLineRepository,
                purchaseOrderRepository,
                purchaseOrderLineRepository,
                goodsReceiptLineRepository,
                paymentDueService,
                journalAutoPostingService,
                documentSequenceService
        );
    }

    @Test
    void shouldRejectMissingCompanyBeforeRepositoryAccess() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new BusinessException(
                        "Selezionare una società attiva."
                ));

        assertThrows(
                BusinessException.class,
                () -> service.createFromGoodsReceipt(
                        TENANT_ID, RECEIPT_ID
                )
        );

        verify(companyContext).requireCurrentCompanyId();

        verifyNoInteractions(goodsReceiptRepository);

        verifyNoPersistenceOrAccounting();
    }

    @Test
    void shouldRejectReceiptOutsideSelectedCompany() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(goodsReceiptRepository
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, RECEIPT_ID
                ))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createFromGoodsReceipt(
                        TENANT_ID, RECEIPT_ID
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "Ricezione merci non trovata"
                )
        );

        verify(companyContext).requireCurrentCompanyId();

        verify(goodsReceiptRepository)
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, RECEIPT_ID
                );

        verifyNoMoreInteractions(goodsReceiptRepository);

        verifyNoPersistenceOrAccounting();
    }
}
