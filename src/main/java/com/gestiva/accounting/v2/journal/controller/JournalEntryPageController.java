package com.gestiva.accounting.v2.journal.controller;

import com.gestiva.accounting.v2.account.service.AccountChartBootstrapService;
import com.gestiva.accounting.v2.account.web.AccountWebService;
import com.gestiva.accounting.v2.journal.service.JournalEntryService;
import com.gestiva.accounting.v2.journal.web.JournalEntryForm;
import com.gestiva.accounting.v2.journal.web.JournalEntryLineForm;
import com.gestiva.accounting.v2.journal.web.JournalEntryWebService;
import com.gestiva.security.usercontext.TenantContext;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/v2/journal-entries")
public class JournalEntryPageController {

    private final JournalEntryService journalEntryService;
    private final JournalEntryWebService journalEntryWebService;
    private final AccountWebService accountWebService;
    private final AccountChartBootstrapService accountChartBootstrapService;
    private final TenantContext tenantContext;

    public JournalEntryPageController(JournalEntryService journalEntryService,
                                      JournalEntryWebService journalEntryWebService,
                                      AccountWebService accountWebService,
                                      AccountChartBootstrapService accountChartBootstrapService,
                                      TenantContext tenantContext) {
        this.journalEntryService = journalEntryService;
        this.journalEntryWebService = journalEntryWebService;
        this.accountWebService = accountWebService;
        this.accountChartBootstrapService = accountChartBootstrapService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public String list(Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        accountChartBootstrapService.initializeDefaultChartOfAccounts(tenantId);

        model.addAttribute("entries", journalEntryWebService.findAll(tenantId));
        model.addAttribute("activeMenu", "v2JournalEntries");
        return "accounting/v2/journal/journal-entry-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        accountChartBootstrapService.initializeDefaultChartOfAccounts(tenantId);

        model.addAttribute("entry", journalEntryWebService.getDetail(tenantId, id));
        model.addAttribute("activeMenu", "v2JournalEntries");
        return "accounting/v2/journal/journal-entry-detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        accountChartBootstrapService.initializeDefaultChartOfAccounts(tenantId);

        JournalEntryForm form = new JournalEntryForm();
        form.setEntryDate(LocalDate.now());
        form.setCausalCode("MANUAL_JOURNAL");
        form.setCurrencyCode("EUR");
        form.getLines().add(defaultLine());
        form.getLines().add(defaultLine());

        model.addAttribute("journalEntryForm", form);
        model.addAttribute("accountOptions", accountWebService.findLeafOptions(tenantId));
        model.addAttribute("activeMenu", "v2JournalEntries");
        return "accounting/v2/journal/journal-entry-form";
    }

    @PostMapping(params = "addLine")
    public String addLine(@ModelAttribute("journalEntryForm") JournalEntryForm form, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        accountChartBootstrapService.initializeDefaultChartOfAccounts(tenantId);

        form.getLines().add(defaultLine());

        model.addAttribute("accountOptions", accountWebService.findLeafOptions(tenantId));
        model.addAttribute("activeMenu", "v2JournalEntries");
        return "accounting/v2/journal/journal-entry-form";
    }

    @PostMapping(params = "removeLine")
    public String removeLine(@ModelAttribute("journalEntryForm") JournalEntryForm form,
                             @RequestParam("removeLine") int index,
                             Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        accountChartBootstrapService.initializeDefaultChartOfAccounts(tenantId);

        if (form.getLines() != null && index >= 0 && index < form.getLines().size()) {
            form.getLines().remove(index);
        }

        if (form.getLines() == null || form.getLines().size() < 2) {
            while (form.getLines().size() < 2) {
                form.getLines().add(defaultLine());
            }
        }

        model.addAttribute("accountOptions", accountWebService.findLeafOptions(tenantId));
        model.addAttribute("activeMenu", "v2JournalEntries");
        return "accounting/v2/journal/journal-entry-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("journalEntryForm") JournalEntryForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = tenantContext.getCurrentTenantId();
        accountChartBootstrapService.initializeDefaultChartOfAccounts(tenantId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("accountOptions", accountWebService.findLeafOptions(tenantId));
            model.addAttribute("activeMenu", "v2JournalEntries");
            return "accounting/v2/journal/journal-entry-form";
        }

        Long id = journalEntryService.createManualEntry(tenantId, form);
        redirectAttributes.addFlashAttribute("successMessage", "Scrittura contabile V2 salvata con successo.");
        return "redirect:/v2/journal-entries/" + id;
    }

    private JournalEntryLineForm defaultLine() {
        JournalEntryLineForm line = new JournalEntryLineForm();
        line.setDebitAmount(java.math.BigDecimal.ZERO);
        line.setCreditAmount(java.math.BigDecimal.ZERO);
        return line;
    }
}