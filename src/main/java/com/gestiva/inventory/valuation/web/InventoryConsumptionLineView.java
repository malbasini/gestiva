package com.gestiva.inventory.valuation.web;

public class InventoryConsumptionLineView {

    private Long layerId;
    private String formattedLayerDate;
    private String formattedConsumedQty;
    private String formattedUnitCost;
    private String formattedTotalCost;

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

    public String getFormattedConsumedQty() {
        return formattedConsumedQty;
    }

    public void setFormattedConsumedQty(String formattedConsumedQty) {
        this.formattedConsumedQty = formattedConsumedQty;
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