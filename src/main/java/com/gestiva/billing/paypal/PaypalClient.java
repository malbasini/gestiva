package com.gestiva.billing.paypal;

import java.math.BigDecimal;

public interface PaypalClient {

    PaypalCreateOrderResult createOrder(Long tenantId,
                                        String planCode,
                                        BigDecimal amount,
                                        String currencyCode,
                                        String returnUrl,
                                        String cancelUrl);

    String captureOrder(String providerOrderId);

    boolean verifyWebhook(String transmissionId,
                          String timestamp,
                          String webhookId,
                          String eventBody,
                          String certUrl,
                          String authAlgo,
                          String transmissionSig);
}