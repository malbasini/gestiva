package com.gestiva.billing.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/billing/paypal")
public class PaypalReturnPageController {

    @GetMapping("/success")
    public String success() {
        return "billing/paypal-success";
    }

    @GetMapping("/cancel")
    public String cancel() {
        return "billing/paypal-cancel";
    }
}