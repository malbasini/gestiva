package com.gestiva.purchasing.invoice.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.service.SupplierInvoiceService;
import com.gestiva.purchasing.invoice.web.SupplierInvoiceWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/supplier-invoices")
public class SupplierInvoicePageController {

    private final SupplierInvoiceService supplierInvoiceService;
    private final TenantContext tenantContext;
    private final SupplierInvoiceWebService supplierInvoiceWebService;

    public SupplierInvoicePageController(SupplierInvoiceService supplierInvoiceService,
                                         TenantContext tenantContext,
                                         SupplierInvoiceWebService supplierInvoiceWebService) {

        this.supplierInvoiceService = supplierInvoiceService;
        this.tenantContext = tenantContext;
        this.supplierInvoiceWebService = supplierInvoiceWebService;
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
    @GetMapping
    public String list(@RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "10") int size,
                       @RequestParam(name = "q", required = false) String q,
                       @RequestParam(name = "status", required = false) String status,
                       @RequestParam(name = "dateFrom", required = false)
                       @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                       java.time.LocalDate dateFrom,
                       @RequestParam(name = "dateTo", required = false)
                       @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                       java.time.LocalDate dateTo,
                       Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();
        var resultPage = supplierInvoiceWebService.findPage(tenantId, page, size, q, status, dateFrom, dateTo);
        model.addAttribute("page", resultPage);
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("size", size);
        model.addAttribute("activeMenu", "supplierInvoices");
        return "purchasing/invoice/supplier-invoice-list";
    }









}