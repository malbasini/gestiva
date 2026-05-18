package com.gestiva.inventory.valuation.web;

import java.math.BigDecimal;

public class CostOfGoodsSoldSummaryRow {

    private final Long itemId;
    private final BigDecimal totalQuantity;
    private final BigDecimal totalCost;

    public CostOfGoodsSoldSummaryRow(Long itemId, BigDecimal totalQuantity, BigDecimal totalCost) {
        this.itemId = itemId;
        this.totalQuantity = totalQuantity;
        this.totalCost = totalCost;
    }

    public Long getItemId() {
        return itemId;
    }

    public BigDecimal getTotalQuantity() {
        return totalQuantity;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }
}