package com.gestiva.logistics.ddt.web;

import java.util.List;

public class DeliveryNoteDetailView {

    private Long id;
    private Long customerId;
    private Long salesOrderId;

    private String customerName;
    private String ddtNumber;
    private String status;
    private String currencyCode;

    private String formattedDdtDate;
    private String formattedSubtotalAmount;
    private String formattedTaxAmount;
    private String formattedTotalAmount;

    private String transportReason;
    private String carriageCondition;
    private String carrierName;
    private String notes;

    private List<DeliveryNoteDetailLineView> lines;

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

    public String getDdtNumber() {
        return ddtNumber;
    }

    public void setDdtNumber(String ddtNumber) {
        this.ddtNumber = ddtNumber;
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

    public String getFormattedDdtDate() {
        return formattedDdtDate;
    }

    public void setFormattedDdtDate(String formattedDdtDate) {
        this.formattedDdtDate = formattedDdtDate;
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

    public String getTransportReason() {
        return transportReason;
    }

    public void setTransportReason(String transportReason) {
        this.transportReason = transportReason;
    }

    public String getCarriageCondition() {
        return carriageCondition;
    }

    public void setCarriageCondition(String carriageCondition) {
        this.carriageCondition = carriageCondition;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<DeliveryNoteDetailLineView> getLines() {
        return lines;
    }

    public void setLines(List<DeliveryNoteDetailLineView> lines) {
        this.lines = lines;
    }
}