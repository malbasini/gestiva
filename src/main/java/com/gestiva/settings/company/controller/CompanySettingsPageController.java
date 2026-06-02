package com.gestiva.settings.company.controller;

import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.settings.company.service.CompanySettingsService;
import com.gestiva.settings.company.web.CompanySettingsForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings/company")
public class CompanySettingsPageController {

    private final TenantContext tenantContext;
    private final CompanySettingsService companySettingsService;

    public CompanySettingsPageController(TenantContext tenantContext,
                                         CompanySettingsService companySettingsService) {
        this.tenantContext = tenantContext;
        this.companySettingsService = companySettingsService;
    }

    @GetMapping
    public String page(Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        if (!model.containsAttribute("companySettingsForm")) {
            model.addAttribute("companySettingsForm", companySettingsService.getOrCreateForm(tenantId));
        }

        model.addAttribute("activeMenu", "settings");
        return "settings/company/company-settings";
    }

    @PostMapping
    public String save(@Valid CompanySettingsForm companySettingsForm,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        Long tenantId = tenantContext.getCurrentTenantId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "settings");
            return "settings/company/company-settings";
        }

        companySettingsService.save(tenantId, companySettingsForm);
        redirectAttributes.addFlashAttribute("successMessage", "Impostazioni azienda salvate con successo.");
        return "redirect:/settings/company";
    }
}
