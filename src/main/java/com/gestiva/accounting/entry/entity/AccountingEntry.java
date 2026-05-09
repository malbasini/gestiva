package com.gestiva.accounting.entry.entity;

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
        name = "accounting_entry",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_accounting_entry_tenant_number", columnNames = {"tenant_id", "entry_number"})
        },
        indexes = {
                @Index(name = "idx_accounting_entry_tenant_date", columnList = "tenant_id,entry_date"),
                @Index(name = "idx_accounting_entry_tenant_causal", columnList = "tenant_id,causal_code"),
                @Index(name = "idx_accounting_entry_tenant_ref", columnList = "tenant_id,reference_type,reference_id")
        }
)
public class AccountingEntry extends TenantAwareEntity {

    @Column(name = "entry_number", nullable = false, length = 50)
    private String entryNumber;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "causal_code", nullable = false, length = 40)
    private String causalCode; // CUSTOMER_RECEIPT, SUPPLIER_PAYMENT, MANUAL_INCOME, MANUAL_EXPENSE

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "reference_type", length = 40)
    private String referenceType; // PAYMENT_DUE, CUSTOMER_INVOICE, SUPPLIER_INVOICE

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "notes", length = 1000)
    private String notes;

    public String getEntryNumber() {
        return entryNumber;
    }

    public void setEntryNumber(String entryNumber) {
        this.entryNumber = entryNumber;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public String getCausalCode() {
        return causalCode;
    }

    public void setCausalCode(String causalCode) {
        this.causalCode = causalCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}