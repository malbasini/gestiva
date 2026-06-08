package com.gestiva.billing.web;

public class CreatePaypalOrderResponse {

    private Long billingOrderId;
    private String providerOrderId;
    private String approvalUrl;

    public Long getBillingOrderId() { return billingOrderId; }
    public void setBillingOrderId(Long billingOrderId) { this.billingOrderId = billingOrderId; }

    public String getProviderOrderId() { return providerOrderId; }
    public void setProviderOrderId(String providerOrderId) { this.providerOrderId = providerOrderId; }

    public String getApprovalUrl() { return approvalUrl; }
    public void setApprovalUrl(String approvalUrl) { this.approvalUrl = approvalUrl; }
}
