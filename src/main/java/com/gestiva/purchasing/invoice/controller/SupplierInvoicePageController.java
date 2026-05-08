package com.gestiva.purchasing.invoice.controller;

import com.gestiva.purchasing.invoice.service.SupplierInvoiceService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String createInvoice(@PathVariable Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Long invoiceId = supplierInvoiceService.createFromGoodsReceipt(tenantId, id);
        return "redirect:/supplier-invoices/" + invoiceId;
    }
}