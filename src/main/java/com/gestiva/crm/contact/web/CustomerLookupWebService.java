package com.gestiva.crm.contact.web;

import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.crm.contact.repository.CustomerSpecifications;
import com.gestiva.security.company.context.CompanyContext;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CustomerLookupWebService {

    private final CustomerRepository customerRepository;
    private final CompanyContext companyContext;

    public CustomerLookupWebService(
            CustomerRepository customerRepository,
            CompanyContext companyContext
    ) {
        this.customerRepository = customerRepository;
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

    public List<CustomerOptionView> findActiveOptions(Long tenantId) {

        Long companyId = requireCompanyId();

        var specification = CustomerSpecifications.hasTenantId(tenantId)
                .and((root, query, cb) ->
                        cb.equal(root.get("companyId"), companyId)
                )
                .and(CustomerSpecifications.hasStatus("ACTIVE"));

        return customerRepository.findAll(
                        specification,
                        Sort.by(Sort.Direction.ASC, "name")
                )
                .stream()
                .map(customer -> {

                    String label = customer.getName();

                    if (customer.getVatNumber() != null
                            && !customer.getVatNumber().isBlank()) {

                        label += " - " + customer.getVatNumber();
                    }

                    return new CustomerOptionView(
                            customer.getId(),
                            label
                    );
                })
                .toList();
    }
}