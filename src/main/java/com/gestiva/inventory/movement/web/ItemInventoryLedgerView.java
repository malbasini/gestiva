package com.gestiva.inventory.movement.web;

import java.util.ArrayList;
import java.util.List;

public class ItemInventoryLedgerView {

    private Long itemId;
    private String itemCode;
    private String itemName;
    private String itemType;
    private boolean stockManaged;
    private String formattedCurrentStock;
    private List<ItemInventoryLedgerLineView> lines = new ArrayList<>();

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

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public boolean isStockManaged() {
        return stockManaged;
    }

    public void setStockManaged(boolean stockManaged) {
        this.stockManaged = stockManaged;
    }

    public String getFormattedCurrentStock() {
        return formattedCurrentStock;
    }

    public void setFormattedCurrentStock(String formattedCurrentStock) {
        this.formattedCurrentStock = formattedCurrentStock;
    }

    public List<ItemInventoryLedgerLineView> getLines() {
        return lines;
    }

    public void setLines(List<ItemInventoryLedgerLineView> lines) {
        this.lines = lines;
    }
}