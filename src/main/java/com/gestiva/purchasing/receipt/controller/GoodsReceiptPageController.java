package com.gestiva.purchasing.receipt.controller;

import com.gestiva.purchasing.invoice.service.SupplierInvoiceService;
import com.gestiva.purchasing.invoice.web.SupplierInvoiceWebService;
import com.gestiva.purchasing.receipt.service.GoodsReceiptService;
import com.gestiva.purchasing.receipt.web.GoodsReceiptWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/purchase-orders")
public class GoodsReceiptPageController {

    private final GoodsReceiptService goodsReceiptService;
    private final TenantContext tenantContext;



    public GoodsReceiptPageController(GoodsReceiptService goodsReceiptService,
                                      TenantContext tenantContext) {

        this.goodsReceiptService = goodsReceiptService;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{id}/receive")
    public String receive(@PathVariable Long id, RedirectAttributes redirectAttributes,Model model) {
        try {
            Long tenantId = tenantContext.getCurrentTenantId();
            Long goodsReceiptId = goodsReceiptService.createFromPurchaseOrder(tenantId, id);
            redirectAttributes.addFlashAttribute("successMessage", "Ricezione merce creata con successo.");
            model.addAttribute("activeMenu", "goodsReceipts");
            return "redirect:/goods-receipts/" + goodsReceiptId;
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/goods-receipts/" + id;
        }
    }

}