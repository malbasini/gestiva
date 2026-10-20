package com.gestiva.accounting.due.web;

public class PaymentDueTransactionView {

    private Long id;
    private Long journalEntryId;
    private String formattedTransactionDate;
    private String direction;
    private String formattedAmount;
    private String notes;

    public String getFormattedTransactionDate() {
        return formattedTransactionDate;
    }

    public void setFormattedTransactionDate(String formattedTransactionDate) {
        this.formattedTransactionDate = formattedTransactionDate;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getFormattedAmount() {
        return formattedAmount;
    }

    public void setFormattedAmount(String formattedAmount) {
        this.formattedAmount = formattedAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJournalEntryId() {
        return journalEntryId;
    }

    public void setJournalEntryId(Long journalEntryId) {
        this.journalEntryId = journalEntryId;
    }
}