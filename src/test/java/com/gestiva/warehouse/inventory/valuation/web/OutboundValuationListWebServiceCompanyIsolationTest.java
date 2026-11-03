package com.gestiva.warehouse.inventory.valuation.web;

import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.warehouse.inventory.movement.entity.InventoryMovement;
import com.gestiva.warehouse.inventory.movement.repository.InventoryMovementRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboundValuationListWebServiceCompanyIsolationTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;

    @Mock
    private CompanyContext companyContext;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private OutboundValuationListWebService service;

    @Test
    void findPageShouldRejectMissingActiveCompany() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new IllegalStateException("Nessuna società attiva"));

        assertThrows(IllegalStateException.class,
                () -> service.findPage(
                        TENANT_ID, 0, 10, null, null, null, null
                ));

        verifyNoInteractions(inventoryMovementRepository, itemRepository);
    }

    @Test
    void findAllShouldRejectMissingActiveCompany() {
        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new IllegalStateException("Nessuna società attiva"));

        assertThrows(IllegalStateException.class,
                () -> service.findAll(
                        TENANT_ID, null, null, null, null
                ));

        verifyNoInteractions(inventoryMovementRepository, itemRepository);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findPageShouldApplyTenantAndCompanyFilters() {
        when(companyContext.requireCurrentCompanyId()).thenReturn(COMPANY_ID);

        when(inventoryMovementRepository.findAll(
                any(Specification.class), any(Pageable.class)
        )).thenReturn(Page.empty());

        service.findPage(TENANT_ID, 0, 10, null, null, null, null);

        ArgumentCaptor<Specification> captor =
                ArgumentCaptor.forClass(Specification.class);

        verify(inventoryMovementRepository).findAll(
                captor.capture(), any(Pageable.class)
        );

        assertTenantAndCompanyFilters(captor.getValue());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAllShouldApplyTenantAndCompanyFilters() {
        when(companyContext.requireCurrentCompanyId()).thenReturn(COMPANY_ID);

        when(inventoryMovementRepository.findAll(
                any(Specification.class), any(Sort.class)
        )).thenReturn(List.of());

        when(itemRepository.findByTenantIdOrderByCodeAsc(TENANT_ID))
                .thenReturn(List.of());

        service.findAll(TENANT_ID, null, null, null, null);

        ArgumentCaptor<Specification> captor =
                ArgumentCaptor.forClass(Specification.class);

        verify(inventoryMovementRepository).findAll(
                captor.capture(), any(Sort.class)
        );

        assertTenantAndCompanyFilters(captor.getValue());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void assertTenantAndCompanyFilters(Specification specification) {
        Root<InventoryMovement> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<Object> tenantPath = mock(Path.class);
        Path<Object> companyPath = mock(Path.class);

        when(root.get("tenantId")).thenReturn(tenantPath);
        when(root.get("companyId")).thenReturn(companyPath);

        when(cb.equal(tenantPath, TENANT_ID))
                .thenReturn(mock(Predicate.class));

        when(cb.equal(companyPath, COMPANY_ID))
                .thenReturn(mock(Predicate.class));

        specification.toPredicate(root, query, cb);

        verify(cb).equal(tenantPath, TENANT_ID);
        verify(cb).equal(companyPath, COMPANY_ID);
    }
}
