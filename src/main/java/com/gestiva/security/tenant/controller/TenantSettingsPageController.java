package com.gestiva.security.tenant.controller;

import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.security.tenant.web.TenantInventoryValuationForm;
import com.gestiva.security.tenant.web.TenantSettingsWebService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tenant-settings")
public class TenantSettingsPageController {

    private final TenantContext tenantContext;
    private final TenantSettingsWebService tenantSettingsWebService;

    public TenantSettingsPageController(TenantContext tenantContext,
                                        TenantSettingsWebService tenantSettingsWebService) {
        this.tenantContext = tenantContext;
        this.tenantSettingsWebService = tenantSettingsWebService;
    }

    @GetMapping("/inventory-valuation")
    public String inventoryValuationForm(Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        model.addAttribute(
                "tenantInventoryValuationForm",
                tenantSettingsWebService.getInventoryValuationForm(tenantId)
        );
        model.addAttribute("activeMenu", "items");

        return "tenant/tenant-inventory-valuation-form";
    }

    @PostMapping("/inventory-valuation")
    public String updateInventoryValuation(@Valid @ModelAttribute("tenantInventoryValuationForm") TenantInventoryValuationForm form,
                                           BindingResult bindingResult,
                                           Model model,
                                           RedirectAttributes redirectAttributes) {
        Long tenantId = tenantContext.getCurrentTenantId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "items");
            return "tenant/tenant-inventory-valuation-form";
        }

        tenantSettingsWebService.updateInventoryValuationMethod(tenantId, form);
        redirectAttributes.addFlashAttribute("successMessage", "Metodo di valorizzazione aggiornato con successo.");
        return "redirect:/tenant-settings/inventory-valuation";
    }
}