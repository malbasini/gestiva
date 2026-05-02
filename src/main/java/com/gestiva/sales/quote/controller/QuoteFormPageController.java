package com.gestiva.sales.quote.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.crm.contact.web.CustomerLookupWebService;
import com.gestiva.sales.order.repository.SalesOrderRepository;
import com.gestiva.sales.quote.repository.QuoteRepository;
import com.gestiva.sales.quote.web.QuoteManageWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/quotes")
public class QuoteFormPageController {

    private final QuoteManageWebService quoteManageWebService;
    private final TenantContext tenantContext;
    private final CustomerLookupWebService customerLookupWebService;

    public QuoteFormPageController(QuoteManageWebService quoteManageWebService,
                                   TenantContext tenantContext,
                                   CustomerLookupWebService customerLookupWebService) {

        this.quoteManageWebService = quoteManageWebService;
        this.tenantContext = tenantContext;
        this.customerLookupWebService = customerLookupWebService;
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(required = false) Long tenantId, Model model) {
        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();
        model.addAttribute("customerOptions", customerLookupWebService.findActiveOptions(resolvedTenantId));
        model.addAttribute("quoteForm", quoteManageWebService.buildCreateForm());
        var form = quoteManageWebService.buildCreateForm();
        model.addAttribute("quoteForm", form);
        model.addAttribute("customerOptions", customerLookupWebService.findActiveOptions(resolvedTenantId));
        model.addAttribute("totalsPreview", quoteManageWebService.calculatePreviewTotals(form));
        model.addAttribute("formMode", "create");
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("activeMenu", "quotes");
        model.addAttribute("formMode", "create");
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("activeMenu", "quotes");

        return "quote/quote-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,

                           @RequestParam(required = false) Long tenantId,

                           Model model) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();
        model.addAttribute("quoteForm", quoteManageWebService.buildEditForm(resolvedTenantId, id));
        model.addAttribute("customerOptions", customerLookupWebService.findActiveOptions(resolvedTenantId));
        var form = quoteManageWebService.buildEditForm(resolvedTenantId, id);
        model.addAttribute("quoteForm", form);
        model.addAttribute("customerOptions", customerLookupWebService.findActiveOptions(resolvedTenantId));
        model.addAttribute("totalsPreview", quoteManageWebService.calculatePreviewTotals(form));
        model.addAttribute("quoteId", id);
        model.addAttribute("formMode", "edit");
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("activeMenu", "quotes");
        model.addAttribute("quoteId", id);
        model.addAttribute("formMode", "edit");
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("activeMenu", "quotes");
        return "quote/quote-form";

    }

}