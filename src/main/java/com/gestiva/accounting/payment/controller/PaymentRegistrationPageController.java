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

@Controller
@RequestMapping("/payment-dues")
public class PaymentRegistrationPageController {

    private final TenantContext tenantContext;
    private final PaymentDueRepository paymentDueRepository;
    private final PaymentRegistrationService paymentRegistrationService;
    private final AccountWebService accountWebService;

    public PaymentRegistrationPageController(TenantContext tenantContext,
                                             PaymentDueRepository paymentDueRepository,
                                             PaymentRegistrationService paymentRegistrationService,
                                             AccountWebService accountWebService) {
        this.tenantContext = tenantContext;
        this.paymentDueRepository = paymentDueRepository;
        this.paymentRegistrationService = paymentRegistrationService;
        this.accountWebService = accountWebService;
    }

    @GetMapping("/{id}/register-payment")
    public String form(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        var due = paymentDueRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new BusinessException("Scadenza non trovata."));

        String grossAmount = PdfFormatUtils.formatMoney(due.getGrossAmount());
        String paidAmount = PdfFormatUtils.formatMoney(due.getPaidAmount());
        String openAmount = PdfFormatUtils.formatMoney(due.getOpenAmount());
        PaymentRegistrationForm form = new PaymentRegistrationForm();
        form.setPaymentDueId(due.getId());
        form.setPaymentDate(java.time.LocalDate.now());

        model.addAttribute("paymentRegistrationForm", form);
        model.addAttribute("financialAccountOptions", accountWebService.findFinancialAccountOptions(tenantId));
        model.addAttribute("due", due);
        model.addAttribute("grossAmount", grossAmount);
        model.addAttribute("paidAmount", paidAmount);
        model.addAttribute("openAmount", openAmount);
        model.addAttribute("activeMenu", "accounting");

        return "accounting/payment-due/payment-registration-form";
    }

    @PostMapping("/{id}/register-payment")
    public String submit(@PathVariable Long id,
                         @Valid @ModelAttribute("paymentRegistrationForm") PaymentRegistrationForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = tenantContext.getCurrentTenantId();

        var due = paymentDueRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new BusinessException("Scadenza non trovata."));

        String grossAmount = PdfFormatUtils.formatMoney(due.getGrossAmount());
        String paidAmount = PdfFormatUtils.formatMoney(due.getPaidAmount());
        String openAmount = PdfFormatUtils.formatMoney(due.getOpenAmount());
        model.addAttribute("financialAccountOptions", accountWebService.findFinancialAccountOptions(tenantId));
        if (bindingResult.hasErrors()) {
            model.addAttribute("due", due);
            model.addAttribute("grossAmount", grossAmount);
            model.addAttribute("financialAccountOptions", accountWebService.findFinancialAccountOptions(tenantId));
            model.addAttribute("paidAmount", paidAmount);
            model.addAttribute("openAmount", openAmount);
            model.addAttribute("activeMenu", "accounting");
            model.addAttribute("errorMessage", bindingResult.getAllErrors().getFirst().getDefaultMessage());
            return "accounting/payment-due/payment-registration-form";
        }

        try {
            form.setPaymentDueId(id);
            paymentRegistrationService.register(tenantId, form, form.getFinancialAccountId(), form.getFinancialAccountId());
            redirectAttributes.addFlashAttribute("successMessage", "Operazione registrata con successo.");
            return "redirect:/payment-dues";
        } catch (BusinessException ex) {
            model.addAttribute("due", due);
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("financialAccountOptions", accountWebService.findFinancialAccountOptions(tenantId));
            model.addAttribute("grossAmount", grossAmount);
            model.addAttribute("paidAmount", paidAmount);
            model.addAttribute("openAmount", openAmount);
            model.addAttribute("activeMenu", "accounting");
            return "accounting/payment-due/payment-registration-form";
        }
    }
}