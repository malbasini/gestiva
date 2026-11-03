package com.gestiva.purchasing.receipt.web;

import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.receipt.entity.GoodsReceipt;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.supplier.repository.SupplierRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.item.repository.ItemRepository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class GoodsReceiptSpecificationCompanyPredicateTest {

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
    @SuppressWarnings({"unchecked", "rawtypes"})
    void shouldCombineTenantAndCompanyPredicatesWithAnd() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(goodsReceiptRepository.findAll(
                org.mockito.ArgumentMatchers
                        .<Specification<GoodsReceipt>>any(),
                any(Sort.class)
        )).thenReturn(List.of());

        assertTrue(service.findAll(TENANT_ID).isEmpty());

        ArgumentCaptor<Specification<GoodsReceipt>> specCaptor =
                ArgumentCaptor.forClass(Specification.class);

        verify(goodsReceiptRepository).findAll(
                specCaptor.capture(),
                any(Sort.class)
        );

        Specification<GoodsReceipt> specification =
                specCaptor.getValue();

        assertNotNull(specification);

        Root<GoodsReceipt> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<Object> tenantPath = mock(Path.class);
        Path<Object> companyPath = mock(Path.class);

        Predicate tenantPredicate = mock(Predicate.class);
        Predicate companyPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        when(root.get("tenantId"))
                .thenReturn(tenantPath);

        when(root.get("companyId"))
                .thenReturn(companyPath);

        when(cb.equal(tenantPath, TENANT_ID))
                .thenReturn(tenantPredicate);

        when(cb.equal(companyPath, COMPANY_ID))
                .thenReturn(companyPredicate);

        when(cb.and(tenantPredicate, companyPredicate))
                .thenReturn(combinedPredicate);

        Predicate result =
                specification.toPredicate(root, query, cb);

        assertSame(combinedPredicate, result);

        verify(root).get("tenantId");
        verify(root).get("companyId");

        verify(cb).equal(tenantPath, TENANT_ID);
        verify(cb).equal(companyPath, COMPANY_ID);

        verify(cb).and(
                tenantPredicate,
                companyPredicate
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
