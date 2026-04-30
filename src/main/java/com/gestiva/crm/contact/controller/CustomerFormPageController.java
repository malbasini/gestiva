package com.gestiva.crm.contact.controller;

import com.gestiva.crm.contact.web.CustomerManageWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customers")
public class CustomerFormPageController {

    private final CustomerManageWebService customerManageWebService;
    private final TenantContext tenantContext;

    public CustomerFormPageController(CustomerManageWebService customerManageWebService,
                                      TenantContext tenantContext) {
        this.customerManageWebService = customerManageWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(required = false) Long tenantId, Model model) {
        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        model.addAttribute("customerForm", customerManageWebService.buildCreateForm());
        model.addAttribute("formMode", "create");
        model.addAttribute("tenantId", resolvedTenantId);

        return "customer/customer-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @RequestParam(required = false) Long tenantId,
                           Model model) {
        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        model.addAttribute("customerForm", customerManageWebService.buildEditForm(resolvedTenantId, id));
        model.addAttribute("customerId", id);
        model.addAttribute("formMode", "edit");
        model.addAttribute("tenantId", resolvedTenantId);

        return "customer/customer-form";
    }
}