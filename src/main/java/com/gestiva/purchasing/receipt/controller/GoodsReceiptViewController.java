package com.gestiva.purchasing.receipt.controller;

import com.gestiva.purchasing.invoice.service.SupplierInvoiceService;
import com.gestiva.purchasing.receipt.web.GoodsReceiptWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/goods-receipts")
public class GoodsReceiptViewController {

    private final GoodsReceiptWebService goodsReceiptWebService;
    private final TenantContext tenantContext;
    private final SupplierInvoiceService supplierInvoiceService;

    public GoodsReceiptViewController(GoodsReceiptWebService goodsReceiptWebService,
                                      TenantContext tenantContext,
                                      SupplierInvoiceService supplierInvoiceService) {

        this.goodsReceiptWebService = goodsReceiptWebService;
        this.tenantContext = tenantContext;
        this.supplierInvoiceService = supplierInvoiceService;
    }
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("goodsReceipt", goodsReceiptWebService.getDetail(tenantId, id));
        model.addAttribute("activeMenu", "goodsReceipts");
        return "purchasing/receipt/goods-receipt-detail";
    }
    @PostMapping("/{id}/invoice")
    public String invoice(@PathVariable Long id,Model model,
                          RedirectAttributes redirectAttributes) {
        try {
            Long tenantId = tenantContext.getCurrentTenantId();
            supplierInvoiceService.createFromGoodsReceipt(tenantId, id);
            redirectAttributes.addFlashAttribute("successMessage", "Fattura generata con successo.");
            model.addAttribute("activeMenu", "goodsReceipts");
            return "redirect:/goods-receipts/" + id;
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/goods-receipts/" + id;
        }
    }
    @GetMapping
    public String list(@RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "10") int size,
                       @RequestParam(name = "q", required = false) String q,
                       @RequestParam(name = "dateFrom", required = false)
                       @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                       java.time.LocalDate dateFrom,
                       @RequestParam(name = "dateTo", required = false)
                       @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                       java.time.LocalDate dateTo,
                       Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();
        var resultPage = goodsReceiptWebService.findPage(tenantId, page, size, q, dateFrom, dateTo);
        model.addAttribute("goodsReceipts", resultPage);
        model.addAttribute("page", resultPage);
        model.addAttribute("q", q);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("size", size);
        model.addAttribute("activeMenu", "goodsReceipts");
        return "purchasing/receipt/goods-receipt-list";
    }
}