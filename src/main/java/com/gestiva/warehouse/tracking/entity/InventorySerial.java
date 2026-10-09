package com.gestiva.warehouse.tracking.entity;

import com.gestiva.common.model.TenantAwareEntity;
import com.gestiva.warehouse.item.entity.Item;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "inventory_serial",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventory_serial_item_number",
                        columnNames = {
                                "tenant_id",
                                "item_id",
                                "serial_number"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_inventory_serial_item",
                        columnList = "tenant_id, item_id"
                ),
                @Index(
                        name = "idx_inventory_serial_expiration",
                        columnList = "tenant_id, expiration_date"
                )
        }
)
public class InventorySerial extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "item_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_inventory_serial_item"
            )
    )
    private Item item;

    @Column(
            name = "serial_number",
            nullable = false,
            length = 150
    )
    private String serialNumber;

    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;

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

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public LocalDate getManufacturingDate() {
        return manufacturingDate;
    }

    public void setManufacturingDate(LocalDate manufacturingDate) {
        this.manufacturingDate = manufacturingDate;
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