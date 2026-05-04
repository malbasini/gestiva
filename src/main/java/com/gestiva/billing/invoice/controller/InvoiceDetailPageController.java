package com.gestiva.billing.invoice.controller;

import com.gestiva.billing.invoice.web.InvoiceDetailWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/invoices")
public class InvoiceDetailPageController {

    private final InvoiceDetailWebService invoiceDetailWebService;
    private final TenantContext tenantContext;

    public InvoiceDetailPageController(InvoiceDetailWebService invoiceDetailWebService,
                                       TenantContext tenantContext) {
        this.invoiceDetailWebService = invoiceDetailWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(required = false) Long tenantId,
                         Model model) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        model.addAttribute("invoice", invoiceDetailWebService.getDetail(resolvedTenantId, id));
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("activeMenu", "invoices");

        return "invoice/invoice-detail";
    }
}