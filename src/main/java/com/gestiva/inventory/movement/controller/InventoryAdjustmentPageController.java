package com.gestiva.inventory.movement.controller;

import com.gestiva.inventory.item.web.ItemWebService;
import com.gestiva.inventory.movement.service.InventoryAdjustmentService;
import com.gestiva.inventory.movement.web.InventoryAdjustmentForm;
import com.gestiva.security.usercontext.TenantContext;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/inventory-adjustments")
public class InventoryAdjustmentPageController {

    private final InventoryAdjustmentService inventoryAdjustmentService;
    private final ItemWebService itemWebService;
    private final TenantContext tenantContext;

    public InventoryAdjustmentPageController(InventoryAdjustmentService inventoryAdjustmentService,
                                             ItemWebService itemWebService,
                                             TenantContext tenantContext) {
        this.inventoryAdjustmentService = inventoryAdjustmentService;
        this.itemWebService = itemWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        InventoryAdjustmentForm form = new InventoryAdjustmentForm();
        form.setMovementDate(LocalDate.now());
        form.setAdjustmentType("IN");

        model.addAttribute("inventoryAdjustmentForm", form);
        model.addAttribute("itemOptions", itemWebService.findStockManagedOptions(tenantId));
        model.addAttribute("activeMenu", "items");

        return "warehouse/inventory/inventory-adjustment-form";
    }

    @PostMapping
    public String create(@Valid InventoryAdjustmentForm inventoryAdjustmentForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = tenantContext.getCurrentTenantId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("itemOptions", itemWebService.findStockManagedOptions(tenantId));
            model.addAttribute("activeMenu", "items");
            return "warehouse/inventory/inventory-adjustment-form";
        }
        Long itemId = 0L;
        try {
            inventoryAdjustmentService.registerAdjustment(tenantId, inventoryAdjustmentForm);
            itemId = inventoryAdjustmentForm.getItemId();
            redirectAttributes.addFlashAttribute("successMessage", "Rettifica inventario registrata con successo.");
            return "redirect:/items/" + itemId + "/inventory";
        }
        catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            model.addAttribute("itemOptions", itemWebService.findStockManagedOptions(tenantId));
            model.addAttribute("activeMenu", "items");
            model.addAttribute("errorMessage", ex.getMessage());
            return "warehouse/inventory/inventory-adjustment-form";

        }
    }
}