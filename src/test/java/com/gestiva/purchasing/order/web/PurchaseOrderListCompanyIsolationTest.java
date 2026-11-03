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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderListCompanyIsolationTest {

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
    void shouldRejectMissingCompanyBeforeAnyRepositoryAccess() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new BusinessException(
                        "Selezionare una società attiva."
                ));

        assertThrows(
                BusinessException.class,
                () -> service.findAll(TENANT_ID)
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
    void shouldBuildTenantAndCompanyPredicates() {
        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(purchaseOrderRepository.findAll(
                org.mockito.ArgumentMatchers
                        .<Specification<PurchaseOrder>>any(),
                any(Sort.class)
        )).thenReturn(List.of());

        assertTrue(service.findAll(TENANT_ID).isEmpty());

        ArgumentCaptor<Specification<PurchaseOrder>> captor =
                ArgumentCaptor.forClass(Specification.class);

        verify(purchaseOrderRepository).findAll(
                captor.capture(),
                any(Sort.class)
        );

        Specification<PurchaseOrder> specification =
                captor.getValue();

        assertNotNull(specification);

        Root<PurchaseOrder> root = mock(Root.class);
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
