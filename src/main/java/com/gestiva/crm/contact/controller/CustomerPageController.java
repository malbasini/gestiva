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
    public String list(@RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "10") int size,
                       @RequestParam(name = "q", required = false) String q,
                       @RequestParam(name = "status", required = false) String status,
                       Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();
        var resultPage = customerWebService.findPage(tenantId, page, size, q, status);

        model.addAttribute("page", resultPage);
        model.addAttribute("tenantId", tenantId);
        model.addAttribute("customersPage", resultPage);
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("size", size);
        model.addAttribute("activeMenu", "customers");

        return "customer/customer-list";
    }

}
