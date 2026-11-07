package com.gestiva.creditnote.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Access(AccessType.FIELD)
@Table(
    name = "credit_note_financial_position",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_credit_note_position_company_note",
        columnNames = {
            "tenant_id", "company_id", "credit_note_id"
        }
    )
)
public class CreditNoteFinancialPosition extends TenantAwareEntity {

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "credit_note_id", nullable = false)
    private Long creditNoteId;

    @Column(name = "position_type", nullable = false, length = 20)
    private String positionType;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(
        name = "original_amount",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal originalAmount;

    @Column(
        name = "compensated_amount",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal compensatedAmount;

    @Column(
        name = "refunded_amount",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal refundedAmount;

    @Column(
        name = "available_amount",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal availableAmount;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getCreditNoteId() {
        return creditNoteId;
    }

    public void setCreditNoteId(Long creditNoteId) {
        this.creditNoteId = creditNoteId;
    }

    public String getPositionType() {
        return positionType;
    }

    public void setPositionType(String positionType) {
        this.positionType = positionType;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getCompensatedAmount() {
        return compensatedAmount;
    }

    public void setCompensatedAmount(BigDecimal compensatedAmount) {
        this.compensatedAmount = compensatedAmount;
    }

    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }

    public void setRefundedAmount(BigDecimal refundedAmount) {
        this.refundedAmount = refundedAmount;
    }

    public BigDecimal getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(BigDecimal availableAmount) {
        this.availableAmount = availableAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
