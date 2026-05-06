package com.gestiva.warehouse.stock.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "stock_movement")
public class StockMovement extends TenantAwareEntity {

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "movement_date", nullable = false)
    private LocalDate movementDate;

    @Column(name = "direction", nullable = false, length = 10)
    private String direction; // IN, OUT

    @Column(name = "reason_code", nullable = false, length = 40)
    private String reasonCode; // MANUAL_LOAD, MANUAL_UNLOAD, DDT_ISSUE, DDT_CANCEL

    @Column(name = "quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "reference_type", length = 40)
    private String referenceType; // DDT, MANUAL

    @Column(name = "reference_id")
    private Long referenceId;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public LocalDate getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(LocalDate movementDate) {
        this.movementDate = movementDate;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }
}