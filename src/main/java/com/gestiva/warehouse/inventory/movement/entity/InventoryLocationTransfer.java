package com.gestiva.warehouse.inventory.movement.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "inventory_location_transfer")
public class InventoryLocationTransfer extends TenantAwareEntity {

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "source_location_id", nullable = false)
    private Long sourceLocationId;

    @Column(name = "target_location_id", nullable = false)
    private Long targetLocationId;

    @Column(name = "quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "movement_date", nullable = false)
    private LocalDate movementDate;

    @Column(name = "operation_key", nullable = false, length = 120)
    private String operationKey;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "out_movement_id", nullable = true)
    private Long outMovementId;

    @Column(name = "in_movement_id", nullable = true)
    private Long inMovementId;

    @Column(name = "notes", nullable = true, length = 1000)
    private String notes;

    public InventoryLocationTransfer() {
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

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getSourceLocationId() {
        return sourceLocationId;
    }

    public void setSourceLocationId(Long sourceLocationId) {
        this.sourceLocationId = sourceLocationId;
    }

    public Long getTargetLocationId() {
        return targetLocationId;
    }

    public void setTargetLocationId(Long targetLocationId) {
        this.targetLocationId = targetLocationId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public LocalDate getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(LocalDate movementDate) {
        this.movementDate = movementDate;
    }

    public String getOperationKey() {
        return operationKey;
    }

    public void setOperationKey(String operationKey) {
        this.operationKey = operationKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getOutMovementId() {
        return outMovementId;
    }

    public void setOutMovementId(Long outMovementId) {
        this.outMovementId = outMovementId;
    }

    public Long getInMovementId() {
        return inMovementId;
    }

    public void setInMovementId(Long inMovementId) {
        this.inMovementId = inMovementId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

}
