package com.gestiva.security.company.controller;

import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.company.service.CompanyAccessService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/company")
public class CompanySelectionController {

    private final CompanyContext companyContext;
    private final CompanyAccessService companyAccessService;

    public CompanySelectionController(
            CompanyContext companyContext,
            CompanyAccessService companyAccessService) {

        this.companyContext = companyContext;
        this.companyAccessService = companyAccessService;
    }

    @GetMapping("/select")
    public String showSelection(Model model) {

        model.addAttribute(
                "companies",
                companyAccessService.findAccessibleCompanies()
        );

        model.addAttribute(
                "activeCompanyId",
                companyContext.resolveCurrentCompanyId()
        );

        model.addAttribute("activeMenu", "company");

        return "company/select";
    }

    @PostMapping("/select")
    public String selectCompany(
            @RequestParam("companyId") Long companyId,
            RedirectAttributes redirectAttributes) {

        companyContext.selectCompany(companyId);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Società selezionata correttamente."
        );

        return "redirect:/dashboard";
    }
}