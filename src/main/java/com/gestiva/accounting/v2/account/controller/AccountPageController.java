package com.gestiva.accounting.v2.account.controller;

import com.gestiva.accounting.v2.account.web.AccountWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/v2/accounts")
public class AccountPageController {

    private final AccountWebService accountWebService;
    private final TenantContext tenantContext;

    public AccountPageController(AccountWebService accountWebService,
                                 TenantContext tenantContext) {
        this.accountWebService = accountWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public String list(Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();


        model.addAttribute("accounts", accountWebService.findAll(tenantId));
        model.addAttribute("activeMenu", "v2Accounts");
        return "accounting/v2/account/account-list";
    }

    @GetMapping("/{id}")
    public String ledger(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();


        model.addAttribute("ledger", accountWebService.getLedger(tenantId, id));
        model.addAttribute("activeMenu", "v2Accounts");
        return "accounting/v2/account/account-ledger";
    }
}