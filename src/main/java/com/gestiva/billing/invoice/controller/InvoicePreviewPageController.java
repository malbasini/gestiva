package com.gestiva.billing.invoice.controller;

import com.gestiva.billing.invoice.web.InvoiceDocumentWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/invoices")
public class InvoicePreviewPageController {

    private final InvoiceDocumentWebService invoiceDocumentWebService;
    private final TenantContext tenantContext;

    public InvoicePreviewPageController(InvoiceDocumentWebService invoiceDocumentWebService,
                                        TenantContext tenantContext) {
        this.invoiceDocumentWebService = invoiceDocumentWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}/preview")
    public String preview(@PathVariable Long id,
                          @RequestParam(required = false) Long tenantId,
                          Model model) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        model.addAttribute("invoice", invoiceDocumentWebService.getDocument(resolvedTenantId, id));
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("activeMenu", "invoices");

        return "invoice/invoice-preview";
    }
}