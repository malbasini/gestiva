package com.gestiva.purchasing.order.controller;

import com.gestiva.purchasing.order.web.PurchaseOrderForm;
import com.gestiva.purchasing.order.web.PurchaseOrderWebService;
import com.gestiva.purchasing.supplier.web.SupplierWebService;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.warehouse.item.web.ItemWebService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/purchase-orders")
public class PurchaseOrderPageController {

    private final PurchaseOrderWebService purchaseOrderWebService;
    private final SupplierWebService supplierWebService;
    private final ItemWebService itemWebService;
    private final TenantContext tenantContext;

    public PurchaseOrderPageController(PurchaseOrderWebService purchaseOrderWebService,
                                       SupplierWebService supplierWebService,
                                       ItemWebService itemWebService,
                                       TenantContext tenantContext) {
        this.purchaseOrderWebService = purchaseOrderWebService;
        this.supplierWebService = supplierWebService;
        this.itemWebService = itemWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public String list(Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("purchaseOrders", purchaseOrderWebService.findAll(tenantId));
        model.addAttribute("activeMenu", "purchaseOrders");
        return "purchasing/order/purchase-order-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("purchaseOrder", purchaseOrderWebService.getDetail(tenantId, id));
        model.addAttribute("activeMenu", "purchaseOrders");
        return "purchasing/order/purchase-order-detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("purchaseOrderForm", purchaseOrderWebService.buildCreateForm());
        model.addAttribute("supplierOptions", supplierWebService.findOptions(tenantId));
        model.addAttribute("itemOptions", itemWebService.findOptions(tenantId));
        model.addAttribute("formMode", "create");
        model.addAttribute("activeMenu", "purchaseOrders");
        return "purchasing/order/purchase-order-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("purchaseOrderForm") PurchaseOrderForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = tenantContext.getCurrentTenantId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("supplierOptions", supplierWebService.findOptions(tenantId));
            model.addAttribute("itemOptions", itemWebService.findOptions(tenantId));
            model.addAttribute("formMode", "create");
            model.addAttribute("activeMenu", "purchaseOrders");
            return "purchasing/order/purchase-order-form";
        }

        Long id = purchaseOrderWebService.create(tenantId, form);
        redirectAttributes.addFlashAttribute("successMessage", "Ordine fornitore creato con successo.");
        return "redirect:/purchase-orders/" + id;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("purchaseOrderForm", purchaseOrderWebService.getForm(tenantId, id));
        model.addAttribute("supplierOptions", supplierWebService.findOptions(tenantId));
        model.addAttribute("itemOptions", itemWebService.findOptions(tenantId));
        model.addAttribute("purchaseOrderId", id);
        model.addAttribute("formMode", "edit");
        model.addAttribute("activeMenu", "purchaseOrders");
        return "purchasing/order/purchase-order-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("purchaseOrderForm") PurchaseOrderForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = tenantContext.getCurrentTenantId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("supplierOptions", supplierWebService.findOptions(tenantId));
            model.addAttribute("itemOptions", itemWebService.findOptions(tenantId));
            model.addAttribute("purchaseOrderId", id);
            model.addAttribute("formMode", "edit");
            model.addAttribute("activeMenu", "purchaseOrders");
            return "purchasing/order/purchase-order-form";
        }

        purchaseOrderWebService.update(tenantId, id, form);
        redirectAttributes.addFlashAttribute("successMessage", "Ordine fornitore aggiornato con successo.");
        return "redirect:/purchase-orders/" + id;
    }
}