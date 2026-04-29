package com.gestiva.crm.contact.web;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.crm.contact.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerDetailWebService {

    private final CustomerRepository customerRepository;

    public CustomerDetailWebService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerDetailView getDetail(Long tenantId, Long customerId) {
        var customer = customerRepository.findByTenantIdAndId(tenantId, customerId)
                .orElseThrow(() -> new NotFoundException("Cliente non trovato"));

        CustomerDetailView view = new CustomerDetailView();
        view.setId(customer.getId());
        view.setName(customer.getName());
        view.setEmail(customer.getEmail());
        view.setPhone(customer.getPhone());
        view.setVatNumber(customer.getVatNumber());
        view.setType(customer.getType());
        view.setStatus(customer.getStatus());
        view.setAddressLine1(customer.getAddressLine1());
        view.setCity(customer.getCity());
        view.setPostalCode(customer.getPostalCode());
        view.setCountryCode(customer.getCountryCode());

        return view;
    }
}