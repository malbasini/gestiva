package com.gestiva.sales.order.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

public class SalesOrderForm {

    private Long customerId;
    private Long quoteId;
    private String orderNumber;
    private String formattedOrderDate;

    @NotBlank(message = "Lo stato è obbligatorio")
    private String status;

    @Valid
    private List<SalesOrderLineForm> lines = new ArrayList<>();

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

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getFormattedOrderDate() {
        return formattedOrderDate;
    }

    public void setFormattedOrderDate(String formattedOrderDate) {
        this.formattedOrderDate = formattedOrderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<SalesOrderLineForm> getLines() {
        return lines;
    }

    public void setLines(List<SalesOrderLineForm> lines) {
        this.lines = lines;
    }
}