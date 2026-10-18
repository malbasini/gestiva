package com.gestiva.warehouse.inventory.movement.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.warehouse.inventory.movement.service.InventoryAdjustmentService;
import com.gestiva.warehouse.inventory.movement.web.InventoryAdjustmentForm;
import com.gestiva.warehouse.item.web.ItemWebService;
import com.gestiva.warehouse.location.service.WarehouseService;
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
    private final CompanyContext companyContext;
    private final WarehouseService warehouseService;

    public InventoryAdjustmentPageController(
            InventoryAdjustmentService inventoryAdjustmentService,
            ItemWebService itemWebService,
            TenantContext tenantContext,
            CompanyContext companyContext,
            WarehouseService warehouseService) {

        this.inventoryAdjustmentService = inventoryAdjustmentService;
        this.itemWebService = itemWebService;
        this.tenantContext = tenantContext;
        this.companyContext = companyContext;
        this.warehouseService = warehouseService;
    }

    @GetMapping("/new")
    public String createForm(
            Model model,
            RedirectAttributes redirectAttributes) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Long companyId;
        try {
            companyId = companyContext.requireCurrentCompanyId();
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Seleziona una società prima di effettuare una rettifica di inventario."
            );
            return "redirect:/company/select";
        }
        InventoryAdjustmentForm form = new InventoryAdjustmentForm();
        form.setMovementDate(LocalDate.now());
        form.setAdjustmentType("IN");
        model.addAttribute("inventoryAdjustmentForm", form);
        populateFormOptions(model, tenantId, companyId);
        return "warehouse/inventory/inventory-adjustment-form";
    }

    @PostMapping
    public String create(
            @Valid InventoryAdjustmentForm inventoryAdjustmentForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long tenantId = tenantContext.getCurrentTenantId();
        Long companyId;
        try {
            companyId = companyContext.requireCurrentCompanyId();
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Seleziona una società prima di registrare una rettifica di inventario."
            );
            return "redirect:/company/select";
        }
        if (bindingResult.hasErrors()) {
            populateFormOptions(model, tenantId, companyId);
            return "warehouse/inventory/inventory-adjustment-form";
        }
        try {
            Long movementId = inventoryAdjustmentService.registerAdjustment(
                    tenantId,
                    inventoryAdjustmentForm
            );
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Rettifica di magazzino registrata correttamente. Movimento n. "
                            + movementId
            );
            return "redirect:/items";

        } catch (BusinessException ex) {
            populateFormOptions(model, tenantId, companyId);
            model.addAttribute("errorMessage", ex.getMessage());
            return "warehouse/inventory/inventory-adjustment-form";
        }
    }
    private void populateFormOptions(
            Model model,
            Long tenantId,
            Long companyId) {

        model.addAttribute(
                "itemOptions",
                itemWebService.findStockManagedOptions(tenantId)
        );
        model.addAttribute(
                "warehouseOptions",
                warehouseService.findActive(tenantId, companyId)
        );
        model.addAttribute("activeMenu", "items");
    }
}