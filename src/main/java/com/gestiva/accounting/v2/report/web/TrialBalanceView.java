package com.gestiva.accounting.v2.report.web;

import java.util.ArrayList;
import java.util.List;

public class TrialBalanceView {

    private String formattedDateFrom;
    private String formattedDateTo;
    private String formattedGrandTotalDebit;
    private String formattedGrandTotalCredit;
    private List<TrialBalanceRowView> rows = new ArrayList<>();

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

    public String getFormattedGrandTotalDebit() {
        return formattedGrandTotalDebit;
    }

    public void setFormattedGrandTotalDebit(String formattedGrandTotalDebit) {
        this.formattedGrandTotalDebit = formattedGrandTotalDebit;
    }

    public String getFormattedGrandTotalCredit() {
        return formattedGrandTotalCredit;
    }

    public void setFormattedGrandTotalCredit(String formattedGrandTotalCredit) {
        this.formattedGrandTotalCredit = formattedGrandTotalCredit;
    }

    public List<TrialBalanceRowView> getRows() {
        return rows;
    }

    public void setRows(List<TrialBalanceRowView> rows) {
        this.rows = rows;
    }
}
