package com.gestiva.crm.contact.web;

import com.gestiva.common.dto.PageResponse;
import com.gestiva.crm.contact.dto.CustomerSearchRequest;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.crm.contact.repository.CustomerSpecifications;
import org.springframework.data.domain.Pageable;
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
}
