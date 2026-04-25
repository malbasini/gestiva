package com.gestiva.crm.contact.service;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.crm.contact.dto.CustomerCreateRequest;
import com.gestiva.crm.contact.dto.CustomerResponse;
import com.gestiva.crm.contact.dto.CustomerUpdateRequest;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.crm.contact.mapper.CustomerMapper;
import com.gestiva.crm.contact.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepository customerRepository,
                           CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll(Long tenantId) {
        return customerRepository.findByTenantIdOrderByNameAsc(tenantId)
                .stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long tenantId, Long id) {
        Customer customer = customerRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Cliente non trovato"));
        return customerMapper.toResponse(customer);
    }

    public CustomerResponse create(Long tenantId, CustomerCreateRequest request) {
        Customer customer = customerMapper.toEntity(request);
        customer.setTenantId(tenantId);
        Customer saved = customerRepository.save(customer);
        return customerMapper.toResponse(saved);
    }

    public CustomerResponse update(Long tenantId, Long id, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Cliente non trovato"));

        customerMapper.updateEntity(customer, request);

        Customer saved = customerRepository.save(customer);
        return customerMapper.toResponse(saved);
    }

    public void delete(Long tenantId, Long id) {
        Customer customer = customerRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Cliente non trovato"));
        customerRepository.delete(customer);
    }
}