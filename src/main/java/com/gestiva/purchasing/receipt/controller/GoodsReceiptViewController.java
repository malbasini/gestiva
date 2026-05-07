package com.gestiva.purchasing.receipt.controller;

import com.gestiva.purchasing.receipt.web.GoodsReceiptWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/goods-receipts")
public class GoodsReceiptViewController {

    private final GoodsReceiptWebService goodsReceiptWebService;
    private final TenantContext tenantContext;

    public GoodsReceiptViewController(GoodsReceiptWebService goodsReceiptWebService,
                                      TenantContext tenantContext) {
        this.goodsReceiptWebService = goodsReceiptWebService;
        this.tenantContext = tenantContext;
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
}