package com.gestiva.accounting.vat.web;

public class VatSalesRegisterRowView {

    private Long invoiceId;
    private String formattedInvoiceDate;
    private String invoiceNumber;
    private String customerName;
    private String formattedTaxPct;
    private String formattedTaxableAmount;
    private String formattedTaxAmount;
    private String formattedTotalAmount;

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getFormattedInvoiceDate() {
        return formattedInvoiceDate;
    }

    public void setFormattedInvoiceDate(String formattedInvoiceDate) {
        this.formattedInvoiceDate = formattedInvoiceDate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getFormattedTaxPct() {
        return formattedTaxPct;
    }

    public void setFormattedTaxPct(String formattedTaxPct) {
        this.formattedTaxPct = formattedTaxPct;
    }

    public String getFormattedTaxableAmount() {
        return formattedTaxableAmount;
    }

    public void setFormattedTaxableAmount(String formattedTaxableAmount) {
        this.formattedTaxableAmount = formattedTaxableAmount;
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