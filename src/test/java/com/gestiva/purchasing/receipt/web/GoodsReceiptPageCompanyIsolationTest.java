package com.gestiva.purchasing.receipt.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.supplier.repository.SupplierRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.item.repository.ItemRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoodsReceiptPageCompanyIsolationTest {

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
    void shouldRejectMissingCompanyBeforeAnyRepositoryAccess() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new BusinessException(
                        "Selezionare una società attiva."
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.findPage(
                        TENANT_ID,
                        0,
                        20,
                        null,
                        null,
                        null
                )
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
    void shouldExecutePaginatedQueryForSelectedCompany() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(goodsReceiptRepository.findAll(
                org.mockito.ArgumentMatchers
                        .<Specification<GoodsReceipt>>any(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);

        Page<GoodsReceiptListItemView> result =
                service.findPage(
                        TENANT_ID,
                        1,
                        15,
                        "GR-TEST",
                        from,
                        to
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Specification<GoodsReceipt>> specificationCaptor =
                ArgumentCaptor.forClass(Specification.class);

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(goodsReceiptRepository).findAll(
                specificationCaptor.capture(),
                pageableCaptor.capture()
        );

        assertNotNull(specificationCaptor.getValue());

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(1, pageable.getPageNumber());
        assertEquals(15, pageable.getPageSize());

        assertEquals(
                org.springframework.data.domain.Sort.Direction.DESC,
                pageable.getSort().getOrderFor(
                        "receiptDate"
                ).getDirection()
        );

        assertEquals(
                org.springframework.data.domain.Sort.Direction.DESC,
                pageable.getSort().getOrderFor(
                        "id"
                ).getDirection()
        );

        verify(companyContext).requireCurrentCompanyId();

        verifyNoInteractions(
                goodsReceiptLineRepository,
                itemRepository,
                supplierRepository,
                purchaseOrderRepository,
                supplierInvoiceRepository
        );
    }
}
