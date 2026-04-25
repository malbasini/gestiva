package com.gestiva.crm.contact.controller;

import com.gestiva.crm.contact.dto.CustomerCreateRequest;
import com.gestiva.crm.contact.dto.CustomerResponse;
import com.gestiva.crm.contact.dto.CustomerUpdateRequest;
import com.gestiva.crm.contact.service.CustomerService;
import com.gestiva.security.usercontext.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final TenantContext tenantContext;

    public CustomerController(CustomerService customerService,
                              TenantContext tenantContext) {
        this.customerService = customerService;
        this.tenantContext = tenantContext;
    }

    // TODO: sostituire con tenant reale preso dal contesto di sicurezza
    private Long getTenantId() {
        return tenantContext.getCurrentTenantId();
    }

    @GetMapping
    public List<CustomerResponse> findAll() {
        return customerService.findAll(getTenantId());
    }

    @GetMapping("/{id}")
    public CustomerResponse findById(@PathVariable Long id) {
        return customerService.findById(getTenantId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@Valid @RequestBody CustomerCreateRequest request) {
        return customerService.create(getTenantId(), request);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id,
                                   @Valid @RequestBody CustomerUpdateRequest request) {
        return customerService.update(getTenantId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        customerService.delete(getTenantId(), id);
    }
}