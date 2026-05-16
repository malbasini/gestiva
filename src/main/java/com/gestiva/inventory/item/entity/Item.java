package com.gestiva.inventory.item.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Table(
        name = "item",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_item_tenant_code", columnNames = {"tenant_id", "code"})
        }
)
public class Item extends TenantAwareEntity {

    @Column(name = "code", nullable = false, length = 60)
    private String code;

    @Column(name = "name", nullable = false, length = 180)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "item_type", nullable = false, length = 20)
    private String itemType; // PRODUCT, SERVICE

    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private String unitOfMeasure; // pz, kg, h, etc.

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "track_stock", nullable = false)
    private boolean trackStock;

    @Column(name = "base_price", precision = 15, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "default_tax_pct", precision = 6, scale = 2)
    private BigDecimal defaultTaxPct;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isTrackStock() {
        return trackStock;
    }

    public void setTrackStock(boolean trackStock) {
        this.trackStock = trackStock;
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
}
