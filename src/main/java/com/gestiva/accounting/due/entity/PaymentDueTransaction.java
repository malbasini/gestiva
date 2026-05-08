package com.gestiva.accounting.due.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "payment_due_transaction",
        indexes = {
                @Index(name = "idx_due_tx_tenant_due", columnList = "tenant_id,payment_due_id"),
                @Index(name = "idx_due_tx_tenant_date", columnList = "tenant_id,transaction_date")
        }
)
public class PaymentDueTransaction extends TenantAwareEntity {

    @Column(name = "payment_due_id", nullable = false)
    private Long paymentDueId;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "direction", nullable = false, length = 20)
    private String direction; // RECEIPT, PAYMENT

    @Column(name = "notes", length = 1000)
    private String notes;

    public Long getPaymentDueId() {
        return paymentDueId;
    }

    public void setPaymentDueId(Long paymentDueId) {
        this.paymentDueId = paymentDueId;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
