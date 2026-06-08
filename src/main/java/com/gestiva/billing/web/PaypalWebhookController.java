package com.gestiva.billing.web;

import com.gestiva.billing.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/billing/paypal")
public class PaypalWebhookController {

    private final BillingService billingService;

    public PaypalWebhookController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String payload,
                                        @RequestHeader(value = "PayPal-Transmission-Id", required = false) String transmissionId,
                                        @RequestHeader(value = "PayPal-Transmission-Time", required = false) String transmissionTime,
                                        @RequestHeader(value = "PayPal-Transmission-Sig", required = false) String transmissionSig,
                                        @RequestHeader(value = "PayPal-Cert-Url", required = false) String certUrl,
                                        @RequestHeader(value = "PayPal-Auth-Algo", required = false) String authAlgo) {

        // TODO: verifica firma webhook con paypalClient.verifyWebhook(...)

        // TODO: parse payload JSON e individua providerOrderId + eventType
        String providerOrderId = extractProviderOrderId(payload);
        String eventType = extractEventType(payload);

        if ("CHECKOUT.ORDER.APPROVED".equalsIgnoreCase(eventType)
                || "PAYMENT.CAPTURE.COMPLETED".equalsIgnoreCase(eventType)) {
            billingService.markOrderCompleted(providerOrderId, payload);
        } else if ("CHECKOUT.ORDER.CANCELLED".equalsIgnoreCase(eventType)) {
            billingService.markOrderCancelled(providerOrderId, payload);
        }

        return ResponseEntity.ok().build();
    }

    private String extractProviderOrderId(String payload) {
        // TODO: sostituire con parsing JSON vero
        return "";
    }

    private String extractEventType(String payload) {
        // TODO: sostituire con parsing JSON vero
        return "";
    }
}
