package com.gestiva.logistics.ddt.web;

public class DeliveryNoteListItemView {

    private Long id;
    private String ddtNumber;
    private String formattedDdtDate;
    private String customerName;
    private String status;
    private Long salesOrderId;
    private String formattedTotalAmount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDdtNumber() {
        return ddtNumber;
    }

    public void setDdtNumber(String ddtNumber) {
        this.ddtNumber = ddtNumber;
    }

    public String getFormattedDdtDate() {
        return formattedDdtDate;
    }

    public void setFormattedDdtDate(String formattedDdtDate) {
        this.formattedDdtDate = formattedDdtDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getSalesOrderId() {
        return salesOrderId;
    }

    public void setSalesOrderId(Long salesOrderId) {
        this.salesOrderId = salesOrderId;
    }

    public String getFormattedTotalAmount() {
        return formattedTotalAmount;
    }

    public void setFormattedTotalAmount(String formattedTotalAmount) {
        this.formattedTotalAmount = formattedTotalAmount;
    }
}