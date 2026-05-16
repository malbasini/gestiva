package com.gestiva.inventory.valuation.model;

import java.math.BigDecimal;

public class InventoryValuationResult {

    private final BigDecimal totalCost;
    private final BigDecimal unitCost;

    public InventoryValuationResult(BigDecimal totalCost, BigDecimal unitCost) {
        this.totalCost = totalCost;
        this.unitCost = unitCost;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }
}