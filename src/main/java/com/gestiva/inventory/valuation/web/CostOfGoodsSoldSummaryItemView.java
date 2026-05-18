package com.gestiva.inventory.valuation.web;

public class CostOfGoodsSoldSummaryItemView {

    private Long itemId;
    private String itemCode;
    private String itemName;
    private String formattedTotalQuantity;
    private String formattedTotalCost;
    private String formattedAverageUnitCost;

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

    public String getFormattedTotalQuantity() {
        return formattedTotalQuantity;
    }

    public void setFormattedTotalQuantity(String formattedTotalQuantity) {
        this.formattedTotalQuantity = formattedTotalQuantity;
    }

    public String getFormattedTotalCost() {
        return formattedTotalCost;
    }

    public void setFormattedTotalCost(String formattedTotalCost) {
        this.formattedTotalCost = formattedTotalCost;
    }

    public String getFormattedAverageUnitCost() {
        return formattedAverageUnitCost;
    }

    public void setFormattedAverageUnitCost(String formattedAverageUnitCost) {
        this.formattedAverageUnitCost = formattedAverageUnitCost;
    }
}
