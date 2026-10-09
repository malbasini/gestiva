package com.gestiva.warehouse.tracking.entity;

import com.gestiva.common.model.TenantAwareEntity;
import com.gestiva.warehouse.item.entity.Item;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "inventory_lot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventory_lot_item_code",
                        columnNames = {
                                "tenant_id",
                                "item_id",
                                "lot_code"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_inventory_lot_item",
                        columnList = "tenant_id, item_id"
                ),
                @Index(
                        name = "idx_inventory_lot_expiration",
                        columnList = "tenant_id, expiration_date"
                )
        }
)
public class InventoryLot extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "item_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_inventory_lot_item"
            )
    )
    private Item item;

    @Column(
            name = "lot_code",
            nullable = false,
            length = 100
    )
    private String lotCode;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private boolean active = true;

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getLotCode() {
        return lotCode;
    }

    public void setLotCode(String lotCode) {
        this.lotCode = lotCode;
    }

    public LocalDate getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(LocalDate productionDate) {
        this.productionDate = productionDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}