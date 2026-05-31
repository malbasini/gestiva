package com.gestiva.accounting.v2.report.web;

import java.util.ArrayList;
import java.util.List;

public class IncomeStatementSectionView {

    private String sectionCode;
    private String sectionLabel;
    private String formattedTotal;
    private List<IncomeStatementRowView> rows = new ArrayList<>();

    public String getSectionCode() {
        return sectionCode;
    }

    public void setSectionCode(String sectionCode) {
        this.sectionCode = sectionCode;
    }

    public String getSectionLabel() {
        return sectionLabel;
    }

    public void setSectionLabel(String sectionLabel) {
        this.sectionLabel = sectionLabel;
    }

    public String getFormattedTotal() {
        return formattedTotal;
    }

    public void setFormattedTotal(String formattedTotal) {
        this.formattedTotal = formattedTotal;
    }

    public List<IncomeStatementRowView> getRows() {
        return rows;
    }

    public void setRows(List<IncomeStatementRowView> rows) {
        this.rows = rows;
    }
}
