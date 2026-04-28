package com.gestiva.sales.order.pdf;

import java.math.BigDecimal;

public class SalesOrderPdfLineView {

    private Integer lineNo;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    private String formattedQuantity;
    private String formattedUnitPrice;
    private String formattedLineTotal;

    private BigDecimal discountPct;
    private BigDecimal taxPct;
    private BigDecimal taxAmount;

    private String formattedDiscountPct;
    private String formattedTaxPct;
    private String formattedTaxAmount;

    public Integer getLineNo() { return lineNo; }
    public void setLineNo(Integer lineNo) { this.lineNo = lineNo; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

    public String getFormattedQuantity() { return formattedQuantity; }
    public void setFormattedQuantity(String formattedQuantity) { this.formattedQuantity = formattedQuantity; }

    public String getFormattedUnitPrice() { return formattedUnitPrice; }
    public void setFormattedUnitPrice(String formattedUnitPrice) { this.formattedUnitPrice = formattedUnitPrice; }

    public String getFormattedLineTotal() { return formattedLineTotal; }
    public void setFormattedLineTotal(String formattedLineTotal) { this.formattedLineTotal = formattedLineTotal; }

    public BigDecimal getDiscountPct() {
        return discountPct;
    }

    public void setDiscountPct(BigDecimal discountPct) {
        this.discountPct = discountPct;
    }

    public BigDecimal getTaxPct() {
        return taxPct;
    }

    public void setTaxPct(BigDecimal taxPct) {
        this.taxPct = taxPct;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getFormattedDiscountPct() {
        return formattedDiscountPct;
    }

    public void setFormattedDiscountPct(String formattedDiscountPct) {
        this.formattedDiscountPct = formattedDiscountPct;
    }

    public String getFormattedTaxPct() {
        return formattedTaxPct;
    }

    public void setFormattedTaxPct(String formattedTaxPct) {
        this.formattedTaxPct = formattedTaxPct;
    }

    public String getFormattedTaxAmount() {
        return formattedTaxAmount;
    }

    public void setFormattedTaxAmount(String formattedTaxAmount) {
        this.formattedTaxAmount = formattedTaxAmount;
    }
}