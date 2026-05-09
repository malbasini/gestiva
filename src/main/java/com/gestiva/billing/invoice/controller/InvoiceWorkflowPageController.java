package com.gestiva.billing.invoice.controller;

import com.gestiva.billing.invoice.service.InvoiceWorkflowService;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/invoices")
public class InvoiceWorkflowPageController {

    private final InvoiceWorkflowService invoiceWorkflowService;
    private final TenantContext tenantContext;

    public InvoiceWorkflowPageController(InvoiceWorkflowService invoiceWorkflowService,
                                         TenantContext tenantContext) {
        this.invoiceWorkflowService = invoiceWorkflowService;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         @RequestParam(required = false) Long tenantId,
                         RedirectAttributes redirectAttributes) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        try {
            invoiceWorkflowService.cancel(resolvedTenantId, id);
            redirectAttributes.addFlashAttribute("successMessage", "Fattura annullata con successo.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/invoices/" + id + "?tenantId=" + resolvedTenantId;
        }

        return "redirect:/invoices/" + id + "?tenantId=" + resolvedTenantId;
    }
}