package com.gestiva.accounting.dashboard.controller;

import com.gestiva.accounting.dashboard.web.AccountingDashboardWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/accounting-dashboard")
public class AccountingDashboardPageController {

    private final AccountingDashboardWebService accountingDashboardWebService;
    private final TenantContext tenantContext;

    public AccountingDashboardPageController(AccountingDashboardWebService accountingDashboardWebService,
                                             TenantContext tenantContext) {
        this.accountingDashboardWebService = accountingDashboardWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public String view(Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("dashboard", accountingDashboardWebService.build(tenantId));
        model.addAttribute("activeMenu", "accountingDashboard");
        return "accounting/dashboard/accounting-dashboard";
    }
}