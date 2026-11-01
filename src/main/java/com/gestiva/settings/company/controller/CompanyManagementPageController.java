package com.gestiva.settings.company.controller;

import com.gestiva.platform.company.service.CompanyManagementService;
import com.gestiva.platform.company.entity.Company;
import com.gestiva.settings.company.web.CompanyCreateForm;
import com.gestiva.settings.company.web.CompanyEditForm;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import jakarta.validation.Valid;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings/companies")
@PreAuthorize("hasRole('ADMIN')")
public class CompanyManagementPageController {

    private final TenantContext tenantContext;
    private final CompanyManagementService companyManagementService;

    public CompanyManagementPageController(
            TenantContext tenantContext,
            CompanyManagementService companyManagementService) {

        this.tenantContext = tenantContext;
        this.companyManagementService = companyManagementService;
    }

    @GetMapping
    public String list(Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();

        model.addAttribute(
                "companies",
                companyManagementService.listCompanies(tenantId)
        );

        model.addAttribute("activeMenu", "settings");

        return "settings/company/company-list";
    }

    @GetMapping("/new")
    public String newCompany(Model model) {

        model.addAttribute("companyCreateForm", new CompanyCreateForm());
        model.addAttribute("activeMenu", "settings");

        return "settings/company/company-create";
    }

    @PostMapping("/new")
    public String create(
            @Valid @ModelAttribute("companyCreateForm")
            CompanyCreateForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "settings");
            return "settings/company/company-create";
        }

        Long tenantId = tenantContext.getCurrentTenantId();

        try {
            companyManagementService.createCompany(
                    tenantId,
                    form.getCode(),
                    form.getLegalName()
            );
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("activeMenu", "settings");
            return "settings/company/company-create";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Società creata e inizializzata correttamente."
        );

        return "redirect:/settings/companies";
    }


    @GetMapping("/{companyId}/edit")
    public String editCompany(
            @PathVariable Long companyId,
            Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();

        Company company = companyManagementService.getCompany(
                tenantId, companyId
        );

        CompanyEditForm form = new CompanyEditForm();
        form.setLegalName(company.getLegalName());
        form.setActive(company.isActive());

        model.addAttribute("company", company);
        model.addAttribute("companyEditForm", form);
        model.addAttribute("activeMenu", "settings");

        return "settings/company/company-edit";
    }

    @PostMapping("/{companyId}/edit")
    public String updateCompany(
            @PathVariable Long companyId,
            @Valid @ModelAttribute("companyEditForm")
            CompanyEditForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long tenantId = tenantContext.getCurrentTenantId();

        Company company = companyManagementService.getCompany(
                tenantId, companyId
        );

        if (bindingResult.hasErrors()) {
            model.addAttribute("company", company);
            model.addAttribute("activeMenu", "settings");
            return "settings/company/company-edit";
        }

        try {
            companyManagementService.updateCompany(
                    tenantId,
                    companyId,
                    form.getLegalName(),
                    form.isActive()
            );
        } catch (IllegalArgumentException ex) {
            model.addAttribute("company", company);
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("activeMenu", "settings");
            return "settings/company/company-edit";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Società aggiornata correttamente."
        );

        return "redirect:/settings/companies";
    }

}
