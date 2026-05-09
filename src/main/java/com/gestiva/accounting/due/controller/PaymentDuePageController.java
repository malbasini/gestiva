package com.gestiva.accounting.due.controller;

import com.gestiva.accounting.due.service.PaymentDueRegistrationService;
import com.gestiva.accounting.due.web.PaymentDueRegistrationForm;
import com.gestiva.accounting.due.web.PaymentDueWebService;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.usercontext.TenantContext;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/payment-dues")
public class PaymentDuePageController {

    private final PaymentDueWebService paymentDueWebService;
    private final TenantContext tenantContext;
    private final PaymentDueRegistrationService paymentDueRegistrationService;

    public PaymentDuePageController(PaymentDueWebService paymentDueWebService,
                                    TenantContext tenantContext,
                                    PaymentDueRegistrationService paymentDueRegistrationService) {

        this.paymentDueWebService = paymentDueWebService;
        this.tenantContext = tenantContext;
        this.paymentDueRegistrationService = paymentDueRegistrationService;
    }

    @GetMapping
    public String list(@RequestParam(name = "direction", required = false) String direction,
                       @RequestParam(name = "openOnly", required = false, defaultValue = "true") boolean openOnly,
                       Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        model.addAttribute("dues", paymentDueWebService.findAll(tenantId, direction, openOnly));
        model.addAttribute("selectedDirection", direction);
        model.addAttribute("openOnly", openOnly);
        model.addAttribute("activeMenu", "paymentDues");

        return "accounting/due/payment-due-list";
    }
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("due", paymentDueWebService.getDetail(tenantId, id));
        PaymentDueRegistrationForm form = new PaymentDueRegistrationForm();
        form.setTransactionDate(java.time.LocalDate.now());
        model.addAttribute("registrationForm", form);
        model.addAttribute("activeMenu", "paymentDues");
        return "accounting/due/payment-due-detail";

    }

    @PostMapping("/{id}/register")
    public String register(@PathVariable Long id,
                           @Valid @ModelAttribute("registrationForm") PaymentDueRegistrationForm form,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        Long tenantId = tenantContext.getCurrentTenantId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("due", paymentDueWebService.getDetail(tenantId, id));
            model.addAttribute("activeMenu", "paymentDues");
            return "accounting/due/payment-due-detail";
        }
        try {
            paymentDueRegistrationService.registerMovement(tenantId, id, form);
            redirectAttributes.addFlashAttribute("successMessage", "Registrazione eseguita con successo.");
        }
        catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/payment-dues/" + id;

    }
}