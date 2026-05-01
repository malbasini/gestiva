package com.gestiva.sales.quote.web;

import java.math.BigDecimal;
import java.util.List;

public class QuoteDetailView {

    private Long id;
    private Long customerId;
    private String customerName;
    private String quoteNumber;
    private String status;
    private String currencyCode;
    private String notes;

    private String formattedQuoteDate;
    private String formattedValidUntil;
    private String formattedSubtotalAmount;
    private String formattedTaxAmount;
    private String formattedTotalAmount;

    private List<QuoteDetailLineView> lines;

    private boolean locked;

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getFormattedQuoteDate() {
        return formattedQuoteDate;
    }

    public void setFormattedQuoteDate(String formattedQuoteDate) {
        this.formattedQuoteDate = formattedQuoteDate;
    }

    public String getFormattedValidUntil() {
        return formattedValidUntil;
    }

    public void setFormattedValidUntil(String formattedValidUntil) {
        this.formattedValidUntil = formattedValidUntil;
    }

    public String getFormattedSubtotalAmount() {
        return formattedSubtotalAmount;
    }

    public void setFormattedSubtotalAmount(String formattedSubtotalAmount) {
        this.formattedSubtotalAmount = formattedSubtotalAmount;
    }

    public String getFormattedTaxAmount() {
        return formattedTaxAmount;
    }

    public void setFormattedTaxAmount(String formattedTaxAmount) {
        this.formattedTaxAmount = formattedTaxAmount;
    }

    public String getFormattedTotalAmount() {
        return formattedTotalAmount;
    }

    public void setFormattedTotalAmount(String formattedTotalAmount) {
        this.formattedTotalAmount = formattedTotalAmount;
    }

    public List<QuoteDetailLineView> getLines() {
        return lines;
    }

    public void setLines(List<QuoteDetailLineView> lines) {
        this.lines = lines;
    }
}