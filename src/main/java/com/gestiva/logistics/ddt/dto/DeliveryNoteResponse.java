package com.gestiva.logistics.ddt.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DeliveryNoteResponse {

    private Long id;
    private Long customerId;
    private Long salesOrderId;
    private String ddtNumber;
    private LocalDate ddtDate;
    private String status;
    private String currencyCode;
    private BigDecimal subtotalAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;

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

    public String getDdtNumber() {
        return ddtNumber;
    }

    public void setDdtNumber(String ddtNumber) {
        this.ddtNumber = ddtNumber;
    }

    public LocalDate getDdtDate() {
        return ddtDate;
    }

    public void setDdtDate(LocalDate ddtDate) {
        this.ddtDate = ddtDate;
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

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(BigDecimal subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}