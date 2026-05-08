package com.gestiva.purchasing.invoice.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.service.SupplierInvoiceService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/goods-receipts")
public class SupplierInvoicePageController {

    private final SupplierInvoiceService supplierInvoiceService;
    private final TenantContext tenantContext;

    public SupplierInvoicePageController(SupplierInvoiceService supplierInvoiceService,
                                         TenantContext tenantContext) {
        this.supplierInvoiceService = supplierInvoiceService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}/invoice")
    public String createInvoice(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Long invoiceId = null;
        try {
            Long tenantId = tenantContext.getCurrentTenantId();
            invoiceId = supplierInvoiceService.createFromGoodsReceipt(tenantId, id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Fattura creata con successo: " + invoiceId
            );

        }
        catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/goods-receipts/" + id;
        }
        return "redirect:/supplier-invoices/" + invoiceId;
    }
}