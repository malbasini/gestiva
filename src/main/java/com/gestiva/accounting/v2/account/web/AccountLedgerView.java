package com.gestiva.accounting.v2.account.web;

import java.util.ArrayList;
import java.util.List;

public class AccountLedgerView {

    private Long accountId;
    private String code;
    private String name;
    private String accountType;
    private String nature;
    private boolean active;
    private boolean systemAccount;
    private String formattedTotalDebit;
    private String formattedTotalCredit;
    private String formattedBalance;
    private List<AccountLedgerLineView> lines = new ArrayList<>();

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }    public boolean isSystemAccount() {
        return systemAccount;
    }

    public void setSystemAccount(boolean systemAccount) {
        this.systemAccount = systemAccount;
    }    public String getFormattedTotalDebit() {
        return formattedTotalDebit;
    }

    public void setFormattedTotalDebit(String formattedTotalDebit) {
        this.formattedTotalDebit = formattedTotalDebit;
    }    public String getFormattedTotalCredit() {
        return formattedTotalCredit;
    }

    public void setFormattedTotalCredit(String formattedTotalCredit) {
        this.formattedTotalCredit = formattedTotalCredit;
    }    public String getFormattedBalance() {
        return formattedBalance;
    }

    public void setFormattedBalance(String formattedBalance) {
        this.formattedBalance = formattedBalance;
    }    public List<AccountLedgerLineView> getLines() {
        return lines;
    }

    public void setLines(List<AccountLedgerLineView> lines) {
        this.lines = lines;
    }
}
