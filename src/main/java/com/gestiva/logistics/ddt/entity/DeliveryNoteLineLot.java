package com.gestiva.logistics.ddt.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "delivery_note_line_lot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ddt_line_lot",
                        columnNames = {"delivery_note_line_id", "lot_id"}
                ),
                @UniqueConstraint(
                        name = "uk_ddt_line_lot_movement",
                        columnNames = {"movement_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_ddt_line_lot_trace",
                        columnList = "tenant_id,item_id,lot_id,company_id"
                )
        }
)
public class DeliveryNoteLineLot extends TenantAwareEntity {

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "delivery_note_line_id", nullable = false)
    private Long deliveryNoteLineId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "lot_id", nullable = false)
    private Long lotId;

    @Column(name = "quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "movement_id")
    private Long movementId;

    public DeliveryNoteLineLot() {
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getDeliveryNoteLineId() {
        return deliveryNoteLineId;
    }

    public void setDeliveryNoteLineId(Long deliveryNoteLineId) {
        this.deliveryNoteLineId = deliveryNoteLineId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getLotId() {
        return lotId;
    }

    public void setLotId(Long lotId) {
        this.lotId = lotId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Long getMovementId() {
        return movementId;
    }

    public void setMovementId(Long movementId) {
        this.movementId = movementId;
    }
}
