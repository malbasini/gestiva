package com.gestiva.purchasing.order.web;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.entity.PurchaseOrder;
import com.gestiva.purchasing.order.repository.PurchaseOrderRepository;
import com.gestiva.purchasing.order.repository.PurchaseOrderLineRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptRepository;
import com.gestiva.purchasing.receipt.repository.GoodsReceiptLineRepository;
import com.gestiva.purchasing.supplier.repository.SupplierRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.item.repository.ItemUomRepository;

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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderPageCompanyIsolationTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;

    @Mock PurchaseOrderRepository purchaseOrderRepository;
    @Mock PurchaseOrderLineRepository purchaseOrderLineRepository;
    @Mock GoodsReceiptRepository goodsReceiptRepository;
    @Mock GoodsReceiptLineRepository goodsReceiptLineRepository;
    @Mock SupplierRepository supplierRepository;
    @Mock DocumentSequenceService documentSequenceService;
    @Mock ItemRepository itemRepository;
    @Mock ItemUomRepository itemUomRepository;
    @Mock CompanyContext companyContext;

    @InjectMocks
    PurchaseOrderWebService service;

    @Test
    void shouldRejectMissingCompanyBeforeRepositoryAccess() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new BusinessException(
                        "Selezionare una società attiva."
                ));

        assertThrows(
                BusinessException.class,
                () -> service.findPage(
                        TENANT_ID, 0, 20,
                        null, null, null, null
                )
        );

        verify(companyContext).requireCurrentCompanyId();

        verifyNoInteractions(
                purchaseOrderRepository,
                purchaseOrderLineRepository,
                goodsReceiptRepository,
                goodsReceiptLineRepository,
                supplierRepository,
                documentSequenceService,
                itemRepository,
                itemUomRepository
        );
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void shouldApplyTenantAndCompanyWithCorrectPagination() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(purchaseOrderRepository.findAll(
                org.mockito.ArgumentMatchers
                    .<Specification<PurchaseOrder>>any(),
                any(Pageable.class)
        )).thenAnswer(invocation -> {
            Pageable pageable = invocation.getArgument(1);
            return Page.empty(pageable);
        });

        var result = service.findPage(
                TENANT_ID,
                2,
                15,
                null,
                null,
                null,
                null
        );

        assertTrue(result.isEmpty());
        assertEquals(2, result.getNumber());
        assertEquals(15, result.getSize());

        ArgumentCaptor<Specification<PurchaseOrder>> specCaptor =
                ArgumentCaptor.forClass(Specification.class);

        ArgumentCaptor<Pageable> pageCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(purchaseOrderRepository).findAll(
                specCaptor.capture(),
                pageCaptor.capture()
        );

        Pageable pageable = pageCaptor.getValue();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(15, pageable.getPageSize());

        assertEquals(
                Sort.Direction.DESC,
                pageable.getSort().getOrderFor("orderDate")
                        .getDirection()
        );

        assertEquals(
                Sort.Direction.DESC,
                pageable.getSort().getOrderFor("id")
                        .getDirection()
        );

        Root<PurchaseOrder> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<Object> tenantPath = mock(Path.class);
        Path<Object> companyPath = mock(Path.class);

        Predicate tenantPredicate = mock(Predicate.class);
        Predicate companyPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        when(root.get("tenantId")).thenReturn(tenantPath);
        when(root.get("companyId")).thenReturn(companyPath);

        when(cb.equal(tenantPath, TENANT_ID))
                .thenReturn(tenantPredicate);

        when(cb.equal(companyPath, COMPANY_ID))
                .thenReturn(companyPredicate);

        when(cb.and(tenantPredicate, companyPredicate))
                .thenReturn(combinedPredicate);

        Predicate predicate = specCaptor.getValue()
                .toPredicate(root, query, cb);

        assertSame(combinedPredicate, predicate);

        verify(cb).equal(tenantPath, TENANT_ID);
        verify(cb).equal(companyPath, COMPANY_ID);
        verify(cb).and(
                tenantPredicate,
                companyPredicate
        );

        verifyNoInteractions(
                purchaseOrderLineRepository,
                goodsReceiptRepository,
                goodsReceiptLineRepository,
                supplierRepository,
                documentSequenceService,
                itemRepository,
                itemUomRepository
        );
    }
}
