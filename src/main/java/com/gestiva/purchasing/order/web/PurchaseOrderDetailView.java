package com.gestiva.purchasing.order.web;

import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderDetailView {

    private Long id;
    private String orderNumber;
    private String formattedOrderDate;
    private String formattedExpectedDeliveryDate;
    private String supplierName;
    private String status;
    private String currencyCode;
    private String notes;
    private String formattedSubtotalAmount;
    private String formattedTaxAmount;
    private String formattedTotalAmount;
    private List<PurchaseOrderDetailLineView> lines = new ArrayList<>();

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
    }    public String getFormattedExpectedDeliveryDate() {
        return formattedExpectedDeliveryDate;
    }

    public void setFormattedExpectedDeliveryDate(String formattedExpectedDeliveryDate) {
        this.formattedExpectedDeliveryDate = formattedExpectedDeliveryDate;
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
    }    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }    public String getFormattedSubtotalAmount() {
        return formattedSubtotalAmount;
    }

    public void setFormattedSubtotalAmount(String formattedSubtotalAmount) {
        this.formattedSubtotalAmount = formattedSubtotalAmount;
    }    public String getFormattedTaxAmount() {
        return formattedTaxAmount;
    }

    public void setFormattedTaxAmount(String formattedTaxAmount) {
        this.formattedTaxAmount = formattedTaxAmount;
    }    public String getFormattedTotalAmount() {
        return formattedTotalAmount;
    }

    public void setFormattedTotalAmount(String formattedTotalAmount) {
        this.formattedTotalAmount = formattedTotalAmount;
    }    public List<PurchaseOrderDetailLineView> getLines() {
        return lines;
    }

    public void setLines(List<PurchaseOrderDetailLineView> lines) {
        this.lines = lines;
    }
}