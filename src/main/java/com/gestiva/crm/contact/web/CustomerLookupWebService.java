package com.gestiva.crm.contact.web;

import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.crm.contact.repository.CustomerSpecifications;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CustomerLookupWebService {

    private final CustomerRepository customerRepository;

    public CustomerLookupWebService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<CustomerOptionView> findActiveOptions(Long tenantId) {
        var specification = CustomerSpecifications.hasTenantId(tenantId)
                .and(CustomerSpecifications.hasStatus("ACTIVE"));

        return customerRepository.findAll(specification, Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(customer -> {
                    String label = customer.getName();
                    if (customer.getVatNumber() != null && !customer.getVatNumber().isBlank()) {
                        label += " - " + customer.getVatNumber();
                    }
                    return new CustomerOptionView(customer.getId(), label);
                })
                .toList();
    }
}