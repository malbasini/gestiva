package com.gestiva.purchasing.receipt.web;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.supplier.repository.SupplierRepository;
import com.gestiva.warehouse.item.repository.ItemRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoodsReceiptCompanyIsolationTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;
    private static final Long RECEIPT_ID = 77L;
    private static final Long ORDER_ID = 88L;

    @Mock GoodsReceiptRepository goodsReceiptRepository;
    @Mock GoodsReceiptLineRepository goodsReceiptLineRepository;
    @Mock ItemRepository itemRepository;
    @Mock SupplierRepository supplierRepository;
    @Mock PurchaseOrderRepository purchaseOrderRepository;
    @Mock SupplierInvoiceRepository supplierInvoiceRepository;
    @Mock CompanyContext companyContext;

    @InjectMocks
    GoodsReceiptWebService service;

    @Test
    void shouldRejectReceiptOutsideCurrentCompanyBeforeReadingLines() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(goodsReceiptRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID, COMPANY_ID, RECEIPT_ID
        )).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.getDetail(TENANT_ID, RECEIPT_ID)
        );

        verify(goodsReceiptRepository)
                .findByTenantIdAndCompanyIdAndId(
                        TENANT_ID, COMPANY_ID, RECEIPT_ID
                );

        verifyNoInteractions(
                goodsReceiptLineRepository,
                itemRepository,
                supplierRepository,
                purchaseOrderRepository,
                supplierInvoiceRepository
        );
    }

    @Test
    void shouldReadOnlyCompanyScopedReceiptLines() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        GoodsReceipt receipt = new GoodsReceipt();

        ReflectionTestUtils.setField(
                receipt, "id", RECEIPT_ID
        );

        receipt.setTenantId(TENANT_ID);
        receipt.setCompanyId(COMPANY_ID);
        receipt.setPurchaseOrderId(ORDER_ID);
        receipt.setSupplierId(33L);
        receipt.setReceiptNumber("GR-ISOLATION-TEST");
        receipt.setReceiptDate(LocalDate.of(2026, 11, 3));

        when(goodsReceiptRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID, COMPANY_ID, RECEIPT_ID
        )).thenReturn(Optional.of(receipt));

        when(goodsReceiptLineRepository
                .findByTenantIdAndCompanyIdAndGoodsReceiptIdOrderByLineNoAsc(
                        TENANT_ID, COMPANY_ID, RECEIPT_ID
                ))
                .thenReturn(List.of());

        var detail = service.getDetail(
                TENANT_ID, RECEIPT_ID
        );

        assertNotNull(detail);
        assertEquals(RECEIPT_ID, detail.getId());
        assertTrue(detail.getLines().isEmpty());

        verify(goodsReceiptLineRepository)
                .findByTenantIdAndCompanyIdAndGoodsReceiptIdOrderByLineNoAsc(
                        TENANT_ID, COMPANY_ID, RECEIPT_ID
                );


        verifyNoInteractions(itemRepository);
    }

    @Test
    void shouldRejectMissingCompanyBeforeAnyRepositoryAccess() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(
                        new com.gestiva.common.exception.BusinessException(
                                "Selezionare una società attiva."
                        )
                );

        var exception = assertThrows(
                com.gestiva.common.exception.BusinessException.class,
                () -> service.getDetail(TENANT_ID, RECEIPT_ID)
        );

        assertTrue(
                exception.getMessage().contains(
                        "Selezionare una società attiva"
                )
        );

        verify(companyContext).requireCurrentCompanyId();

        verifyNoInteractions(
                goodsReceiptRepository,
                goodsReceiptLineRepository,
                itemRepository,
                supplierRepository,
                purchaseOrderRepository,
                supplierInvoiceRepository
        );
    }
}
