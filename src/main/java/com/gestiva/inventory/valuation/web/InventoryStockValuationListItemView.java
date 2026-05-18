package com.gestiva.inventory.valuation.web;

public class InventoryStockValuationListItemView {

    private Long itemId;
    private String itemCode;
    private String itemName;
    private String formattedCurrentQty;
    private String formattedInventoryValue;
    private String formattedAverageResidualCost;
    private int openLayerCount;

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

    public String getFormattedCurrentQty() {
        return formattedCurrentQty;
    }

    public void setFormattedCurrentQty(String formattedCurrentQty) {
        this.formattedCurrentQty = formattedCurrentQty;
    }

    public String getFormattedInventoryValue() {
        return formattedInventoryValue;
    }

    public void setFormattedInventoryValue(String formattedInventoryValue) {
        this.formattedInventoryValue = formattedInventoryValue;
    }

    public String getFormattedAverageResidualCost() {
        return formattedAverageResidualCost;
    }

    public void setFormattedAverageResidualCost(String formattedAverageResidualCost) {
        this.formattedAverageResidualCost = formattedAverageResidualCost;
    }

    public int getOpenLayerCount() {
        return openLayerCount;
    }

    public void setOpenLayerCount(int openLayerCount) {
        this.openLayerCount = openLayerCount;
    }
}