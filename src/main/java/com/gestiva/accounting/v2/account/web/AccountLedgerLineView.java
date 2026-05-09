package com.gestiva.accounting.v2.account.web;

public class AccountLedgerLineView {

    private Long journalEntryId;
    private String journalEntryNumber;
    private String formattedEntryDate;
    private String causalCode;
    private String description;
    private String formattedDebitAmount;
    private String formattedCreditAmount;

    public Long getJournalEntryId() {
        return journalEntryId;
    }

    public void setJournalEntryId(Long journalEntryId) {
        this.journalEntryId = journalEntryId;
    }

    public String getJournalEntryNumber() {
        return journalEntryNumber;
    }

    public void setJournalEntryNumber(String journalEntryNumber) {
        this.journalEntryNumber = journalEntryNumber;
    }

    public String getFormattedEntryDate() {
        return formattedEntryDate;
    }

    public void setFormattedEntryDate(String formattedEntryDate) {
        this.formattedEntryDate = formattedEntryDate;
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

    public String getFormattedDebitAmount() {
        return formattedDebitAmount;
    }

    public void setFormattedDebitAmount(String formattedDebitAmount) {
        this.formattedDebitAmount = formattedDebitAmount;
    }

    public String getFormattedCreditAmount() {
        return formattedCreditAmount;
    }

    public void setFormattedCreditAmount(String formattedCreditAmount) {
        this.formattedCreditAmount = formattedCreditAmount;
    }
}