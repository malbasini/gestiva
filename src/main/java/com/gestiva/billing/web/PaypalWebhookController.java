package com.gestiva.billing.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestiva.billing.paypal.PaypalClient;
import com.gestiva.billing.service.BillingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/billing/paypal")
public class PaypalWebhookController {

    private final PaypalClient paypalClient;
    private final BillingService billingService;
    private final ObjectMapper objectMapper;

    @Value("${paypal.webhook-id}")
    private String webhookId;

    public PaypalWebhookController(PaypalClient paypalClient,
                                   BillingService billingService,
                                   ObjectMapper objectMapper) {
        this.paypalClient = paypalClient;
        this.billingService = billingService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String payload,
                                        @RequestHeader(value = "PayPal-Transmission-Id", required = false) String transmissionId,
                                        @RequestHeader(value = "PayPal-Transmission-Time", required = false) String transmissionTime,
                                        @RequestHeader(value = "PayPal-Transmission-Sig", required = false) String transmissionSig,
                                        @RequestHeader(value = "PayPal-Cert-Url", required = false) String certUrl,
                                        @RequestHeader(value = "PayPal-Auth-Algo", required = false) String authAlgo) {

        boolean verified = paypalClient.verifyWebhook(
                transmissionId,
                transmissionTime,
                webhookId,
                payload,
                certUrl,
                authAlgo,
                transmissionSig
        );

        if (!verified) {
            return ResponseEntity.badRequest().build();
        }

        String eventType = extractEventType(payload);
        String providerOrderId = extractProviderOrderId(payload);

        if (providerOrderId == null || providerOrderId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        if ("PAYMENT.CAPTURE.COMPLETED".equalsIgnoreCase(eventType)) {
            billingService.markOrderCompleted(providerOrderId, payload);
        } else if ("CHECKOUT.ORDER.APPROVED".equalsIgnoreCase(eventType)) {
            // opzionale: puoi introdurre billingService.markOrderApproved(...)
            // per ora non attivo il tenant qui
            billingService.captureAndActivate(providerOrderId, payload);
        } else if ("CHECKOUT.ORDER.DECLINED".equalsIgnoreCase(eventType)
                || "PAYMENT.CAPTURE.DENIED".equalsIgnoreCase(eventType)) {
            billingService.markOrderCancelled(providerOrderId, payload);
        }

        return ResponseEntity.ok().build();
    }

    private String extractProviderOrderId(String payload) {
        JsonNode root = readJson(payload);
        String eventType = text(root, "/event_type");

        if ("PAYMENT.CAPTURE.COMPLETED".equalsIgnoreCase(eventType)
                || "PAYMENT.CAPTURE.DENIED".equalsIgnoreCase(eventType)) {

            String orderId = text(root, "/resource/supplementary_data/related_ids/order_id");
            if (orderId != null && !orderId.isBlank()) {
                return orderId;
            }
        }

        if ("CHECKOUT.ORDER.APPROVED".equalsIgnoreCase(eventType)
                || "CHECKOUT.ORDER.DECLINED".equalsIgnoreCase(eventType)) {

            String orderId = text(root, "/resource/id");
            if (orderId != null && !orderId.isBlank()) {
                return orderId;
            }
        }

        String fallback = text(root, "/resource/id");
        return (fallback == null || fallback.isBlank()) ? null : fallback;
    }

    private String extractEventType(String payload) {
        JsonNode root = readJson(payload);
        return text(root, "/event_type");
    }

    private JsonNode readJson(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Payload PayPal non valido.", ex);
        }
    }

    private String text(JsonNode root, String pointer) {
        JsonNode node = root.at(pointer);
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value == null || value.isBlank() ? null : value;
    }
}