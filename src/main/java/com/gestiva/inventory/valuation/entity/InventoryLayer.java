package com.gestiva.inventory.valuation.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "inventory_layer",
        indexes = {
                @Index(name = "idx_inv_layer_tenant_item_date", columnList = "tenant_id,item_id,layer_date,id"),
                @Index(name = "idx_inv_layer_tenant_item_open", columnList = "tenant_id,item_id,closed"),
                @Index(name = "idx_inv_layer_source_movement", columnList = "tenant_id,source_movement_id")
        }
)
public class InventoryLayer extends TenantAwareEntity {

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "source_movement_id", nullable = false)
    private Long sourceMovementId;

    @Column(name = "layer_date", nullable = false)
    private LocalDate layerDate;

    @Column(name = "original_qty", nullable = false, precision = 15, scale = 3)
    private BigDecimal originalQty;

    @Column(name = "remaining_qty", nullable = false, precision = 15, scale = 3)
    private BigDecimal remainingQty;

    @Column(name = "unit_cost", nullable = false, precision = 15, scale = 4)
    private BigDecimal unitCost;

    @Column(name = "closed", nullable = false)
    private boolean closed;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getSourceMovementId() {
        return sourceMovementId;
    }

    public void setSourceMovementId(Long sourceMovementId) {
        this.sourceMovementId = sourceMovementId;
    }

    public LocalDate getLayerDate() {
        return layerDate;
    }

    public void setLayerDate(LocalDate layerDate) {
        this.layerDate = layerDate;
    }

    public BigDecimal getOriginalQty() {
        return originalQty;
    }

    public void setOriginalQty(BigDecimal originalQty) {
        this.originalQty = originalQty;
    }

    public BigDecimal getRemainingQty() {
        return remainingQty;
    }

    public void setRemainingQty(BigDecimal remainingQty) {
        this.remainingQty = remainingQty;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
