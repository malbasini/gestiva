package com.gestiva.crm.contact.controller;

import com.gestiva.crm.contact.dto.CustomerSearchRequest;
import com.gestiva.crm.contact.web.CustomerWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customers")
public class CustomerPageController {

    private final CustomerWebService customerWebService;
    private final TenantContext tenantContext;

    public CustomerPageController(CustomerWebService customerWebService,
                                  TenantContext tenantContext) {
        this.customerWebService = customerWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public String listCustomers(@RequestParam(required = false) String search,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String type,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(defaultValue = "name") String sortBy,
                                @RequestParam(defaultValue = "asc") String sortDir,
                                @RequestParam(required = false) Long tenantId,
                                Model model) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        var pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        CustomerSearchRequest request = new CustomerSearchRequest();
        request.setSearch(search);
        request.setStatus(status);
        request.setType(type);

        var result = customerWebService.search(resolvedTenantId, request, pageable);

        model.addAttribute("customersPage", result);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("activeMenu", "customers");
        return "customer/customer-list";
    }
}
