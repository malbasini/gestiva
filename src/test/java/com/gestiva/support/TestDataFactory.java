package com.gestiva.support;

import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.platform.tenant.entity.Tenant;
import com.gestiva.platform.tenant.repository.TenantRepository;
import org.springframework.stereotype.Component;

@Component
public class TestDataFactory {

    private final TenantRepository tenantRepository;
    private final CustomerRepository customerRepository;

    public TestDataFactory(TenantRepository tenantRepository,
                           CustomerRepository customerRepository) {
        this.tenantRepository = tenantRepository;
        this.customerRepository = customerRepository;
    }

    public Tenant createTenant() {
        Tenant tenant = new Tenant();
        tenant.setName("Tenant Test");
        tenant.setSlug("tenant-test-" + System.nanoTime());
        tenant.setEmail("tenant" + System.nanoTime() + "@example.com");
        tenant.setStatus("ACTIVE");
        tenant.setDefaultLocale("it");
        tenant.setDefaultCurrency("EUR");
        return tenantRepository.save(tenant);
    }

    public Customer createCustomer(Long tenantId, String name) {
        Customer customer = new Customer();
        customer.setTenantId(tenantId);
        customer.setName(name);
        customer.setEmail("customer-" + System.nanoTime() + "@example.com");
        customer.setType("COMPANY");
        customer.setStatus("ACTIVE");
        return customerRepository.save(customer);
    }
}