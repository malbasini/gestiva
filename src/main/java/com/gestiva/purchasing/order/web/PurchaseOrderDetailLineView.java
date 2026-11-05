package com.gestiva.purchasing.order.web;

public class PurchaseOrderDetailLineView {

    private Long lineId;
    private String formattedOrderedBaseQuantity;
    private String formattedReceivedBaseQuantity;
    private String formattedRemainingBaseQuantity;

    private Integer lineNo;
    private String description;
    private String formattedQuantity;
    private String unitOfMeasureCode;
    private String formattedUnitPrice;
    private String formattedDiscountPct;
    private String formattedTaxPct;
    private String formattedLineTotal;

    public String getUnitOfMeasureCode() {
        return unitOfMeasureCode;
    }

    public void setUnitOfMeasureCode(String unitOfMeasureCode) {
        this.unitOfMeasureCode = unitOfMeasureCode;
    }

    public Integer getLineNo() {
        return lineNo;
    }

    public void setLineNo(Integer lineNo) {
        this.lineNo = lineNo;
    }    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }    public String getFormattedQuantity() {
        return formattedQuantity;
    }

    public void setFormattedQuantity(String formattedQuantity) {
        this.formattedQuantity = formattedQuantity;
    }    public String getFormattedUnitPrice() {
        return formattedUnitPrice;
    }

    public void setFormattedUnitPrice(String formattedUnitPrice) {
        this.formattedUnitPrice = formattedUnitPrice;
    }    public String getFormattedDiscountPct() {
        return formattedDiscountPct;
    }

    public void setFormattedDiscountPct(String formattedDiscountPct) {
        this.formattedDiscountPct = formattedDiscountPct;
    }    public String getFormattedTaxPct() {
        return formattedTaxPct;
    }

    public void setFormattedTaxPct(String formattedTaxPct) {
        this.formattedTaxPct = formattedTaxPct;
    }    public String getFormattedLineTotal() {
        return formattedLineTotal;
    }

    public void setFormattedLineTotal(String formattedLineTotal) {
        this.formattedLineTotal = formattedLineTotal;
    }

    public Long getLineId() {
        return lineId;
    }

    public void setLineId(Long lineId) {
        this.lineId = lineId;
    }

    public String getFormattedOrderedBaseQuantity() {
        return formattedOrderedBaseQuantity;
    }

    public void setFormattedOrderedBaseQuantity(
            String formattedOrderedBaseQuantity) {
        this.formattedOrderedBaseQuantity = formattedOrderedBaseQuantity;
    }

    public String getFormattedReceivedBaseQuantity() {
        return formattedReceivedBaseQuantity;
    }

    public void setFormattedReceivedBaseQuantity(
            String formattedReceivedBaseQuantity) {
        this.formattedReceivedBaseQuantity = formattedReceivedBaseQuantity;
    }

    public String getFormattedRemainingBaseQuantity() {
        return formattedRemainingBaseQuantity;
    }

    public void setFormattedRemainingBaseQuantity(
            String formattedRemainingBaseQuantity) {
        this.formattedRemainingBaseQuantity = formattedRemainingBaseQuantity;
    }

}