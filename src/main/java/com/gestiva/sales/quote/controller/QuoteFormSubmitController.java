package com.gestiva.sales.quote.controller;

import com.gestiva.crm.contact.web.CustomerLookupWebService;
import com.gestiva.sales.quote.service.QuoteService;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.sales.quote.web.QuoteForm;
import com.gestiva.sales.quote.web.QuoteManageWebService;
import com.gestiva.inventory.item.web.ItemWebService;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/quotes")
public class QuoteFormSubmitController {

    private final QuoteManageWebService quoteManageWebService;
    private final CustomerLookupWebService customerLookupWebService;
    private final TenantContext tenantContext;
    private final ItemWebService itemWebService;
    private final QuoteService quoteService;

    public QuoteFormSubmitController(QuoteManageWebService quoteManageWebService,
                                     CustomerLookupWebService customerLookupWebService,
                                     TenantContext tenantContext,
                                     ItemWebService itemWebService,
                                     QuoteService quoteService
                                     ) {
        this.quoteManageWebService = quoteManageWebService;
        this.customerLookupWebService = customerLookupWebService;
        this.tenantContext = tenantContext;
        this.itemWebService = itemWebService;
        this.quoteService = quoteService;
    }

    @PostMapping(params = "addLine")
    public String addLine(@ModelAttribute("quoteForm") QuoteForm quoteForm,
                          @RequestParam(required = false) Long tenantId,
                          @RequestParam(required = false) Long quoteId,
                          @RequestParam(required = false) String formMode,
                          Model model) {
        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();
        quoteManageWebService.addLine(quoteForm);
        return getString(quoteForm, quoteId, formMode, model, resolvedTenantId);
    }

    @NonNull
    private String getString(@ModelAttribute("quoteForm") QuoteForm quoteForm, @RequestParam(required = false) Long quoteId, @RequestParam(required = false) String formMode, Model model, Long resolvedTenantId) {
        model.addAttribute("customerOptions", customerLookupWebService.findActiveOptions(resolvedTenantId));
        model.addAttribute("itemOptions", itemWebService.findOptions(resolvedTenantId));
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("quoteId", quoteId);
        model.addAttribute("formMode", formMode == null ? "create" : formMode);
        model.addAttribute("activeMenu", "quotes");
        model.addAttribute("totalsPreview", quoteManageWebService.calculatePreviewTotals(quoteForm));
        return "quote/quote-form";
    }

    @PostMapping(params = "removeLine")
    public String removeLine(@ModelAttribute("quoteForm") QuoteForm quoteForm,
                             @RequestParam int removeLine,
                             @RequestParam(required = false) Long tenantId,
                             @RequestParam(required = false) Long quoteId,
                             @RequestParam(required = false) String formMode,
                             Model model) {
        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();
        quoteManageWebService.removeLine(quoteForm, removeLine);
        model.addAttribute("totalsPreview", quoteManageWebService.calculatePreviewTotals(quoteForm));
        return getString(quoteForm, quoteId, formMode, model, resolvedTenantId);
    }

    @PostMapping(value = "/{id}", params = "addLine")
    public String addLineEdit(@PathVariable Long id,
                              @ModelAttribute("quoteForm") QuoteForm quoteForm,
                              @RequestParam(required = false) Long tenantId,
                              @RequestParam(required = false) String formMode,
                              Model model) {
        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();
        quoteManageWebService.addLine(quoteForm);
        model.addAttribute("totalsPreview", quoteManageWebService.calculatePreviewTotals(quoteForm));
        return getString(id, formMode, model, resolvedTenantId);
    }

    @PostMapping(value = "/{id}", params = "removeLine")
    public String removeLineEdit(@PathVariable Long id,
                                 @ModelAttribute("quoteForm") QuoteForm quoteForm,
                                 @RequestParam int removeLine,
                                 @RequestParam(required = false) Long tenantId,
                                 @RequestParam(required = false) String formMode,
                                 Model model) {
        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();
        quoteManageWebService.removeLine(quoteForm, removeLine);
        model.addAttribute("totalsPreview", quoteManageWebService.calculatePreviewTotals(quoteForm));
        return getString(id, formMode, model, resolvedTenantId);
    }

