package com.gestiva.billing.invoice.web;

public class InvoiceDetailLineView {

    // GESTIVA V2 - PASSO 5.13.2395CC
    // Identificativo reale della riga fattura.

    private Long id;

    // GESTIVA V2 - PASSO 5.13.2395CE
    // Quantità numerica originale della riga fattura.
    // Utilizzata dai form, non per la formattazione UI.

    private java.math.BigDecimal quantity;
    private Integer lineNo;
    private String description;
    private String formattedQuantity;
    private String unitOfMeasure;
    private String formattedUnitPrice;
    private String formattedDiscountPct;
    private String formattedTaxPct;
    private String formattedLineTotal;

    public java.math.BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(java.math.BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLineNo() {
        return lineNo;
    }

    public void setLineNo(Integer lineNo) {
        this.lineNo = lineNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFormattedQuantity() {
        return formattedQuantity;
    }

    public void setFormattedQuantity(String formattedQuantity) {
        this.formattedQuantity = formattedQuantity;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getFormattedUnitPrice() {
        return formattedUnitPrice;
    }

    public void setFormattedUnitPrice(String formattedUnitPrice) {
        this.formattedUnitPrice = formattedUnitPrice;
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

    public String getFormattedLineTotal() {
        return formattedLineTotal;
    }

    public void setFormattedLineTotal(String formattedLineTotal) {
        this.formattedLineTotal = formattedLineTotal;
    }
}