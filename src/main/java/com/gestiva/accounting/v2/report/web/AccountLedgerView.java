package com.gestiva.accounting.v2.report.web;

import java.util.ArrayList;
import java.util.List;

public class AccountLedgerView {

    private Long accountId;
    private String accountCode;
    private String accountName;
    private String accountTypeLabel;
    private String natureLabel;
    private String formattedDateFrom;
    private String formattedDateTo;
    private String formattedFinalBalance;
    private List<AccountLedgerRowView> rows = new ArrayList<>();

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

    public String getAccountTypeLabel() {
        return accountTypeLabel;
    }

    public void setAccountTypeLabel(String accountTypeLabel) {
        this.accountTypeLabel = accountTypeLabel;
    }

    public String getNatureLabel() {
        return natureLabel;
    }

    public void setNatureLabel(String natureLabel) {
        this.natureLabel = natureLabel;
    }

    public String getFormattedDateFrom() {
        return formattedDateFrom;
    }

    public void setFormattedDateFrom(String formattedDateFrom) {
        this.formattedDateFrom = formattedDateFrom;
    }

    public String getFormattedDateTo() {
        return formattedDateTo;
    }

    public void setFormattedDateTo(String formattedDateTo) {
        this.formattedDateTo = formattedDateTo;
    }

    public String getFormattedFinalBalance() {
        return formattedFinalBalance;
    }

    public void setFormattedFinalBalance(String formattedFinalBalance) {
        this.formattedFinalBalance = formattedFinalBalance;
    }

    public List<AccountLedgerRowView> getRows() {
        return rows;
    }

    public void setRows(List<AccountLedgerRowView> rows) {
        this.rows = rows;
    }
}
