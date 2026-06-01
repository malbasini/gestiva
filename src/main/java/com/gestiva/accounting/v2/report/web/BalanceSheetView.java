package com.gestiva.accounting.v2.report.web;

import java.util.ArrayList;
import java.util.List;

public class BalanceSheetView {

    private String formattedDateFrom;
    private String formattedDateTo;
    private String formattedTotalAssets;
    private String formattedTotalLiabilities;
    private String formattedTotalEquity;
    private List<BalanceSheetSectionView> sections = new ArrayList<>();
    private String formattedPeriodResult;


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

    public String getFormattedTotalAssets() {
        return formattedTotalAssets;
    }

    public void setFormattedTotalAssets(String formattedTotalAssets) {
        this.formattedTotalAssets = formattedTotalAssets;
    }

    public String getFormattedTotalLiabilities() {
        return formattedTotalLiabilities;
    }

    public void setFormattedTotalLiabilities(String formattedTotalLiabilities) {
        this.formattedTotalLiabilities = formattedTotalLiabilities;
    }

    public String getFormattedTotalEquity() {
        return formattedTotalEquity;
    }

    public void setFormattedTotalEquity(String formattedTotalEquity) {
        this.formattedTotalEquity = formattedTotalEquity;
    }

    public List<BalanceSheetSectionView> getSections() {
        return sections;
    }

    public void setSections(List<BalanceSheetSectionView> sections) {
        this.sections = sections;
    }

    public String getFormattedPeriodResult() {
        return formattedPeriodResult;
    }

    public void setFormattedPeriodResult(String formattedPeriodResult) {
        this.formattedPeriodResult = formattedPeriodResult;
    }
}