    @NonNull
    private String getString(@PathVariable Long id, @RequestParam(required = false) String formMode, Model model, Long resolvedTenantId) {
        model.addAttribute("customerOptions", customerLookupWebService.findActiveOptions(resolvedTenantId));
        model.addAttribute("itemOptions", itemWebService.findOptions(resolvedTenantId));
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("quoteId", id);
        model.addAttribute("formMode", formMode == null ? "edit" : formMode);
        model.addAttribute("activeMenu", "quotes");
        return "quote/quote-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("quoteForm") QuoteForm quoteForm,
                         BindingResult bindingResult,
                         @RequestParam(required = false) Long tenantId,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        try {
            Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();
            if (bindingResult.hasErrors()) {
                  model.addAttribute("customerOptions", customerLookupWebService.findActiveOptions(tenantId));
                  model.addAttribute("totalsPreview", quoteManageWebService.calculatePreviewTotals(quoteForm));
                  model.addAttribute("itemOptions", itemWebService.findOptions(tenantId));
                  model.addAttribute("formMode", "create");
                  model.addAttribute("tenantId", tenantId);
                  model.addAttribute("activeMenu", "quotes");
                  model.addAttribute("totalsPreview", quoteManageWebService.calculatePreviewTotals(quoteForm));
                  model.addAttribute("errorMessage", bindingResult.getAllErrors().getFirst().getDefaultMessage());
                  return "quote/quote-form";
            }
            quoteService.validateLinesForm(quoteForm.getLines());
            Long quoteId = quoteManageWebService.create(resolvedTenantId, quoteForm);
            redirectAttributes.addFlashAttribute("successMessage", "Preventivo creato con successo.");
            return "redirect:/quotes/" + quoteId + "?tenantId=" + resolvedTenantId;
        }
        catch (Exception ex) {
            model.addAttribute("customerOptions", customerLookupWebService.findActiveOptions(tenantId));
            model.addAttribute("totalsPreview", quoteManageWebService.calculatePreviewTotals(quoteForm));
            model.addAttribute("itemOptions", itemWebService.findOptions(tenantId));
            model.addAttribute("formMode", "create");
            model.addAttribute("tenantId", tenantId);
            model.addAttribute("activeMenu", "quotes");
            model.addAttribute("errorMessage", ex.getMessage());
            return "quote/quote-form";
        }

    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("quoteForm") QuoteForm quoteForm,
                         BindingResult bindingResult,
                         @RequestParam(required = false) Long tenantId,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        try {
            Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();
            if (bindingResult.hasErrors()) {
                model.addAttribute("customerOptions", customerLookupWebService.findActiveOptions(resolvedTenantId));
                model.addAttribute("totalsPreview", quoteManageWebService.calculatePreviewTotals(quoteForm));
                model.addAttribute("itemOptions", itemWebService.findOptions(tenantId));
                model.addAttribute("formMode", "edit");
                model.addAttribute("quoteId", id);
                model.addAttribute("tenantId", resolvedTenantId);
                model.addAttribute("activeMenu", "quotes");
                model.addAttribute("errorMessage", bindingResult.getAllErrors().getFirst().getDefaultMessage());
                return "quote/quote-form";
            }
            quoteService.validateLinesForm(quoteForm.getLines());
            Long quoteId = quoteManageWebService.update(resolvedTenantId, id, quoteForm);
            redirectAttributes.addFlashAttribute("successMessage", "Preventivo aggiornato con successo.");
            return "redirect:/quotes/" + quoteId + "?tenantId=" + resolvedTenantId;
        } catch (Exception e) {
             model.addAttribute("customerOptions", customerLookupWebService.findActiveOptions(tenantId));
             model.addAttribute("totalsPreview", quoteManageWebService.calculatePreviewTotals(quoteForm));
             model.addAttribute("itemOptions", itemWebService.findOptions(tenantId));
             model.addAttribute("formMode", "edit");
             model.addAttribute("quoteId", id);
             model.addAttribute("tenantId", tenantId);
             model.addAttribute("activeMenu", "quotes");
             model.addAttribute("errorMessage", e.getMessage());
             return "quote/quote-form";
        }
    }

    @PostMapping(params = "recalculate")
    public String recalculate(@ModelAttribute("quoteForm") QuoteForm quoteForm,
                              @RequestParam(required = false) Long tenantId,
                              @RequestParam(required = false) Long quoteId,
                              @RequestParam(required = false) String formMode,
                              Model model) {
        getTenantId(quoteId, quoteForm, tenantId, model);
        model.addAttribute("formMode", formMode == null ? "create" : formMode);
        model.addAttribute("activeMenu", "quotes");
        model.addAttribute("itemOptions", itemWebService.findOptions(tenantId));
        return "quote/quote-form";
    }
    @PostMapping(value = "/{id}", params = "recalculate")
    public String recalculateEdit(@PathVariable Long id,
                                  @ModelAttribute("quoteForm") QuoteForm quoteForm,
                                  @RequestParam(required = false) Long tenantId,
                                  @RequestParam(required = false) String formMode,
                                  Model model) {
        getTenantId(id, quoteForm, tenantId, model);
        model.addAttribute("formMode", formMode == null ? "edit" : formMode);
        model.addAttribute("activeMenu", "quotes");
        model.addAttribute("itemOptions", itemWebService.findOptions(tenantId));
        return "quote/quote-form";
    }

    private void getTenantId(@PathVariable Long id, @ModelAttribute("quoteForm") QuoteForm quoteForm, @RequestParam(required = false) Long tenantId, Model model) {
        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();
        model.addAttribute("customerOptions", customerLookupWebService.findActiveOptions(resolvedTenantId));
        model.addAttribute("totalsPreview", quoteManageWebService.calculatePreviewTotals(quoteForm));
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("quoteId", id);
    }
}