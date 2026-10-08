package com.gestiva.warehouse.uom.entity;

import com.gestiva.common.model.TenantAwareEntity;
import com.gestiva.warehouse.item.entity.Item;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "item_uom",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_item_uom",
                        columnNames = {"tenant_id", "item_id", "uom_id"}
                )
        }
)
public class ItemUom extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uom_id", nullable = false)
    private UnitOfMeasure uom;

    @Column(
            name = "conversion_factor",
            nullable = false,
            precision = 19,
            scale = 6
    )
    private BigDecimal conversionFactor;

    @Column(name = "purchase_uom", nullable = false)
    private boolean purchaseUom;

    @Column(name = "sales_uom", nullable = false)
    private boolean salesUom;

    @Column(length = 100)
    private String barcode;

    @Column(nullable = false)
    private boolean active = true;

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public UnitOfMeasure getUom() {
        return uom;
    }

    public void setUom(UnitOfMeasure uom) {
        this.uom = uom;
    }

    public BigDecimal getConversionFactor() {
        return conversionFactor;
    }

    public void setConversionFactor(BigDecimal conversionFactor) {
        this.conversionFactor = conversionFactor;
    }

    public boolean isPurchaseUom() {
        return purchaseUom;
    }

    public void setPurchaseUom(boolean purchaseUom) {
        this.purchaseUom = purchaseUom;
    }

    public boolean isSalesUom() {
        return salesUom;
    }

    public void setSalesUom(boolean salesUom) {
        this.salesUom = salesUom;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}