package com.gestiva.crm.contact.controller;

import com.gestiva.crm.contact.web.CustomerDetailWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customers")
public class CustomerDetailPageController {

    private final CustomerDetailWebService customerDetailWebService;
    private final TenantContext tenantContext;

    public CustomerDetailPageController(CustomerDetailWebService customerDetailWebService,
                                        TenantContext tenantContext) {
        this.customerDetailWebService = customerDetailWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(required = false) Long tenantId,
                         Model model) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        var customer = customerDetailWebService.getDetail(resolvedTenantId, id);

        model.addAttribute("customer", customer);
        model.addAttribute("tenantId", resolvedTenantId);

        return "customer/customer-detail";
    }
}