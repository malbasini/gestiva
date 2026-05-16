package com.gestiva.inventory.valuation.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "inventory_layer_consumption",
        indexes = {
                @Index(name = "idx_inv_layer_cons_out", columnList = "tenant_id,out_movement_id"),
                @Index(name = "idx_inv_layer_cons_layer", columnList = "tenant_id,layer_id")
        }
)
public class InventoryLayerConsumption extends TenantAwareEntity {

    @Column(name = "out_movement_id", nullable = false)
    private Long outMovementId;

    @Column(name = "layer_id", nullable = false)
    private Long layerId;

    @Column(name = "consumed_qty", nullable = false, precision = 15, scale = 3)
    private BigDecimal consumedQty;

    @Column(name = "unit_cost", nullable = false, precision = 15, scale = 4)
    private BigDecimal unitCost;

    @Column(name = "total_cost", nullable = false, precision = 15, scale = 4)
    private BigDecimal totalCost;

    public Long getOutMovementId() {
        return outMovementId;
    }

    public void setOutMovementId(Long outMovementId) {
        this.outMovementId = outMovementId;
    }

    public Long getLayerId() {
        return layerId;
    }

    public void setLayerId(Long layerId) {
        this.layerId = layerId;
    }

    public BigDecimal getConsumedQty() {
        return consumedQty;
    }

    public void setConsumedQty(BigDecimal consumedQty) {
        this.consumedQty = consumedQty;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
}