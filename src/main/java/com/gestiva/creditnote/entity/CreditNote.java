package com.gestiva.creditnote.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Access(AccessType.FIELD)
@Table(name = "credit_note"
,
    uniqueConstraints = @UniqueConstraint(
        name = "uk_credit_note_company_type_number",
        columnNames = {
            "tenant_id", "company_id",
            "credit_note_type", "credit_note_number"
        }
    ))
public class CreditNote extends TenantAwareEntity {

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "credit_note_type", nullable = false, length = 20)
    private String creditNoteType;

    @Column(name = "credit_note_number", nullable = false, length = 50)
    private String creditNoteNumber;

    @Column(name = "credit_note_date", nullable = false)
    private LocalDate creditNoteDate;

    @Column(name = "original_invoice_id", nullable = false)
    private Long originalInvoiceId;

    @Column(name = "party_id", nullable = false)
    private Long partyId;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "subtotal_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotalAmount;

    @Column(name = "tax_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCreditNoteType() {
        return creditNoteType;
    }

    public void setCreditNoteType(String creditNoteType) {
        this.creditNoteType = creditNoteType;
    }

    public String getCreditNoteNumber() {
        return creditNoteNumber;
    }

    public void setCreditNoteNumber(String creditNoteNumber) {
        this.creditNoteNumber = creditNoteNumber;
    }

    public LocalDate getCreditNoteDate() {
        return creditNoteDate;
    }

    public void setCreditNoteDate(LocalDate creditNoteDate) {
        this.creditNoteDate = creditNoteDate;
    }

    public Long getOriginalInvoiceId() {
        return originalInvoiceId;
    }

    public void setOriginalInvoiceId(Long originalInvoiceId) {
        this.originalInvoiceId = originalInvoiceId;
    }

    public Long getPartyId() {
        return partyId;
    }

    public void setPartyId(Long partyId) {
        this.partyId = partyId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(BigDecimal subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
