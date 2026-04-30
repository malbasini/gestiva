package com.gestiva.crm.contact.controller;

import com.gestiva.crm.contact.web.CustomerForm;
import com.gestiva.crm.contact.web.CustomerManageWebService;
import com.gestiva.security.usercontext.TenantContext;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
public class CustomerFormSubmitController {

    private final CustomerManageWebService customerManageWebService;
    private final TenantContext tenantContext;

    public CustomerFormSubmitController(CustomerManageWebService customerManageWebService,
                                        TenantContext tenantContext) {
        this.customerManageWebService = customerManageWebService;
        this.tenantContext = tenantContext;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("customerForm") CustomerForm customerForm,
                         BindingResult bindingResult,
                         @RequestParam(required = false) Long tenantId,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("formMode", "create");
            model.addAttribute("tenantId", resolvedTenantId);
            return "customer/customer-form";
        }

        Long customerId = customerManageWebService.create(resolvedTenantId, customerForm);
        redirectAttributes.addFlashAttribute("successMessage", "Cliente creato con successo.");

        return "redirect:/customers/" + customerId + "?tenantId=" + resolvedTenantId;
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("customerForm") CustomerForm customerForm,
                         BindingResult bindingResult,
                         @RequestParam(required = false) Long tenantId,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("formMode", "edit");
            model.addAttribute("customerId", id);
            model.addAttribute("tenantId", resolvedTenantId);
            model.addAttribute("activeMenu", "customers");
            return "customer/customer-form";
        }

        Long customerId = customerManageWebService.update(resolvedTenantId, id, customerForm);
        redirectAttributes.addFlashAttribute("successMessage", "Cliente aggiornato con successo.");

        return "redirect:/customers/" + customerId + "?tenantId=" + resolvedTenantId;
    }
}