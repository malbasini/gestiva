package com.gestiva.inventory.valuation.web;

import java.util.ArrayList;
import java.util.List;

public class ItemInventoryValuationView {

    private Long itemId;
    private String itemCode;
    private String itemName;
    private String valuationMethod;
    private String formattedCurrentQty;
    private String formattedInventoryValue;
    private String formattedAverageResidualCost;
    private List<InventoryLayerLineView> layers = new ArrayList<>();
    private boolean averageMethod;
    private String averageMethodNote;


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

    public String getValuationMethod() {
        return valuationMethod;
    }

    public void setValuationMethod(String valuationMethod) {
        this.valuationMethod = valuationMethod;
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

    public List<InventoryLayerLineView> getLayers() {
        return layers;
    }

    public void setLayers(List<InventoryLayerLineView> layers) {
        this.layers = layers;
    }

    public boolean isAverageMethod() {
        return averageMethod;
    }

    public void setAverageMethod(boolean averageMethod) {
        this.averageMethod = averageMethod;
    }

    public String getAverageMethodNote() {
        return averageMethodNote;
    }

    public void setAverageMethodNote(String averageMethodNote) {
        this.averageMethodNote = averageMethodNote;
    }
}
