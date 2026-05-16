package com.gestiva.inventory.valuation.web;

public class InventoryLayerLineView {

    private Long layerId;
    private String formattedLayerDate;
    private Long sourceMovementId;
    private String formattedOriginalQty;
    private String formattedRemainingQty;
    private String formattedUnitCost;
    private String formattedResidualValue;

    public Long getLayerId() {
        return layerId;
    }

    public void setLayerId(Long layerId) {
        this.layerId = layerId;
    }

    public String getFormattedLayerDate() {
        return formattedLayerDate;
    }

    public void setFormattedLayerDate(String formattedLayerDate) {
        this.formattedLayerDate = formattedLayerDate;
    }

    public Long getSourceMovementId() {
        return sourceMovementId;
    }

    public void setSourceMovementId(Long sourceMovementId) {
        this.sourceMovementId = sourceMovementId;
    }

    public String getFormattedOriginalQty() {
        return formattedOriginalQty;
    }

    public void setFormattedOriginalQty(String formattedOriginalQty) {
        this.formattedOriginalQty = formattedOriginalQty;
    }

    public String getFormattedRemainingQty() {
        return formattedRemainingQty;
    }

    public void setFormattedRemainingQty(String formattedRemainingQty) {
        this.formattedRemainingQty = formattedRemainingQty;
    }

    public String getFormattedUnitCost() {
        return formattedUnitCost;
    }

    public void setFormattedUnitCost(String formattedUnitCost) {
        this.formattedUnitCost = formattedUnitCost;
    }

    public String getFormattedResidualValue() {
        return formattedResidualValue;
    }

    public void setFormattedResidualValue(String formattedResidualValue) {
        this.formattedResidualValue = formattedResidualValue;
    }
}
