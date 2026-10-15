package com.gestiva.crm.contact.service;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.crm.contact.dto.CustomerCreateRequest;
import com.gestiva.crm.contact.dto.CustomerResponse;
import com.gestiva.crm.contact.dto.CustomerUpdateRequest;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.crm.contact.mapper.CustomerMapper;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.security.company.context.CompanyContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CompanyContext companyContext;

    public CustomerService(CustomerRepository customerRepository,
                           CustomerMapper customerMapper,
                           CompanyContext companyContext) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
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

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll(Long tenantId) {

        Long companyId = requireCompanyId();

        return customerRepository
                .findByTenantIdAndCompanyIdOrderByNameAsc(
                        tenantId,
                        companyId
                )
                .stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long tenantId, Long id) {

        Long companyId = requireCompanyId();

        Customer customer = customerRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        id
                )
                .orElseThrow(() ->
                        new NotFoundException("Cliente non trovato")
                );

        return customerMapper.toResponse(customer);
    }

    public CustomerResponse create(
            Long tenantId,
            CustomerCreateRequest request
    ) {

        Long companyId = requireCompanyId();

        Customer customer = customerMapper.toEntity(request);

        customer.setTenantId(tenantId);
        customer.setCompanyId(companyId);

        Customer saved = customerRepository.save(customer);

        return customerMapper.toResponse(saved);
    }

    public CustomerResponse update(
            Long tenantId,
            Long id,
            CustomerUpdateRequest request
    ) {

        Long companyId = requireCompanyId();

        Customer customer = customerRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        id
                )
                .orElseThrow(() ->
                        new NotFoundException("Cliente non trovato")
                );

        customerMapper.updateEntity(customer, request);

        // L'appartenenza alla società non deve cambiare.
        customer.setTenantId(tenantId);
        customer.setCompanyId(companyId);

        Customer saved = customerRepository.save(customer);

        return customerMapper.toResponse(saved);
    }

    public void delete(Long tenantId, Long id) {

        Long companyId = requireCompanyId();

        Customer customer = customerRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId,
                        companyId,
                        id
                )
                .orElseThrow(() ->
                        new NotFoundException("Cliente non trovato")
                );

        customerRepository.delete(customer);
    }
}