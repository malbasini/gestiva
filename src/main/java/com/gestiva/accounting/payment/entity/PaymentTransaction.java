package com.gestiva.accounting.payment.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payment_transaction")
public class PaymentTransaction extends TenantAwareEntity {

    @Column(name = "direction", nullable = false, length = 10)
    private String direction; // IN / OUT

    @Column(name = "counterparty_type", nullable = false, length = 20)
    private String counterpartyType; // CUSTOMER / SUPPLIER

    @Column(name = "counterparty_id", nullable = false)
    private Long counterpartyId;

    @Column(name = "payment_due_id", nullable = false)
    private Long paymentDueId;

    @Column(name = "document_type", length = 50)
    private String documentType;

    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(name = "reference", length = 100)
    private String reference;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "journal_entry_id")
    private Long journalEntryId;

    public Long getJournalEntryId() {
        return journalEntryId;
    }

    public void setJournalEntryId(Long journalEntryId) {
        this.journalEntryId = journalEntryId;
    }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getCounterpartyType() { return counterpartyType; }
    public void setCounterpartyType(String counterpartyType) { this.counterpartyType = counterpartyType; }

    public Long getCounterpartyId() { return counterpartyId; }
    public void setCounterpartyId(Long counterpartyId) { this.counterpartyId = counterpartyId; }

    public Long getPaymentDueId() { return paymentDueId; }
    public void setPaymentDueId(Long paymentDueId) { this.paymentDueId = paymentDueId; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
