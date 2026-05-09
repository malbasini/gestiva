package com.gestiva.dashboard.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.dashboard.web.DashboardWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DashboardPageController {

    private final DashboardWebService dashboardWebService;
    private final TenantContext tenantContext;

    public DashboardPageController(DashboardWebService dashboardWebService,
                                   TenantContext tenantContext) {
        this.dashboardWebService = dashboardWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) Long tenantId,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();
            model.addAttribute("tenantId", resolvedTenantId);
            model.addAttribute("dashboard", dashboardWebService.build(resolvedTenantId));
            model.addAttribute("activeMenu", "dashboard");
            return "dashboard/dashboard";
        }
        catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore durante il caricamento del dashboard.");
            return "redirect:/login";
        }
    }
}