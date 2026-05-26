package com.gestiva.accounting.v2.report.web;

import java.util.ArrayList;
import java.util.List;

public class FinancialPositionDetailView {

    private String formattedDateFrom;
    private String formattedDateTo;
    private List<FinancialPositionSectionView> sections = new ArrayList<>();

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

    public List<FinancialPositionSectionView> getSections() {
        return sections;
    }

    public void setSections(List<FinancialPositionSectionView> sections) {
        this.sections = sections;
    }
}