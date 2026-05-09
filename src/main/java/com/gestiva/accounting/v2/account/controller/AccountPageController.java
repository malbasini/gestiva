package com.gestiva.accounting.v2.account.controller;

import com.gestiva.accounting.v2.account.service.AccountChartBootstrapService;
import com.gestiva.accounting.v2.account.web.AccountWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/v2/accounts")
public class AccountPageController {

    private final AccountWebService accountWebService;
    private final AccountChartBootstrapService accountChartBootstrapService;
    private final TenantContext tenantContext;

    public AccountPageController(AccountWebService accountWebService,
                                 AccountChartBootstrapService accountChartBootstrapService,
                                 TenantContext tenantContext) {
        this.accountWebService = accountWebService;
        this.accountChartBootstrapService = accountChartBootstrapService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public String list(Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        accountChartBootstrapService.initializeDefaultChartOfAccounts(tenantId);

        model.addAttribute("accounts", accountWebService.findAll(tenantId));
        model.addAttribute("activeMenu", "v2Accounts");
        return "accounting/v2/account/account-list";
    }

    @GetMapping("/{id}")
    public String ledger(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        accountChartBootstrapService.initializeDefaultChartOfAccounts(tenantId);

        model.addAttribute("ledger", accountWebService.getLedger(tenantId, id));
        model.addAttribute("activeMenu", "v2Accounts");
        return "accounting/v2/account/account-ledger";
    }
}