package com.gestiva.crm.contact.service;

import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.crm.contact.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> findAll(Long tenantId) {
        return customerRepository.findByTenantIdOrderByNameAsc(tenantId);
    }

    public Customer create(Long tenantId, Customer c) {
        c.setTenantId(tenantId);
        return customerRepository.save(c);
    }
}