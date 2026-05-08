package com.gestiva.accounting.due.web;

public class PaymentDueTransactionView {

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
}