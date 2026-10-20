package com.gestiva.accounting.payment.controller;

import com.gestiva.accounting.payment.service.PaymentRegistrationService;
import com.gestiva.accounting.payment.web.PaymentRegistrationForm;
import com.gestiva.accounting.v2.account.web.AccountWebService;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.accounting.due.repository.PaymentDueRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.security.usercontext.TenantContext;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.gestiva.security.company.context.CompanyContext;

@Controller
@RequestMapping("/payment-dues")
public class PaymentRegistrationPageController {

    private final TenantContext tenantContext;
    private final CompanyContext companyContext;
    private final PaymentDueRepository paymentDueRepository;
    private final PaymentRegistrationService paymentRegistrationService;
    private final AccountWebService accountWebService;

    public PaymentRegistrationPageController(
            TenantContext tenantContext,
            CompanyContext companyContext,
            PaymentDueRepository paymentDueRepository,
            PaymentRegistrationService paymentRegistrationService,
            AccountWebService accountWebService) {

        this.tenantContext = tenantContext;
        this.companyContext = companyContext;
        this.paymentDueRepository = paymentDueRepository;
        this.paymentRegistrationService = paymentRegistrationService;
        this.accountWebService = accountWebService;
    }

    @GetMapping("/{id}/register-payment")
    public String form(@PathVariable Long id, Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();
        Long companyId = companyContext.resolveCurrentCompanyId();
        if (companyId == null) {
            throw new BusinessException("Nessuna società attiva selezionata.");
        }
        var due = paymentDueRepository
                .findByTenantIdAndCompanyIdAndId(tenantId, companyId, id)
                .orElseThrow(() -> new BusinessException(
                        "Scadenza non trovata nella società attiva."
                ));
        String grossAmount = PdfFormatUtils.formatMoney(due.getGrossAmount());
        String paidAmount = PdfFormatUtils.formatMoney(due.getPaidAmount());
        String openAmount = PdfFormatUtils.formatMoney(due.getOpenAmount());
        PaymentRegistrationForm form = new PaymentRegistrationForm();
        form.setPaymentDueId(due.getId());
        form.setPaymentDate(java.time.LocalDate.now());
        model.addAttribute("paymentRegistrationForm", form);
        this.enrichFormModel(model,due,grossAmount,paidAmount,openAmount,tenantId);
        return "accounting/payment-due/payment-registration-form";
    }

    @PostMapping("/{id}/register-payment")
    public String submit(@PathVariable Long id,
                         @Valid @ModelAttribute("paymentRegistrationForm") PaymentRegistrationForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Long companyId = companyContext.resolveCurrentCompanyId();
        if (companyId == null) {
            throw new BusinessException("Nessuna società attiva selezionata.");
        }
        var due = paymentDueRepository
                .findByTenantIdAndCompanyIdAndId(tenantId, companyId, id)
                .orElseThrow(() -> new BusinessException(
                        "Scadenza non trovata nella società attiva."
                ));
        String grossAmount = PdfFormatUtils.formatMoney(due.getGrossAmount());
        String paidAmount = PdfFormatUtils.formatMoney(due.getPaidAmount());
        String openAmount = PdfFormatUtils.formatMoney(due.getOpenAmount());
        model.addAttribute("financialAccountOptions", accountWebService.findFinancialAccountOptions(tenantId));
        if (bindingResult.hasErrors()) {
            this.enrichFormModel(model,due,grossAmount,paidAmount,openAmount,tenantId);
            model.addAttribute("errorMessage", bindingResult.getAllErrors().getFirst().getDefaultMessage());
            return "accounting/payment-due/payment-registration-form";
        }

        try {
            form.setPaymentDueId(id);
            paymentRegistrationService.register(tenantId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Operazione registrata con successo.");
            return "redirect:/payments";
        } catch (BusinessException ex) {
            this.enrichFormModel(model,due,grossAmount,paidAmount,openAmount,tenantId);
            model.addAttribute("errorMessage", ex.getMessage());
            return "accounting/payment-due/payment-registration-form";
        }
    }

    private void enrichFormModel(Model model, Object due, String grossAmount, String paidAmount, String openAmount, Long tenantId) {
        model.addAttribute("due", due);
        model.addAttribute("grossAmount", grossAmount);
        model.addAttribute("paidAmount", paidAmount);
        model.addAttribute("openAmount", openAmount);
        model.addAttribute("financialAccountOptions", accountWebService.findFinancialAccountOptions(tenantId));
        model.addAttribute("activeMenu", "accounting");
    }
}