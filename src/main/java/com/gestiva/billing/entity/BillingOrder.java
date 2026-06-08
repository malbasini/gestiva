package com.gestiva.billing.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "billing_order",
        indexes = {
                @Index(name = "idx_billing_order_tenant", columnList = "tenant_id"),
                @Index(name = "idx_billing_order_provider_order", columnList = "provider_order_id"),
                @Index(name = "idx_billing_order_status", columnList = "status")
        })
public class BillingOrder extends TenantAwareEntity {

    @Column(name = "plan_code", nullable = false, length = 50)
    private String planCode;

    @Column(name = "provider", nullable = false, length = 30)
    private String provider; // PAYPAL

    @Column(name = "provider_order_id", length = 120)
    private String providerOrderId;

    @Column(name = "status", nullable = false, length = 30)
    private String status; // PENDING, APPROVED, COMPLETED, CANCELLED, FAILED

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode;

    @Column(name = "approval_url", length = 1000)
    private String approvalUrl;

    @Column(name = "provider_payer_id", length = 120)
    private String providerPayerId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Lob
    @Column(name = "raw_payload")
    private String rawPayload;

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderOrderId() {
        return providerOrderId;
    }

    public void setProviderOrderId(String providerOrderId) {
        this.providerOrderId = providerOrderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getApprovalUrl() {
        return approvalUrl;
    }

    public void setApprovalUrl(String approvalUrl) {
        this.approvalUrl = approvalUrl;
    }

    public String getProviderPayerId() {
        return providerPayerId;
    }

    public void setProviderPayerId(String providerPayerId) {
        this.providerPayerId = providerPayerId;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }
}
