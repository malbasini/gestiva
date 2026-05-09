package com.gestiva.accounting.v2.journal.web;

public class JournalEntryDetailLineView {

    private Integer lineNo;
    private String accountCode;
    private String accountName;
    private String description;
    private String formattedDebitAmount;
    private String formattedCreditAmount;

    public Integer getLineNo() {
        return lineNo;
    }

    public void setLineNo(Integer lineNo) {
        this.lineNo = lineNo;
    }    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }    public String getFormattedDebitAmount() {
        return formattedDebitAmount;
    }

    public void setFormattedDebitAmount(String formattedDebitAmount) {
        this.formattedDebitAmount = formattedDebitAmount;
    }    public String getFormattedCreditAmount() {
        return formattedCreditAmount;
    }

    public void setFormattedCreditAmount(String formattedCreditAmount) {
        this.formattedCreditAmount = formattedCreditAmount;
    }
}