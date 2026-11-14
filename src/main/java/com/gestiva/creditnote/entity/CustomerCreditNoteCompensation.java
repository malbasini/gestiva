package com.gestiva.creditnote.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Access(AccessType.FIELD)
@Table(
    name = "customer_credit_note_compensation",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_ccn_compensation_operation",
            columnNames = {"tenant_id", "company_id", "operation_key"}
        )
    },
    indexes = {
        @Index(
            name = "idx_ccn_compensation_position",
            columnList = "tenant_id,company_id,credit_note_financial_position_id"
        ),
        @Index(
            name = "idx_ccn_compensation_due",
            columnList = "tenant_id,company_id,payment_due_id"
        ),
        @Index(
            name = "idx_ccn_compensation_date",
            columnList = "tenant_id,company_id,compensation_date"
        )
    }
)
public class CustomerCreditNoteCompensation extends TenantAwareEntity {

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "credit_note_financial_position_id", nullable = false)
    private Long creditNoteFinancialPositionId;

    @Column(name = "payment_due_id", nullable = false)
    private Long paymentDueId;

    @Column(name = "operation_key", nullable = false, length = 100)
    private String operationKey;

    @Column(name = "compensation_date", nullable = false)
    private LocalDate compensationDate;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "notes", length = 1000)
    private String notes;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getCreditNoteFinancialPositionId() {
        return creditNoteFinancialPositionId;
    }

    public void setCreditNoteFinancialPositionId(
            Long creditNoteFinancialPositionId) {
        this.creditNoteFinancialPositionId = creditNoteFinancialPositionId;
    }

    public Long getPaymentDueId() {
        return paymentDueId;
    }

    public void setPaymentDueId(Long paymentDueId) {
        this.paymentDueId = paymentDueId;
    }

    public String getOperationKey() {
        return operationKey;
    }

    public void setOperationKey(String operationKey) {
        this.operationKey = operationKey;
    }

    public LocalDate getCompensationDate() {
        return compensationDate;
    }

    public void setCompensationDate(LocalDate compensationDate) {
        this.compensationDate = compensationDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
