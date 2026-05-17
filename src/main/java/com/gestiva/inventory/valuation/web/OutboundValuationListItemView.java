package com.gestiva.inventory.valuation.web;

public class OutboundValuationListItemView {

    private Long movementId;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private String formattedMovementDate;
    private String causalCode;
    private String referenceLabel;
    private String formattedQuantity;
    private String formattedUnitCost;
    private String formattedTotalCost;

    public Long getMovementId() {
        return movementId;
    }

    public void setMovementId(Long movementId) {
        this.movementId = movementId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getFormattedMovementDate() {
        return formattedMovementDate;
    }

    public void setFormattedMovementDate(String formattedMovementDate) {
        this.formattedMovementDate = formattedMovementDate;
    }

    public String getCausalCode() {
        return causalCode;
    }

    public void setCausalCode(String causalCode) {
        this.causalCode = causalCode;
    }

    public String getReferenceLabel() {
        return referenceLabel;
    }

    public void setReferenceLabel(String referenceLabel) {
        this.referenceLabel = referenceLabel;
    }

    public String getFormattedQuantity() {
        return formattedQuantity;
    }

    public void setFormattedQuantity(String formattedQuantity) {
        this.formattedQuantity = formattedQuantity;
    }

    public String getFormattedUnitCost() {
        return formattedUnitCost;
    }

    public void setFormattedUnitCost(String formattedUnitCost) {
        this.formattedUnitCost = formattedUnitCost;
    }

    public String getFormattedTotalCost() {
        return formattedTotalCost;
    }

    public void setFormattedTotalCost(String formattedTotalCost) {
        this.formattedTotalCost = formattedTotalCost;
    }
}