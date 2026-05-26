package com.gestiva.accounting.v2.report.web;

import java.util.ArrayList;
import java.util.List;

public class FinancialPositionSectionView {

    private String accountType;
    private String accountTypeLabel;
    private String formattedSectionTotal;
    private List<FinancialPositionDetailRowView> details = new ArrayList<>();

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

    public String getFormattedSectionTotal() {
        return formattedSectionTotal;
    }

    public void setFormattedSectionTotal(String formattedSectionTotal) {
        this.formattedSectionTotal = formattedSectionTotal;
    }

    public List<FinancialPositionDetailRowView> getDetails() {
        return details;
    }

    public void setDetails(List<FinancialPositionDetailRowView> details) {
        this.details = details;
    }
}
