package com.gestiva.accounting.v2.report.web;

import java.util.ArrayList;
import java.util.List;

public class FinancialPositionSummaryView {

    private String formattedDateFrom;
    private String formattedDateTo;
    private List<FinancialPositionSummaryRowView> rows = new ArrayList<>();

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

    public List<FinancialPositionSummaryRowView> getRows() {
        return rows;
    }

    public void setRows(List<FinancialPositionSummaryRowView> rows) {
        this.rows = rows;
    }
}
