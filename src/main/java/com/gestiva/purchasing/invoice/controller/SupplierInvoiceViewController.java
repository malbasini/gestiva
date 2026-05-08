package com.gestiva.purchasing.invoice.controller;

import com.gestiva.purchasing.invoice.web.SupplierInvoiceWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/supplier-invoices")
public class SupplierInvoiceViewController {

    private final SupplierInvoiceWebService supplierInvoiceWebService;
    private final TenantContext tenantContext;

    public SupplierInvoiceViewController(SupplierInvoiceWebService supplierInvoiceWebService,
                                         TenantContext tenantContext) {
        this.supplierInvoiceWebService = supplierInvoiceWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public String list(Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("supplierInvoices", supplierInvoiceWebService.findAll(tenantId));
        model.addAttribute("activeMenu", "supplierInvoices");
        return "purchasing/invoice/supplier-invoice-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("supplierInvoice", supplierInvoiceWebService.getDetail(tenantId, id));
        model.addAttribute("activeMenu", "supplierInvoices");
        return "purchasing/invoice/supplier-invoice-detail";
    }
}