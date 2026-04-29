package com.gestiva.sales.quote.web;

import java.math.BigDecimal;
import java.time.LocalDate;

public class QuoteListItemView {

    private Long id;
    private Long customerId;
    private String quoteNumber;
    private LocalDate quoteDate;
    private LocalDate validUntil;
    private String status;
    private String currencyCode;
    private BigDecimal totalAmount;

    private String formattedQuoteDate;
    private String formattedValidUntil;
    private String formattedTotalAmount;

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

    public String getQuoteNumber() {
        return quoteNumber;
    }

    public void setQuoteNumber(String quoteNumber) {
        this.quoteNumber = quoteNumber;
    }

    public LocalDate getQuoteDate() {
        return quoteDate;
    }

    public void setQuoteDate(LocalDate quoteDate) {
        this.quoteDate = quoteDate;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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

    public String getFormattedTotalAmount() {
        return formattedTotalAmount;
    }

    public void setFormattedTotalAmount(String formattedTotalAmount) {
        this.formattedTotalAmount = formattedTotalAmount;
    }
}