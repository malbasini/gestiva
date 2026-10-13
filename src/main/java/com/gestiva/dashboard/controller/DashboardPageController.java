package com.gestiva.dashboard.controller;

import com.gestiva.dashboard.web.DashboardWebService;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardPageController {

    private final TenantContext tenantContext;
    private final CompanyContext companyContext;
    private final DashboardWebService dashboardWebService;

    public DashboardPageController(
            TenantContext tenantContext,
            CompanyContext companyContext,
            DashboardWebService dashboardWebService) {

        this.tenantContext = tenantContext;
        this.companyContext = companyContext;
        this.dashboardWebService = dashboardWebService;
    }

    @GetMapping
    public String page(Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();

        Long companyId = companyContext.resolveCurrentCompanyId();

        // Espone la società attiva alla vista.
        model.addAttribute("activeCompanyId", companyId);

        // Manteniamo temporaneamente la dashboard
        // basata sul tenant.
        model.addAttribute(
                "dashboard",
                dashboardWebService.build(tenantId)
        );

        model.addAttribute("activeMenu", "dashboard");

        return "dashboard/dashboard";
    }
}