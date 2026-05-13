package com.gestiva.purchasing.receipt.controller;

import com.gestiva.purchasing.invoice.service.SupplierInvoiceService;
import com.gestiva.purchasing.invoice.web.SupplierInvoiceWebService;
import com.gestiva.purchasing.receipt.service.GoodsReceiptService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/{id}/receive")
    public String receive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Long tenantId = tenantContext.getCurrentTenantId();
            Long goodsReceiptId = goodsReceiptService.createFromPurchaseOrder(tenantId, id);
            return "redirect:/goods-receipts/" + goodsReceiptId;
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/goods-receipts/" + id;
        }
    }
}