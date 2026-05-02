package com.gestiva.logistics.ddt.web;

public class DeliveryNoteDetailLineView {

    private Integer lineNo;
    private String description;
    private String formattedQuantity;
    private String unitOfMeasure;
    private String formattedUnitPrice;
    private String formattedDiscountPct;
    private String formattedTaxPct;
    private String formattedLineTotal;

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