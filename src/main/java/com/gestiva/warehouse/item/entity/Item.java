package com.gestiva.warehouse.item.entity;

import com.gestiva.common.model.TenantAwareEntity;
import com.gestiva.warehouse.uom.entity.UnitOfMeasure;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "item",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_item_tenant_code",
                        columnNames = {"tenant_id", "code"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_item_tenant_active",
                        columnList = "tenant_id, active"
                ),
                @Index(
                        name = "idx_item_tenant_type",
                        columnList = "tenant_id, item_type"
                ),
                @Index(
                        name = "idx_item_tenant_barcode",
                        columnList = "tenant_id, barcode"
                )
        }
)
public class Item extends TenantAwareEntity {

    @Column(name = "code", nullable = false, length = 60)
    private String code;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "name", nullable = false, length = 180)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private ItemType itemType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "base_uom_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_base_uom")
    )
    private UnitOfMeasure baseUom;

    @Enumerated(EnumType.STRING)
    @Column(name = "tracking_type", nullable = false, length = 20)
    private InventoryTrackingType trackingType = InventoryTrackingType.NONE;

    @Column(name = "track_expiration", nullable = false)
    private boolean trackExpiration = false;

    @Column(name = "base_price", precision = 15, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "default_tax_pct", precision = 6, scale = 2)
    private BigDecimal defaultTaxPct;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Transient
    public boolean isTrackStock() {
        return ItemType.PRODUCT.equals(itemType);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public UnitOfMeasure getBaseUom() {
        return baseUom;
    }

    public void setBaseUom(UnitOfMeasure baseUom) {
        this.baseUom = baseUom;
    }

    public InventoryTrackingType getTrackingType() {
        return trackingType;
    }

    public void setTrackingType(InventoryTrackingType trackingType) {
        this.trackingType = trackingType;
    }

    public boolean isTrackExpiration() {
        return trackExpiration;
    }

    public void setTrackExpiration(boolean trackExpiration) {
        this.trackExpiration = trackExpiration;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getDefaultTaxPct() {
        return defaultTaxPct;
    }

    public void setDefaultTaxPct(BigDecimal defaultTaxPct) {
        this.defaultTaxPct = defaultTaxPct;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}