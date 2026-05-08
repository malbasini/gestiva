package com.gestiva.accounting.due.controller;

import com.gestiva.accounting.due.web.PaymentDueWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/payment-dues")
public class PaymentDuePageController {

    private final PaymentDueWebService paymentDueWebService;
    private final TenantContext tenantContext;

    public PaymentDuePageController(PaymentDueWebService paymentDueWebService,
                                    TenantContext tenantContext) {
        this.paymentDueWebService = paymentDueWebService;
        this.tenantContext = tenantContext;
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
}