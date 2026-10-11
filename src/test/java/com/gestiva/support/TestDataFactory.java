package com.gestiva.support;

import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.security.tenant.entity.Tenant;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.settings.sequence.entity.DocumentSequence;
import com.gestiva.settings.sequence.repository.DocumentSequenceRepository;
import org.springframework.stereotype.Component;

@Component
public class TestDataFactory {

    private final TenantRepository tenantRepository;
    private final CustomerRepository customerRepository;
    private final DocumentSequenceRepository documentSequenceRepository;

    public TestDataFactory(
            TenantRepository tenantRepository,
            CustomerRepository customerRepository,
            DocumentSequenceRepository documentSequenceRepository) {

        this.tenantRepository = tenantRepository;
        this.customerRepository = customerRepository;
        this.documentSequenceRepository = documentSequenceRepository;
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

    public DocumentSequence createDocumentSequence(
            Long tenantId,
            String documentType) {

        DocumentSequence sequence = new DocumentSequence();

        sequence.setTenantId(tenantId);
        sequence.setDocumentType(documentType);
        sequence.setPrefix(
                "SALES_ORDER".equals(documentType)
                        ? "ORD-2026-"
                        : "TEST-"
        );
        sequence.setNextNumber(1L);
        sequence.setPaddingSize(5);

        return documentSequenceRepository.save(sequence);
    }
}