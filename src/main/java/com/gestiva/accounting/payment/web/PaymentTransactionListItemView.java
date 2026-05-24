package com.gestiva.accounting.payment.web;

public class PaymentTransactionListItemView {

    private Long id;
    private Long paymentDueId;
    private Long journalEntryId;
    private String formattedPaymentDate;
    private String directionLabel;
    private String counterpartyType;
    private String partyLabel;
    private String documentLabel;
    private String formattedAmount;
    private String paymentMethod;
    private String reference;
    private String dueStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPaymentDueId() {
        return paymentDueId;
    }

    public void setPaymentDueId(Long paymentDueId) {
        this.paymentDueId = paymentDueId;
    }

    public Long getJournalEntryId() {
        return journalEntryId;
    }

    public void setJournalEntryId(Long journalEntryId) {
        this.journalEntryId = journalEntryId;
    }

    public String getFormattedPaymentDate() {
        return formattedPaymentDate;
    }

    public void setFormattedPaymentDate(String formattedPaymentDate) {
        this.formattedPaymentDate = formattedPaymentDate;
    }

    public String getDirectionLabel() {
        return directionLabel;
    }

    public void setDirectionLabel(String directionLabel) {
        this.directionLabel = directionLabel;
    }

    public String getCounterpartyType() {
        return counterpartyType;
    }

    public void setCounterpartyType(String counterpartyType) {
        this.counterpartyType = counterpartyType;
    }

    public String getPartyLabel() {
        return partyLabel;
    }

    public void setPartyLabel(String partyLabel) {
        this.partyLabel = partyLabel;
    }

    public String getDocumentLabel() {
        return documentLabel;
    }

    public void setDocumentLabel(String documentLabel) {
        this.documentLabel = documentLabel;
    }

    public String getFormattedAmount() {
        return formattedAmount;
    }

    public void setFormattedAmount(String formattedAmount) {
        this.formattedAmount = formattedAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDueStatus() {
        return dueStatus;
    }

    public void setDueStatus(String dueStatus) {
        this.dueStatus = dueStatus;
    }
}
