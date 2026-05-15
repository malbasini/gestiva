package com.gestiva.accounting.entry.controller;

import com.gestiva.accounting.entry.repository.AccountingEntryRepository;
import com.gestiva.accounting.entry.service.AccountingEntryService;
import com.gestiva.accounting.entry.web.AccountingEntryManualForm;
import com.gestiva.accounting.entry.web.AccountingEntryWebService;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.usercontext.TenantContext;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/accounting-entries")
public class AccountingEntryPageController {

    private final AccountingEntryWebService accountingEntryWebService;
    private final AccountingEntryService accountingEntryService;
    private final TenantContext tenantContext;
    private final AccountingEntryRepository accountEntryRepository;



    public AccountingEntryPageController(AccountingEntryWebService accountingEntryWebService,
                                         AccountingEntryService accountingEntryService,
                                         TenantContext tenantContext,
                                         AccountingEntryRepository accountEntryRepository) {

        this.accountingEntryWebService = accountingEntryWebService;
        this.accountingEntryService = accountingEntryService;
        this.tenantContext = tenantContext;
        this.accountEntryRepository = accountEntryRepository;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("entry", accountingEntryWebService.getDetail(tenantId, id));
        model.addAttribute("activeMenu", "accountingEntries");
        return "accounting/entry/accounting-entry-detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        AccountingEntryManualForm form = new AccountingEntryManualForm();
        form.setEntryDate(LocalDate.now());
        form.setMovementType("MANUAL_EXPENSE");
        form.setCurrencyCode("EUR");
        model.addAttribute("manualForm", form);
        model.addAttribute("activeMenu", "accountingEntries");
        return "accounting/entry/accounting-entry-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("manualForm") AccountingEntryManualForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = tenantContext.getCurrentTenantId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "accountingEntries");
            return "accounting/entry/accounting-entry-form";
        }
        try {
            Long entryId = accountingEntryService.registerManualEntry(tenantId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Registrazione di prima nota salvata con successo.");
            return "redirect:/accounting-entries/" + entryId;
        }
        catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "accounting/entry/accounting-entry-form";
        }
    }
    @GetMapping
    public String list(@RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "10") int size,
                       @RequestParam(name = "q", required = false) String q,
                       @RequestParam(name = "causalCode", required = false) String causalCode,
                       @RequestParam(name = "dateFrom", required = false)
                       @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                       java.time.LocalDate dateFrom,
                       @RequestParam(name = "dateTo", required = false)
                       @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                       java.time.LocalDate dateTo,
                       Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();
        var resultPage = accountingEntryWebService.findPage(tenantId, page, size, causalCode, q, dateFrom, dateTo);
        model.addAttribute("entries", resultPage);
        model.addAttribute("causalOptions", accountEntryRepository.findDistinctCausalCodesByTenantId(tenantId));
        model.addAttribute("page", resultPage);
        model.addAttribute("q", q);
        model.addAttribute("causalCode", causalCode);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("size", size);
        model.addAttribute("activeMenu", "accountingEntries");
        return "accounting/entry/accounting-entry-list";
    }
}