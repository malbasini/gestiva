package com.gestiva.accounting.payment.controller;

import com.gestiva.accounting.payment.web.PaymentTransactionListWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/payments")
public class PaymentTransactionPageController {

    private final TenantContext tenantContext;
    private final PaymentTransactionListWebService paymentTransactionListWebService;

    public PaymentTransactionPageController(TenantContext tenantContext,
                                            PaymentTransactionListWebService paymentTransactionListWebService) {
        this.tenantContext = tenantContext;
        this.paymentTransactionListWebService = paymentTransactionListWebService;
    }

    @GetMapping
    public String list(@RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "10") int size,
                       @RequestParam(name = "direction", required = false) String direction,
                       Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        var resultPage = paymentTransactionListWebService.findPage(tenantId, page, size, direction);

        model.addAttribute("page", resultPage);
        model.addAttribute("direction", direction);
        model.addAttribute("size", size);
        model.addAttribute("activeMenu", "accounting");

        return "accounting/payment/payment-transaction-list";
    }
}