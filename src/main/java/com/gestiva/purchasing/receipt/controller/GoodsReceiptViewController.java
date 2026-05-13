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

    @GetMapping
    public String list(Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("goodsReceipts", goodsReceiptWebService.findAll(tenantId));
        model.addAttribute("activeMenu", "goodsReceipts");
        return "purchasing/receipt/goods-receipt-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("goodsReceipt", goodsReceiptWebService.getDetail(tenantId, id));
        model.addAttribute("activeMenu", "goodsReceipts");
        return "purchasing/receipt/goods-receipt-detail";
    }
    @GetMapping("/{id}/invoice")
    public String invoice(@PathVariable Long id,
                          RedirectAttributes redirectAttributes) {
        try {
            Long tenantId = tenantContext.getCurrentTenantId();
            supplierInvoiceService.createFromGoodsReceipt(tenantId, id);
            redirectAttributes.addFlashAttribute("successMessage", "Fattura generata con successo.");
            return "redirect:/goods-receipts/" + id;
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/goods-receipts/" + id;
        }
    }
}