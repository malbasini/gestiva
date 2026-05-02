package com.gestiva.sales.order.controller;

import com.gestiva.sales.order.web.SalesOrderForm;
import com.gestiva.sales.order.web.SalesOrderManageWebService;
import com.gestiva.security.usercontext.TenantContext;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class SalesOrderFormSubmitController {

    private final SalesOrderManageWebService salesOrderManageWebService;
    private final TenantContext tenantContext;

    public SalesOrderFormSubmitController(SalesOrderManageWebService salesOrderManageWebService,
                                          TenantContext tenantContext) {
        this.salesOrderManageWebService = salesOrderManageWebService;
        this.tenantContext = tenantContext;
    }

    @PostMapping(value = "/{id}", params = "addLine")
    public String addLine(@PathVariable Long id,
                          @ModelAttribute("orderForm") SalesOrderForm orderForm,
                          @RequestParam(required = false) Long tenantId,
                          Model model) {
        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        salesOrderManageWebService.addLine(orderForm);

        model.addAttribute("orderId", id);
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("totalsPreview", salesOrderManageWebService.calculatePreviewTotals(orderForm));
        model.addAttribute("activeMenu", "orders");

        return "order/order-form";
    }

    @PostMapping(value = "/{id}", params = "removeLine")
    public String removeLine(@PathVariable Long id,
                             @ModelAttribute("orderForm") SalesOrderForm orderForm,
                             @RequestParam int removeLine,
                             @RequestParam(required = false) Long tenantId,
                             Model model) {
        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        salesOrderManageWebService.removeLine(orderForm, removeLine);

        model.addAttribute("orderId", id);
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("totalsPreview", salesOrderManageWebService.calculatePreviewTotals(orderForm));
        model.addAttribute("activeMenu", "orders");

        return "order/order-form";
    }

    @PostMapping(value = "/{id}", params = "recalculate")
    public String recalculate(@PathVariable Long id,
                              @ModelAttribute("orderForm") SalesOrderForm orderForm,
                              @RequestParam(required = false) Long tenantId,
                              Model model) {
        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        model.addAttribute("orderId", id);
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("totalsPreview", salesOrderManageWebService.calculatePreviewTotals(orderForm));
        model.addAttribute("activeMenu", "orders");

        return "order/order-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("orderForm") SalesOrderForm orderForm,
                         BindingResult bindingResult,
                         @RequestParam(required = false) Long tenantId,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("orderId", id);
            model.addAttribute("tenantId", resolvedTenantId);
            model.addAttribute("totalsPreview", salesOrderManageWebService.calculatePreviewTotals(orderForm));
            model.addAttribute("activeMenu", "orders");
            return "order/order-form";
        }

        Long orderId = salesOrderManageWebService.update(resolvedTenantId, id, orderForm);
        redirectAttributes.addFlashAttribute("successMessage", "Ordine aggiornato con successo.");

        return "redirect:/orders/" + orderId + "?tenantId=" + resolvedTenantId;
    }
}