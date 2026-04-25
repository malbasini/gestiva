package com.gestiva.crm.contact.mapper;

import com.gestiva.crm.contact.dto.CustomerCreateRequest;
import com.gestiva.crm.contact.dto.CustomerResponse;
import com.gestiva.crm.contact.dto.CustomerUpdateRequest;
import com.gestiva.crm.contact.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toEntity(CustomerCreateRequest request) {
        Customer c = new Customer();
        c.setName(request.getName());
        c.setVatNumber(request.getVatNumber());
        c.setTaxCode(request.getTaxCode());
        c.setEmail(request.getEmail());
        c.setPhone(request.getPhone());
        c.setType(request.getType());
        c.setStatus(request.getStatus());
        return c;
    }

    public void updateEntity(Customer customer, CustomerUpdateRequest request) {
        customer.setName(request.getName());
        customer.setVatNumber(request.getVatNumber());
        customer.setTaxCode(request.getTaxCode());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setType(request.getType());
        customer.setStatus(request.getStatus());
    }

    public CustomerResponse toResponse(Customer c) {
        CustomerResponse r = new CustomerResponse();
        r.setId(c.getId());
        r.setName(c.getName());
        r.setVatNumber(c.getVatNumber());
        r.setTaxCode(c.getTaxCode());
        r.setEmail(c.getEmail());
        r.setPhone(c.getPhone());
        r.setType(c.getType());
        r.setStatus(c.getStatus());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        return r;
    }
}