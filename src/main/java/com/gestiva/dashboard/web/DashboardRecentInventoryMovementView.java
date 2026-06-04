package com.gestiva.dashboard.web;

public class DashboardRecentInventoryMovementView {

    private String date;
    private String itemCode;
    private String itemName;
    private String movementTypeLabel;
    private String quantity;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public String getMovementTypeLabel() {
        return movementTypeLabel;
    }

    public void setMovementTypeLabel(String movementTypeLabel) {
        this.movementTypeLabel = movementTypeLabel;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}