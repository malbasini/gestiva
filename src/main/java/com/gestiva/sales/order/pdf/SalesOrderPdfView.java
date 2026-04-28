package com.gestiva.sales.order.pdf;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SalesOrderPdfView {

    private String companyName;
    private String companyEmail;
    private String companyPhone;
    private String companyVatNumber;

    private String customerName;
    private String customerEmail;
    private String customerVatNumber;

    private Long orderId;
    private Long quoteId;
    private String orderNumber;
    private LocalDate orderDate;
    private String currencyCode;
    private String status;
    private BigDecimal totalAmount;

    private String formattedOrderDate;
    private String formattedTotalAmount;

    private List<SalesOrderPdfLineView> lines;
    private BigDecimal subtotalAmount;
    private BigDecimal taxAmount;
    private String formattedSubtotalAmount;
    private String formattedTaxAmount;

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyEmail() { return companyEmail; }
    public void setCompanyEmail(String companyEmail) { this.companyEmail = companyEmail; }

    public String getCompanyPhone() { return companyPhone; }
    public void setCompanyPhone(String companyPhone) { this.companyPhone = companyPhone; }

    public String getCompanyVatNumber() { return companyVatNumber; }
    public void setCompanyVatNumber(String companyVatNumber) { this.companyVatNumber = companyVatNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerVatNumber() { return customerVatNumber; }
    public void setCustomerVatNumber(String customerVatNumber) { this.customerVatNumber = customerVatNumber; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getQuoteId() { return quoteId; }
    public void setQuoteId(Long quoteId) { this.quoteId = quoteId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getFormattedOrderDate() { return formattedOrderDate; }
    public void setFormattedOrderDate(String formattedOrderDate) { this.formattedOrderDate = formattedOrderDate; }

    public String getFormattedTotalAmount() { return formattedTotalAmount; }
    public void setFormattedTotalAmount(String formattedTotalAmount) { this.formattedTotalAmount = formattedTotalAmount; }

    public List<SalesOrderPdfLineView> getLines() { return lines; }
    public void setLines(List<SalesOrderPdfLineView> lines) { this.lines = lines; }

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
}