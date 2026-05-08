package com.gestiva.purchasing.invoice.web;

public class SupplierInvoiceListItemView {

    private Long id;
    private String invoiceNumber;
    private String formattedInvoiceDate;
    private String supplierName;
    private String status;
    private String formattedTotalAmount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }    public String getFormattedInvoiceDate() {
        return formattedInvoiceDate;
    }

    public void setFormattedInvoiceDate(String formattedInvoiceDate) {
        this.formattedInvoiceDate = formattedInvoiceDate;
    }    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }    public String getFormattedTotalAmount() {
        return formattedTotalAmount;
    }

    public void setFormattedTotalAmount(String formattedTotalAmount) {
        this.formattedTotalAmount = formattedTotalAmount;
    }
}
