package com.gestiva.billing.invoice.controller;

import com.gestiva.billing.invoice.dto.InvoiceResponse;
import com.gestiva.billing.invoice.service.InvoiceService;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/delivery-notes")
public class InvoiceCreationPageController {

    private final InvoiceService invoiceService;
    private final TenantContext tenantContext;

    public InvoiceCreationPageController(InvoiceService invoiceService,
                                         TenantContext tenantContext) {
        this.invoiceService = invoiceService;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{id}/invoice")
    public String createInvoice(@PathVariable Long id,
                                @RequestParam(required = false) Long tenantId,
                                RedirectAttributes redirectAttributes) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        try {
            InvoiceResponse response = invoiceService.createFromDeliveryNote(resolvedTenantId, id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Fattura creata con successo: " + response.getInvoiceNumber()
            );
            return "redirect:/invoices/" + response.getId() + "?tenantId=" + resolvedTenantId;
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/delivery-notes/" + id + "?tenantId=" + resolvedTenantId;
        }
    }
}