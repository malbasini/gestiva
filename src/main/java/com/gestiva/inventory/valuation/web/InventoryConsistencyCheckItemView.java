package com.gestiva.inventory.valuation.web;

public class InventoryConsistencyCheckItemView {

    private Long itemId;
    private String itemCode;
    private String itemName;
    private String formattedLedgerQty;
    private String formattedLayerQty;
    private String formattedDifferenceQty;
    private boolean aligned;
    private String statusLabel;

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

    public String getFormattedLedgerQty() {
        return formattedLedgerQty;
    }

    public void setFormattedLedgerQty(String formattedLedgerQty) {
        this.formattedLedgerQty = formattedLedgerQty;
    }

    public String getFormattedLayerQty() {
        return formattedLayerQty;
    }

    public void setFormattedLayerQty(String formattedLayerQty) {
        this.formattedLayerQty = formattedLayerQty;
    }

    public String getFormattedDifferenceQty() {
        return formattedDifferenceQty;
    }

    public void setFormattedDifferenceQty(String formattedDifferenceQty) {
        this.formattedDifferenceQty = formattedDifferenceQty;
    }

    public boolean isAligned() {
        return aligned;
    }

    public void setAligned(boolean aligned) {
        this.aligned = aligned;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }
}