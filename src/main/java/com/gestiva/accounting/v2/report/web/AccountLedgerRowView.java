package com.gestiva.accounting.v2.report.web;

public class AccountLedgerRowView {

    private String formattedEntryDate;
    private String entryNumber;
    private String causalCodeLabel;
    private String description;
    private String formattedDebit;
    private String formattedCredit;
    private String formattedProgressiveBalance;
    private Long journalEntryId;

    public String getFormattedEntryDate() {
        return formattedEntryDate;
    }

    public void setFormattedEntryDate(String formattedEntryDate) {
        this.formattedEntryDate = formattedEntryDate;
    }

    public String getEntryNumber() {
        return entryNumber;
    }

    public void setEntryNumber(String entryNumber) {
        this.entryNumber = entryNumber;
    }

    public String getCausalCodeLabel() {
        return causalCodeLabel;
    }

    public void setCausalCodeLabel(String causalCodeLabel) {
        this.causalCodeLabel = causalCodeLabel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFormattedDebit() {
        return formattedDebit;
    }

    public void setFormattedDebit(String formattedDebit) {
        this.formattedDebit = formattedDebit;
    }

    public String getFormattedCredit() {
        return formattedCredit;
    }

    public void setFormattedCredit(String formattedCredit) {
        this.formattedCredit = formattedCredit;
    }

    public String getFormattedProgressiveBalance() {
        return formattedProgressiveBalance;
    }

    public void setFormattedProgressiveBalance(String formattedProgressiveBalance) {
        this.formattedProgressiveBalance = formattedProgressiveBalance;
    }

    public Long getJournalEntryId() {
        return journalEntryId;
    }

    public void setJournalEntryId(Long journalEntryId) {
        this.journalEntryId = journalEntryId;
    }
}
