package com.gestiva.billing.web;

import com.gestiva.billing.service.BillingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/billing/paypal")
public class PaypalReturnPageController {

    private final BillingService billingService;

    public PaypalReturnPageController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/success")
    public String success(@RequestParam(name = "token", required = false) String token,
                          Model model) {

        boolean activated = false;
        String message;

        try {
            if (token != null && !token.isBlank()) {
                billingService.captureAndActivate(token, "RETURN_SUCCESS");
                activated = true;
                message = "Pagamento completato con successo. Il tenant è stato attivato correttamente.";
            } else {
                message = "Pagamento approvato, ma non è stato ricevuto il riferimento ordine PayPal.";
            }
        } catch (Exception ex) {
            message = "Il pagamento risulta approvato, ma si è verificato un problema durante l'attivazione automatica. Verifica il webhook PayPal o controlla lo stato del tenant.";
        }

        model.addAttribute("activated", activated);
        model.addAttribute("message", message);
        model.addAttribute("activeMenu", "dashboard");

        return "billing/paypal-success";
    }

    @GetMapping("/cancel")
    public String cancel(Model model) {
        model.addAttribute("message", "Il pagamento PayPal è stato annullato. Nessun addebito è stato completato.");
        return "billing/paypal-cancel";
    }

}