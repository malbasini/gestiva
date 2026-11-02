package com.gestiva.billing.invoice.web;

import com.gestiva.billing.invoice.dto.InvoiceSearchRequest;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.billing.invoice.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceWebServiceCompanyIsolationTest {

    private static final Long TENANT_ID = 999L;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CompanyContext companyContext;

    @InjectMocks
    private InvoiceWebService service;

    @Test
    void searchShouldRejectMissingActiveCompany() {

        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new IllegalStateException(
                        "Società attiva obbligatoria."
                ));

        assertThrows(
                IllegalStateException.class,
                () -> service.search(
                        TENANT_ID,
                        new InvoiceSearchRequest(),
                        PageRequest.of(0, 10)
                )
        );

        verifyNoInteractions(
                invoiceRepository,
                customerRepository
        );
    }

    @Test
    void findPageShouldRejectMissingActiveCompany() {

        when(companyContext.requireCurrentCompanyId())
                .thenThrow(new IllegalStateException(
                        "Società attiva obbligatoria."
                ));

        assertThrows(
                IllegalStateException.class,
                () -> service.findPage(
                        TENANT_ID,
                        0,
                        10,
                        null,
                        null,
                        null,
                        null
                )
        );

        verifyNoInteractions(
                invoiceRepository,
                customerRepository
        );
    }


    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findPageShouldApplyTenantAndCompanyFilters() {

        Long companyId = 123L;

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(companyId);

        when(invoiceRepository.findAll(
                any(Specification.class),
                any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(Page.empty());

        service.findPage(
                TENANT_ID, 0, 10,
                null, null, null, null
        );

        ArgumentCaptor<Specification> captor =
                ArgumentCaptor.forClass(Specification.class);

        verify(invoiceRepository).findAll(
                captor.capture(),
                any(org.springframework.data.domain.Pageable.class)
        );

        Root<Invoice> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<Object> tenantPath = mock(Path.class);
        Path<Object> companyPath = mock(Path.class);

        Predicate tenantPredicate = mock(Predicate.class);
        Predicate companyPredicate = mock(Predicate.class);

        when(root.get("tenantId")).thenReturn(tenantPath);
        when(root.get("companyId")).thenReturn(companyPath);

        when(cb.equal(tenantPath, TENANT_ID))
                .thenReturn(tenantPredicate);
        when(cb.equal(companyPath, companyId))
                .thenReturn(companyPredicate);

        when(cb.and(any(Predicate.class), any(Predicate.class)))
                .thenReturn(mock(Predicate.class));

        captor.getValue().toPredicate(root, query, cb);

        verify(cb).equal(tenantPath, TENANT_ID);
        verify(cb).equal(companyPath, companyId);
        verify(cb, atLeastOnce()).and(
                any(Predicate.class),
                any(Predicate.class)
        );
    }



    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void searchShouldApplyTenantAndCompanyFilters() {

        Long companyId = 123L;

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(companyId);

        when(invoiceRepository.findAll(
                any(Specification.class),
                any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(Page.empty());

        InvoiceSearchRequest request = new InvoiceSearchRequest();
        request.setSearch("INV");

        service.search(
                TENANT_ID,
                request,
                PageRequest.of(0, 10)
        );

        ArgumentCaptor<Specification> captor =
                ArgumentCaptor.forClass(Specification.class);

        verify(invoiceRepository).findAll(
                captor.capture(),
                any(org.springframework.data.domain.Pageable.class)
        );

        Root<Invoice> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<Object> tenantPath = mock(Path.class);
        Path<Object> companyPath = mock(Path.class);

        Predicate tenantPredicate = mock(Predicate.class);
        Predicate companyPredicate = mock(Predicate.class);

        when(root.get("tenantId")).thenReturn(tenantPath);
        when(root.get("companyId")).thenReturn(companyPath);

        when(cb.equal(tenantPath, TENANT_ID))
                .thenReturn(tenantPredicate);
        when(cb.equal(companyPath, companyId))
                .thenReturn(companyPredicate);

        when(cb.and(any(Predicate.class), any(Predicate.class)))
                .thenReturn(mock(Predicate.class));

        captor.getValue().toPredicate(root, query, cb);

        verify(cb).equal(tenantPath, TENANT_ID);
        verify(cb).equal(companyPath, companyId);
    }

}
