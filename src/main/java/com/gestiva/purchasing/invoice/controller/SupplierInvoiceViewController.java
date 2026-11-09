package com.gestiva.purchasing.invoice.controller;

import com.gestiva.purchasing.invoice.web.SupplierInvoiceWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/supplier-invoices")
public class SupplierInvoiceViewController {

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.core.env.Environment environment;

    private final SupplierInvoiceWebService supplierInvoiceWebService;
    private final TenantContext tenantContext;

    public SupplierInvoiceViewController(SupplierInvoiceWebService supplierInvoiceWebService,
                                         TenantContext tenantContext) {
        this.supplierInvoiceWebService = supplierInvoiceWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            Model model,
            org.springframework.security.core.Authentication authentication) {
        Long tenantId = tenantContext.getCurrentTenantId();
        var invoice = supplierInvoiceWebService.getDetail(tenantId, id);
        model.addAttribute("supplierInvoice", invoice);

        boolean testProfileActive =
                java.util.Arrays.asList(environment.getActiveProfiles())
                        .contains("test");

        boolean enabled = environment.getProperty(
                "gestiva.purchasing.supplier-invoice.enabled",
                Boolean.class,
                false
        );

        boolean authorized = authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(a ->
                                "ROLE_ADMIN".equals(a.getAuthority())
                                || "ROLE_ACCOUNTING".equals(a.getAuthority())
                        );

        model.addAttribute(
                "canRegisterSupplierInvoice",
                testProfileActive
                        && enabled
                        && authorized
                        && "DRAFT".equals(invoice.getStatus())
        );

        boolean canCreateSupplierCreditNote =
                authentication != null
                        && authentication.isAuthenticated()
                        && authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                "ROLE_ADMIN".equals(authority.getAuthority())
                                        || "ROLE_PURCHASING".equals(authority.getAuthority())
                                        || "ROLE_ACCOUNTING".equals(authority.getAuthority()))
                        && "REGISTERED".equals(invoice.getStatus());

        model.addAttribute(
                "canCreateSupplierCreditNote",
                canCreateSupplierCreditNote
        );

        model.addAttribute("activeMenu", "supplierInvoices");
        return "purchasing/invoice/supplier-invoice-detail";
    }
}