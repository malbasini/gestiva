package com.gestiva.accounting.v2.report.web;

import java.util.ArrayList;
import java.util.List;

public class IncomeStatementView {

    private String formattedDateFrom;
    private String formattedDateTo;
    private String formattedTotalRevenue;
    private String formattedTotalCost;
    private String formattedPeriodResult;
    private List<IncomeStatementSectionView> sections = new ArrayList<>();

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

    public String getFormattedTotalRevenue() {
        return formattedTotalRevenue;
    }

    public void setFormattedTotalRevenue(String formattedTotalRevenue) {
        this.formattedTotalRevenue = formattedTotalRevenue;
    }

    public String getFormattedTotalCost() {
        return formattedTotalCost;
    }

    public void setFormattedTotalCost(String formattedTotalCost) {
        this.formattedTotalCost = formattedTotalCost;
    }

    public String getFormattedPeriodResult() {
        return formattedPeriodResult;
    }

    public void setFormattedPeriodResult(String formattedPeriodResult) {
        this.formattedPeriodResult = formattedPeriodResult;
    }

    public List<IncomeStatementSectionView> getSections() {
        return sections;
    }

    public void setSections(List<IncomeStatementSectionView> sections) {
        this.sections = sections;
    }
}