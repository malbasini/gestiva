package com.gestiva.accounting.v2.report.web;

public class TrialBalanceRowView {

    private Long accountId;
    private String accountCode;
    private String accountName;
    private String accountType;
    private String nature;
    private String formattedTotalDebit;
    private String formattedTotalCredit;
    private String formattedBalance;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }

    public String getFormattedTotalDebit() {
        return formattedTotalDebit;
    }

    public void setFormattedTotalDebit(String formattedTotalDebit) {
        this.formattedTotalDebit = formattedTotalDebit;
    }

    public String getFormattedTotalCredit() {
        return formattedTotalCredit;
    }

    public void setFormattedTotalCredit(String formattedTotalCredit) {
        this.formattedTotalCredit = formattedTotalCredit;
    }

    public String getFormattedBalance() {
        return formattedBalance;
    }

    public void setFormattedBalance(String formattedBalance) {
        this.formattedBalance = formattedBalance;
    }
}