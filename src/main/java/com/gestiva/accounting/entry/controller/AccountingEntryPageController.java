package com.gestiva.accounting.entry.controller;

import com.gestiva.accounting.entry.web.AccountingEntryWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/accounting-entries")
public class AccountingEntryPageController {

    private final AccountingEntryWebService accountingEntryWebService;
    private final TenantContext tenantContext;

    public AccountingEntryPageController(AccountingEntryWebService accountingEntryWebService,
                                         TenantContext tenantContext) {
        this.accountingEntryWebService = accountingEntryWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public String list(Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("entries", accountingEntryWebService.findAll(tenantId));
        model.addAttribute("activeMenu", "accountingEntries");
        return "accounting/entry/accounting-entry-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("entry", accountingEntryWebService.getDetail(tenantId, id));
        model.addAttribute("activeMenu", "accountingEntries");
        return "accounting/entry/accounting-entry-detail";
    }
}