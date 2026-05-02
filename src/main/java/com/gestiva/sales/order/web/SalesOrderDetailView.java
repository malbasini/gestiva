package com.gestiva.sales.order.web;

import java.util.List;

public class SalesOrderDetailView {

    private Long id;
    private Long customerId;
    private Long quoteId;
    private String customerName;
    private String orderNumber;
    private String status;
    private String currencyCode;

    private String formattedOrderDate;
    private String formattedSubtotalAmount;
    private String formattedTaxAmount;
    private String formattedTotalAmount;
    private List<SalesOrderDetailLineView> lines;

    private Long deliveryNoteId;
    private String deliveryNoteNumber;

    public Long getDeliveryNoteId() {
        return deliveryNoteId;
    }

    public void setDeliveryNoteId(Long deliveryNoteId) {
        this.deliveryNoteId = deliveryNoteId;
    }

    public String getDeliveryNoteNumber() {
        return deliveryNoteNumber;
    }

    public void setDeliveryNoteNumber(String deliveryNoteNumber) {
        this.deliveryNoteNumber = deliveryNoteNumber;
    }
    
    private boolean deliveryNoteCreatable;
    private boolean deliveryNoteExists;

    public boolean isDeliveryNoteCreatable() {
        return deliveryNoteCreatable;
    }

    public void setDeliveryNoteCreatable(boolean deliveryNoteCreatable) {
        this.deliveryNoteCreatable = deliveryNoteCreatable;
    }

    public boolean isDeliveryNoteExists() {
        return deliveryNoteExists;
    }

    public void setDeliveryNoteExists(boolean deliveryNoteExists) {
        this.deliveryNoteExists = deliveryNoteExists;
    }

    private boolean actionable;

    public boolean isActionable() {
        return actionable;
    }

    public void setActionable(boolean actionable) {
        this.actionable = actionable;
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

    public Long getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(Long quoteId) {
        this.quoteId = quoteId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
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

    public String getFormattedOrderDate() {
        return formattedOrderDate;
    }

    public void setFormattedOrderDate(String formattedOrderDate) {
        this.formattedOrderDate = formattedOrderDate;
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

    public List<SalesOrderDetailLineView> getLines() {
        return lines;
    }

    public void setLines(List<SalesOrderDetailLineView> lines) {
        this.lines = lines;
    }
}