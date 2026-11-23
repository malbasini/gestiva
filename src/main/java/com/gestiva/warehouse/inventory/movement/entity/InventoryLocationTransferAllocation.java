package com.gestiva.warehouse.inventory.movement.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "inventory_location_transfer_allocation")
public class InventoryLocationTransferAllocation extends TenantAwareEntity {

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "transfer_id", nullable = false)
    private Long transferId;

    @Column(name = "source_layer_id", nullable = false)
    private Long sourceLayerId;

    @Column(name = "target_layer_id", nullable = true)
    private Long targetLayerId;

    @Column(name = "allocated_qty", nullable = false, precision = 15, scale = 3)
    private BigDecimal allocatedQty;

    @Column(name = "unit_cost", nullable = false, precision = 15, scale = 4)
    private BigDecimal unitCost;

    @Column(name = "layer_date", nullable = false)
    private LocalDate layerDate;

    public InventoryLocationTransferAllocation() {
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Long getTransferId() {
        return transferId;
    }

    public void setTransferId(Long transferId) {
        this.transferId = transferId;
    }

    public Long getSourceLayerId() {
        return sourceLayerId;
    }

    public void setSourceLayerId(Long sourceLayerId) {
        this.sourceLayerId = sourceLayerId;
    }

    public Long getTargetLayerId() {
        return targetLayerId;
    }

    public void setTargetLayerId(Long targetLayerId) {
        this.targetLayerId = targetLayerId;
    }

    public BigDecimal getAllocatedQty() {
        return allocatedQty;
    }

    public void setAllocatedQty(BigDecimal allocatedQty) {
        this.allocatedQty = allocatedQty;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public LocalDate getLayerDate() {
        return layerDate;
    }

    public void setLayerDate(LocalDate layerDate) {
        this.layerDate = layerDate;
    }

}
