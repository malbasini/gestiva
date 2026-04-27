package com.gestiva.sales.quote.pdf;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class QuotePdfView {

    private String companyName;
    private String companyEmail;
    private String companyPhone;
    private String companyVatNumber;

    private String customerName;
    private String customerEmail;
    private String customerVatNumber;

    private String quoteNumber;
    private LocalDate quoteDate;
    private LocalDate validUntil;
    private String currencyCode;
    private String notes;

    private BigDecimal subtotalAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;

    private List<QuotePdfLineView> lines;
    private String formattedQuoteDate;
    private String formattedValidUntil;
    private String formattedSubtotalAmount;
    private String formattedTaxAmount;
    private String formattedTotalAmount;

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyEmail() { return companyEmail; }
    public void setCompanyEmail(String companyEmail) { this.companyEmail = companyEmail; }

    public String getCompanyPhone() { return companyPhone; }
    public void setCompanyPhone(String companyPhone) { this.companyPhone = companyPhone; }

    public String getCompanyVatNumber() { return companyVatNumber; }
    public void setCompanyVatNumber(String companyVatNumber) { this.companyVatNumber = companyVatNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerVatNumber() { return customerVatNumber; }
    public void setCustomerVatNumber(String customerVatNumber) { this.customerVatNumber = customerVatNumber; }

    public String getQuoteNumber() { return quoteNumber; }
    public void setQuoteNumber(String quoteNumber) { this.quoteNumber = quoteNumber; }

    public LocalDate getQuoteDate() { return quoteDate; }
    public void setQuoteDate(LocalDate quoteDate) { this.quoteDate = quoteDate; }

    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public BigDecimal getSubtotalAmount() { return subtotalAmount; }
    public void setSubtotalAmount(BigDecimal subtotalAmount) { this.subtotalAmount = subtotalAmount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public List<QuotePdfLineView> getLines() { return lines; }
    public void setLines(List<QuotePdfLineView> lines) { this.lines = lines; }

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



}