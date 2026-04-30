package com.gestiva.crm.contact.web;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.crm.contact.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomerManageWebService {

    private final CustomerRepository customerRepository;

    public CustomerManageWebService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerForm buildCreateForm() {
        CustomerForm form = new CustomerForm();
        form.setType("COMPANY");
        form.setStatus("ACTIVE");
        form.setCountryCode("IT");
        return form;
    }

    public CustomerForm buildEditForm(Long tenantId, Long customerId) {
        Customer customer = customerRepository.findByTenantIdAndId(tenantId, customerId)
                .orElseThrow(() -> new NotFoundException("Cliente non trovato"));

        CustomerForm form = new CustomerForm();
        form.setName(customer.getName());
        form.setEmail(customer.getEmail());
        form.setPhone(customer.getPhone());
        form.setVatNumber(customer.getVatNumber());
        form.setType(customer.getType());
        form.setStatus(customer.getStatus());
        form.setAddressLine1(customer.getAddressLine1());
        form.setCity(customer.getCity());
        form.setPostalCode(customer.getPostalCode());
        form.setCountryCode(customer.getCountryCode());
        return form;
    }

    public Long create(Long tenantId, CustomerForm form) {
        Customer customer = new Customer();
        customer.setTenantId(tenantId);
        applyForm(customer, form);

        Customer saved = customerRepository.save(customer);
        return saved.getId();
    }

    public Long update(Long tenantId, Long customerId, CustomerForm form) {
        Customer customer = customerRepository.findByTenantIdAndId(tenantId, customerId)
                .orElseThrow(() -> new NotFoundException("Cliente non trovato"));

        applyForm(customer, form);

        Customer saved = customerRepository.save(customer);
        return saved.getId();
    }

    private void applyForm(Customer customer, CustomerForm form) {
        customer.setName(trimToNull(form.getName()));
        customer.setEmail(trimToNull(form.getEmail()));
        customer.setPhone(trimToNull(form.getPhone()));
        customer.setVatNumber(trimToNull(form.getVatNumber()));
        customer.setType(trimToNull(form.getType()));
        customer.setStatus(trimToNull(form.getStatus()));
        customer.setAddressLine1(trimToNull(form.getAddressLine1()));
        customer.setCity(trimToNull(form.getCity()));
        customer.setPostalCode(trimToNull(form.getPostalCode()));
        customer.setCountryCode(trimToNull(form.getCountryCode()));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}