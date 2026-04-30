package com.gestiva.dashboard.web;

public class DashboardQuoteItemView {

    private Long id;
    private String quoteNumber;
    private String status;
    private String formattedQuoteDate;
    private String formattedTotalAmount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuoteNumber() {
        return quoteNumber;
    }

    public void setQuoteNumber(String quoteNumber) {
        this.quoteNumber = quoteNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFormattedQuoteDate() {
        return formattedQuoteDate;
    }

    public void setFormattedQuoteDate(String formattedQuoteDate) {
        this.formattedQuoteDate = formattedQuoteDate;
    }

    public String getFormattedTotalAmount() {
        return formattedTotalAmount;
    }

    public void setFormattedTotalAmount(String formattedTotalAmount) {
        this.formattedTotalAmount = formattedTotalAmount;
    }
}