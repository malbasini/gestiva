package com.gestiva.accounting.v2.report.web;

public class FinancialPositionSummaryRowView {

    private String accountType;
    private String accountTypeLabel;
    private String formattedAmount;

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountTypeLabel() {
        return accountTypeLabel;
    }

    public void setAccountTypeLabel(String accountTypeLabel) {
        this.accountTypeLabel = accountTypeLabel;
    }

    public String getFormattedAmount() {
        return formattedAmount;
    }

    public void setFormattedAmount(String formattedAmount) {
        this.formattedAmount = formattedAmount;
    }
}