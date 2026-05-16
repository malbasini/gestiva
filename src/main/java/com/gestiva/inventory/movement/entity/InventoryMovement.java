package com.gestiva.inventory.movement.entity;

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
        name = "inventory_movement",
        indexes = {
                @Index(name = "idx_inv_mov_tenant_item_date", columnList = "tenant_id,item_id,movement_date"),
                @Index(name = "idx_inv_mov_tenant_ref", columnList = "tenant_id,reference_type,reference_id"),
                @Index(name = "idx_inv_mov_tenant_causal", columnList = "tenant_id,causal_code")
        }
)
public class InventoryMovement extends TenantAwareEntity {

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "movement_date", nullable = false)
    private LocalDate movementDate;

    @Column(name = "movement_type", nullable = false, length = 30)
    private String movementType; // IN, OUT, ADJUSTMENT_IN, ADJUSTMENT_OUT

    @Column(name = "causal_code", nullable = false, length = 50)
    private String causalCode; // PURCHASE_RECEIPT, SALES_DELIVERY, MANUAL_ADJUSTMENT_IN, MANUAL_ADJUSTMENT_OUT, DOCUMENT_CANCEL_RESTORE

    @Column(name = "quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_cost", precision = 15, scale = 4)
    private BigDecimal unitCost;

    @Column(name = "total_cost", precision = 15, scale = 4)
    private BigDecimal totalCost;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "reversed", nullable = false)
    private boolean reversed;

    @Column(name = "reversal_of_movement_id")
    private Long reversalOfMovementId;

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

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public String getCausalCode() {
        return causalCode;
    }

    public void setCausalCode(String causalCode) {
        this.causalCode = causalCode;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    public Long getReversalOfMovementId() {
        return reversalOfMovementId;
    }

    public void setReversalOfMovementId(Long reversalOfMovementId) {
        this.reversalOfMovementId = reversalOfMovementId;
    }
}