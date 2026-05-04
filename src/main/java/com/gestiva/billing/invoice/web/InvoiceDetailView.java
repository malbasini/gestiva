package com.gestiva.billing.invoice.web;

import java.util.List;

public class InvoiceDetailView {

    private Long id;
    private Long customerId;
    private Long deliveryNoteId;
    private Long salesOrderId;

    private String customerName;
    private String invoiceNumber;
    private String status;
    private String currencyCode;

    private String formattedInvoiceDate;
    private String formattedSubtotalAmount;
    private String formattedTaxAmount;
    private String formattedTotalAmount;

    private String notes;

    private boolean cancelable;
    private List<InvoiceDetailLineView> lines;

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

    public Long getDeliveryNoteId() {
        return deliveryNoteId;
    }

    public void setDeliveryNoteId(Long deliveryNoteId) {
        this.deliveryNoteId = deliveryNoteId;
    }

    public Long getSalesOrderId() {
        return salesOrderId;
    }

    public void setSalesOrderId(Long salesOrderId) {
        this.salesOrderId = salesOrderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
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

    public String getFormattedInvoiceDate() {
        return formattedInvoiceDate;
    }

    public void setFormattedInvoiceDate(String formattedInvoiceDate) {
        this.formattedInvoiceDate = formattedInvoiceDate;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isCancelable() {
        return cancelable;
    }

    public void setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
    }

    public List<InvoiceDetailLineView> getLines() {
        return lines;
    }

    public void setLines(List<InvoiceDetailLineView> lines) {
        this.lines = lines;
    }
}