package com.gestiva.sales.order.controller;

import com.gestiva.sales.order.web.SalesOrderManageWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/orders")
public class SalesOrderFormPageController {

    private final SalesOrderManageWebService salesOrderManageWebService;
    private final TenantContext tenantContext;

    public SalesOrderFormPageController(SalesOrderManageWebService salesOrderManageWebService,
                                        TenantContext tenantContext) {
        this.salesOrderManageWebService = salesOrderManageWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @RequestParam(required = false) Long tenantId,
                           Model model) {
        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        var form = salesOrderManageWebService.buildEditForm(resolvedTenantId, id);

        model.addAttribute("orderForm", form);
        model.addAttribute("totalsPreview", salesOrderManageWebService.calculatePreviewTotals(form));
        model.addAttribute("orderId", id);
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("activeMenu", "orders");

        return "order/order-form";
    }
}