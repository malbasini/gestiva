package com.gestiva.sales.order.controller;

import com.gestiva.sales.order.pdf.SalesOrderPdfDataService;
import com.gestiva.sales.order.pdf.SalesOrderPdfView;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/orders")
public class SalesOrderPreviewController {

    private final SalesOrderPdfDataService salesOrderPdfDataService;
    private final TenantContext tenantContext;

    public SalesOrderPreviewController(SalesOrderPdfDataService salesOrderPdfDataService,
                                       TenantContext tenantContext) {
        this.salesOrderPdfDataService = salesOrderPdfDataService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}/preview")
    public String previewOrder(@PathVariable Long id,
                               @RequestParam Long tenantId,
                               Model model) {
        SalesOrderPdfView order = salesOrderPdfDataService.buildView(tenantId, id);

        model.addAttribute("order", order);
        model.addAttribute("previewMode", true);
        model.addAttribute("orderId", id);
        model.addAttribute("tenantId", tenantId);

        return "order/order-preview";
    }
}