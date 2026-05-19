package com.gestiva.inventory.valuation.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "inventory_average_balance",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_inv_avg_balance_tenant_item", columnNames = {"tenant_id", "item_id"})
        }
)
public class InventoryAverageBalance extends TenantAwareEntity {

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "current_qty", nullable = false, precision = 15, scale = 3)
    private BigDecimal currentQty;

    @Column(name = "current_total_value", nullable = false, precision = 15, scale = 4)
    private BigDecimal currentTotalValue;

    @Column(name = "current_avg_unit_cost", nullable = false, precision = 15, scale = 4)
    private BigDecimal currentAvgUnitCost;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getCurrentQty() {
        return currentQty;
    }

    public void setCurrentQty(BigDecimal currentQty) {
        this.currentQty = currentQty;
    }

    public BigDecimal getCurrentTotalValue() {
        return currentTotalValue;
    }

    public void setCurrentTotalValue(BigDecimal currentTotalValue) {
        this.currentTotalValue = currentTotalValue;
    }

    public BigDecimal getCurrentAvgUnitCost() {
        return currentAvgUnitCost;
    }

    public void setCurrentAvgUnitCost(BigDecimal currentAvgUnitCost) {
        this.currentAvgUnitCost = currentAvgUnitCost;
    }
}