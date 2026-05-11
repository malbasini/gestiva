package com.gestiva.crm.contact.web;

import com.gestiva.common.dto.PageResponse;
import com.gestiva.crm.contact.dto.CustomerSearchRequest;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.crm.contact.repository.CustomerSpecifications;
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

    public CustomerWebService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public PageResponse<CustomerListItemView> search(Long tenantId,
                                                     CustomerSearchRequest request,
                                                     Pageable pageable) {

        var specification = CustomerSpecifications.hasTenantId(tenantId)
                .and(CustomerSpecifications.hasStatus(request.getStatus()))
                .and(CustomerSpecifications.hasType(request.getType()))
                .and(CustomerSpecifications.matchesSearch(request.getSearch()));

        var page = customerRepository.findAll(specification, pageable);

        var content = page.getContent().stream().map(customer -> {
            CustomerListItemView item = new CustomerListItemView();
            item.setId(customer.getId());
            item.setName(customer.getName());
            item.setEmail(customer.getEmail());
            item.setVatNumber(customer.getVatNumber());
            item.setType(customer.getType());
            item.setStatus(customer.getStatus());
            return item;
        }).toList();

        PageResponse<CustomerListItemView> response = new PageResponse<>();
        response.setContent(content);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());

        return response;
    }

    public Page<CustomerListItemView> findPage(Long tenantId, int page, int size, String q, String status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("email")));
        Specification<Customer> spec = Specification.where(byTenant(tenantId))
                .and(bySearch(q))
                .and(byStatus(status));
        return customerRepository.findAll(spec, pageable).map(this::toListItemViewCustomer);
    }

    private Specification<Customer> byTenant(Long tenantId) {

        return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);

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
        return (root, query, cb) -> {
            if ("ACTIVE".equalsIgnoreCase(status)) {
                return cb.isTrue(root.get("active"));
            }
            if ("INACTIVE".equalsIgnoreCase(status)) {
                return cb.isFalse(root.get("active"));
            }
            return null;
        };
    }
    private CustomerListItemView toListItemViewCustomer(Customer customer) {

        CustomerListItemView v = new CustomerListItemView();
        v.setId(customer.getId());
        v.setName(customer.getName());
        v.setVatNumber(customer.getVatNumber());
        v.setEmail(customer.getEmail());
        v.setType(customer.getType());
        v.setStatus(customer.getStatus());
        return v;
    }

}
