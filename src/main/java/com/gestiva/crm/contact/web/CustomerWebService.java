package com.gestiva.crm.contact.web;

import com.gestiva.common.dto.PageResponse;
import com.gestiva.crm.contact.dto.CustomerSearchRequest;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.crm.contact.repository.CustomerSpecifications;
import com.gestiva.security.company.context.CompanyContext;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerWebService {

    private final CustomerRepository customerRepository;
    private final CompanyContext companyContext;

    public CustomerWebService(
            CustomerRepository customerRepository,
            CompanyContext companyContext
    ) {
        this.customerRepository = customerRepository;
        this.companyContext = companyContext;
    }

    private Long requireCompanyId() {

        Long companyId = companyContext.resolveCurrentCompanyId();

        if (companyId == null) {
            throw new IllegalStateException(
                    "Nessuna società attiva selezionata"
            );
        }

        return companyId;
    }

    public PageResponse<CustomerListItemView> search(
            Long tenantId,
            CustomerSearchRequest request,
            Pageable pageable
    ) {

        Long companyId = requireCompanyId();

        Specification<Customer> specification =
                CustomerSpecifications.hasTenantId(tenantId)
                        .and(byCompany(companyId))
                        .and(CustomerSpecifications.hasStatus(request.getStatus()))
                        .and(CustomerSpecifications.hasType(request.getType()))
                        .and(CustomerSpecifications.matchesSearch(request.getSearch()));

        Page<Customer> page =
                customerRepository.findAll(specification, pageable);

        var content = page.getContent()
                .stream()
                .map(this::toListItemViewCustomer)
                .toList();

        PageResponse<CustomerListItemView> response =
                new PageResponse<>();

        response.setContent(content);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());

        return response;
    }

    public Page<CustomerListItemView> findPage(
            Long tenantId,
            int page,
            int size,
            String q,
            String status
    ) {

        Long companyId = requireCompanyId();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.asc("email"))
        );

        Specification<Customer> spec =
                Specification.where(byTenant(tenantId))
                        .and(byCompany(companyId))
                        .and(bySearch(q))
                        .and(byStatus(status));

        return customerRepository
                .findAll(spec, pageable)
                .map(this::toListItemViewCustomer);
    }

    private Specification<Customer> byTenant(Long tenantId) {

        return (root, query, cb) ->
                cb.equal(root.get("tenantId"), tenantId);
    }

    private Specification<Customer> byCompany(Long companyId) {

        return (root, query, cb) ->
                cb.equal(root.get("companyId"), companyId);
    }

    private Specification<Customer> bySearch(String q) {

        if (q == null || q.trim().isEmpty()) {
            return null;
        }

        String like = "%" + q.trim().toLowerCase() + "%";

        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("vatNumber")), like),
                cb.like(cb.lower(root.get("email")), like)
        );
    }

    private Specification<Customer> byStatus(String status) {

        if (status == null || status.isBlank()) {
            return null;
        }

        return (root, query, cb) ->
                cb.equal(
                        cb.upper(root.get("status")),
                        status.trim().toUpperCase(java.util.Locale.ROOT)
                );
    }

    private CustomerListItemView toListItemViewCustomer(
            Customer customer
    ) {

        CustomerListItemView view = new CustomerListItemView();

        view.setId(customer.getId());
        view.setName(customer.getName());
        view.setVatNumber(customer.getVatNumber());
        view.setEmail(customer.getEmail());
        view.setType(customer.getType());
        view.setStatus(customer.getStatus());

        return view;
    }
}