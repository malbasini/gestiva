package com.gestiva.purchasing.order.web;

public class PurchaseOrderListItemView {

    private Long id;
    private String orderNumber;
    private String formattedOrderDate;
    private String supplierName;
    private String status;
    private String formattedTotalAmount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }    public String getFormattedOrderDate() {
        return formattedOrderDate;
    }

    public void setFormattedOrderDate(String formattedOrderDate) {
        this.formattedOrderDate = formattedOrderDate;
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