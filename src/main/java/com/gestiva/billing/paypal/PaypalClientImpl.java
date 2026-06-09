package com.gestiva.billing.paypal;

import com.gestiva.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
public class PaypalClientImpl implements PaypalClient {

    private final RestClient restClient;

    @Value("${paypal.base-url}")
    private String baseUrl;

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    public PaypalClientImpl(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    public PaypalCreateOrderResult createOrder(Long tenantId,
                                               String planCode,
                                               BigDecimal amount,
                                               String currencyCode,
                                               String returnUrl,
                                               String cancelUrl) {

        String accessToken = getAccessToken();

        Map<String, Object> body = Map.of(
                "intent", "CAPTURE",
                "purchase_units", new Object[]{
                        Map.of(
                                "reference_id", "tenant-" + tenantId + "-" + planCode,
                                "amount", Map.of(
                                        "currency_code", currencyCode,
                                        "value", amount.toPlainString()
                                ),
                                "description", "Gestiva " + planCode
                        )
                },
                "application_context", Map.of(
                        "return_url", returnUrl,
                        "cancel_url", cancelUrl,
                        "user_action", "PAY_NOW"
                )
        );

        PaypalCreateOrderResponse response = restClient.post()
                .uri(baseUrl + "/v2/checkout/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(PaypalCreateOrderResponse.class);

        if (response == null || response.getId() == null) {
            throw new BusinessException("PayPal non ha restituito un ordine valido.");
        }

        String approvalUrl = response.getLinks() == null ? null :
                response.getLinks().stream()
                        .filter(l -> "approve".equalsIgnoreCase(l.getRel()))
                        .map(PaypalCreateOrderResponse.Link::getHref)
                        .findFirst()
                        .orElse(null);

        PaypalCreateOrderResult result = new PaypalCreateOrderResult();
        result.setOrderId(response.getId());
        result.setApprovalUrl(approvalUrl);
        result.setRawPayload("status=" + response.getStatus());
        return result;
    }

    @Override
    public String captureOrder(String providerOrderId) {
        String accessToken = getAccessToken();

        PaypalCaptureOrderResponse response = restClient.post()
                .uri(baseUrl + "/v2/checkout/orders/{id}/capture", providerOrderId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(PaypalCaptureOrderResponse.class);

        if (response == null || response.getStatus() == null) {
            throw new BusinessException("Capture PayPal non valida.");
        }

        return response.getStatus();
    }

    @Override
    public boolean verifyWebhook(String transmissionId,
                                 String timestamp,
                                 String webhookId,
                                 String eventBody,
                                 String certUrl,
                                 String authAlgo,
                                 String transmissionSig) {
        String accessToken = getAccessToken();

        Map<String, Object> body = Map.of(
                "transmission_id", transmissionId,
                "transmission_time", timestamp,
                "cert_url", certUrl,
                "auth_algo", authAlgo,
                "transmission_sig", transmissionSig,
                "webhook_id", webhookId,
                "webhook_event", JsonUtils.readTree(eventBody)
        );

        Map<?, ?> response = restClient.post()
                .uri(baseUrl + "/v1/notifications/verify-webhook-signature")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        Object verificationStatus = response == null ? null : response.get("verification_status");
        return "SUCCESS".equals(String.valueOf(verificationStatus));
    }

    private String getAccessToken() {
        String basic = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        PaypalAccessTokenResponse tokenResponse = restClient.post()
                .uri(baseUrl + "/v1/oauth2/token")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=client_credentials")
                .retrieve()
                .body(PaypalAccessTokenResponse.class);

        if (tokenResponse == null || tokenResponse.getAccess_token() == null) {
            throw new BusinessException("Impossibile ottenere access token PayPal.");
        }

        return tokenResponse.getAccess_token();
    }
}
