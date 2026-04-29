package com.gestiva.sales.order.controller;

import com.gestiva.sales.order.web.SalesOrderDetailWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/orders")
public class SalesOrderDetailPageController {

    private final SalesOrderDetailWebService salesOrderDetailWebService;
    private final TenantContext tenantContext;

    public SalesOrderDetailPageController(SalesOrderDetailWebService salesOrderDetailWebService,
                                          TenantContext tenantContext) {
        this.salesOrderDetailWebService = salesOrderDetailWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(required = false) Long tenantId,
                         Model model) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        var order = salesOrderDetailWebService.getDetail(resolvedTenantId, id);

        model.addAttribute("order", order);
        model.addAttribute("tenantId", resolvedTenantId);

        return "order/order-detail";
    }
}