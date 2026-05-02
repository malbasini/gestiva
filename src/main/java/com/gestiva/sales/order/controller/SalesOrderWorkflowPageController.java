package com.gestiva.sales.order.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.sales.order.service.SalesOrderWorkflowService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class SalesOrderWorkflowPageController {

    private final SalesOrderWorkflowService salesOrderWorkflowService;
    private final TenantContext tenantContext;

    public SalesOrderWorkflowPageController(SalesOrderWorkflowService salesOrderWorkflowService,
                                            TenantContext tenantContext) {
        this.salesOrderWorkflowService = salesOrderWorkflowService;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{id}/fulfill")
    public String fulfill(@PathVariable Long id,
                          @RequestParam(required = false) Long tenantId,
                          RedirectAttributes redirectAttributes) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        try {
            salesOrderWorkflowService.markFulfilled(resolvedTenantId, id);
            redirectAttributes.addFlashAttribute("successMessage", "Ordine evaso con successo.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/orders/" + id + "?tenantId=" + resolvedTenantId;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         @RequestParam(required = false) Long tenantId,
                         RedirectAttributes redirectAttributes) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        try {
            salesOrderWorkflowService.cancel(resolvedTenantId, id);
            redirectAttributes.addFlashAttribute("successMessage", "Ordine annullato con successo.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/orders/" + id + "?tenantId=" + resolvedTenantId;
    }
}