package com.gestiva.dashboard.controller;

import com.gestiva.dashboard.web.DashboardWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardPageController {

    private final DashboardWebService dashboardWebService;
    private final TenantContext tenantContext;

    public DashboardPageController(DashboardWebService dashboardWebService,
                                   TenantContext tenantContext) {
        this.dashboardWebService = dashboardWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(@RequestParam(required = false) Long tenantId, Model model) {
        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("dashboard", dashboardWebService.build(resolvedTenantId));
        model.addAttribute("activeMenu", "dashboard");
        return "dashboard/dashboard";
    }
}