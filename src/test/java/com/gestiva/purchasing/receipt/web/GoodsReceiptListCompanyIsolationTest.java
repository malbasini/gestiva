package com.gestiva.purchasing.receipt.web;

import com.gestiva.common.exception.BusinessException;
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

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoodsReceiptListCompanyIsolationTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;

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
    void shouldRejectMissingCompanyBeforeRepositoryAccess() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new BusinessException(
                        "Selezionare una società attiva."
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.findAll(TENANT_ID)
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

    @Test
    void shouldQueryReceiptsWithCompanyContext() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(goodsReceiptRepository.findAll(
                org.mockito.ArgumentMatchers
                        .<Specification<GoodsReceipt>>any(),
                any(Sort.class)
        )).thenReturn(List.of());

        var result = service.findAll(TENANT_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(companyContext).requireCurrentCompanyId();

        verify(goodsReceiptRepository).findAll(
                org.mockito.ArgumentMatchers
                        .<Specification<GoodsReceipt>>any(),
                any(Sort.class)
        );

        verifyNoInteractions(
                goodsReceiptLineRepository,
                itemRepository,
                supplierRepository,
                purchaseOrderRepository,
                supplierInvoiceRepository
        );
    }
}
