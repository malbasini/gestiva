package com.gestiva.accounting.dashboard.web;

import java.util.ArrayList;
import java.util.List;

public class AccountingDashboardView {

    private String formattedOpenReceivables;
    private String formattedOpenPayables;
    private long overdueCount;
    private String formattedOverdueAmount;
    private String formattedMonthReceipts;
    private String formattedMonthPayments;
    private List<AccountingDashboardRecentEntryView> recentEntries = new ArrayList<>();

    public String getFormattedOpenReceivables() {
        return formattedOpenReceivables;
    }

    public void setFormattedOpenReceivables(String formattedOpenReceivables) {
        this.formattedOpenReceivables = formattedOpenReceivables;
    }

    public String getFormattedOpenPayables() {
        return formattedOpenPayables;
    }

    public void setFormattedOpenPayables(String formattedOpenPayables) {
        this.formattedOpenPayables = formattedOpenPayables;
    }

    public long getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(long overdueCount) {
        this.overdueCount = overdueCount;
    }

    public String getFormattedOverdueAmount() {
        return formattedOverdueAmount;
    }

    public void setFormattedOverdueAmount(String formattedOverdueAmount) {
        this.formattedOverdueAmount = formattedOverdueAmount;
    }

    public String getFormattedMonthReceipts() {
        return formattedMonthReceipts;
    }

    public void setFormattedMonthReceipts(String formattedMonthReceipts) {
        this.formattedMonthReceipts = formattedMonthReceipts;
    }

    public String getFormattedMonthPayments() {
        return formattedMonthPayments;
    }

    public void setFormattedMonthPayments(String formattedMonthPayments) {
        this.formattedMonthPayments = formattedMonthPayments;
    }

    public List<AccountingDashboardRecentEntryView> getRecentEntries() {
        return recentEntries;
    }

    public void setRecentEntries(List<AccountingDashboardRecentEntryView> recentEntries) {
        this.recentEntries = recentEntries;
    }
}