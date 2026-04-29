package com.gestiva.sales.quote.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.sales.order.dto.SalesOrderResponse;
import com.gestiva.sales.order.service.SalesOrderService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/quotes")
public class QuoteConversionPageController {

    private final SalesOrderService salesOrderService;
    private final TenantContext tenantContext;

    public QuoteConversionPageController(SalesOrderService salesOrderService,
                                         TenantContext tenantContext) {
        this.salesOrderService = salesOrderService;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{id}/convert")
    public String convertToOrder(@PathVariable Long id,
                                 @RequestParam(required = false) Long tenantId,
                                 RedirectAttributes redirectAttributes) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        try {
            SalesOrderResponse order = salesOrderService.convertFromQuote(resolvedTenantId, id);
            redirectAttributes.addFlashAttribute("successMessage", "Preventivo convertito in ordine con successo.");
            return "redirect:/orders/" + order.getId() + "?tenantId=" + resolvedTenantId;
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/quotes/" + id + "?tenantId=" + resolvedTenantId;
        }
    }
}