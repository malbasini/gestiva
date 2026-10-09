package com.gestiva.warehouse.location.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.*;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "warehouse_location",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_warehouse_location_code",
                        columnNames = {
                                "tenant_id",
                                "warehouse_id",
                                "code"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_warehouse_location_parent",
                        columnList = "tenant_id, warehouse_id, parent_location_id"
                ),
                @Index(
                        name = "idx_warehouse_location_active",
                        columnList = "tenant_id, warehouse_id, active"
                )
        }
)
public class WarehouseLocation extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "warehouse_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_location_warehouse"
            )
    )
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "parent_location_id",
            foreignKey = @ForeignKey(
                    name = "fk_location_parent"
            )
    )
    private WarehouseLocation parentLocation;

    @Column(
            name = "code",
            nullable = false,
            length = 50
    )
    private String code;

    @Column(
            name = "name",
            nullable = false,
            length = 180
    )
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "location_type",
            nullable = false,
            length = 30
    )
    private WarehouseLocationType locationType =
            WarehouseLocationType.STORAGE;

    @Column(
            name = "barcode",
            length = 100
    )
    private String barcode;

    @Column(
            name = "active",
            nullable = false
    )
    private boolean active = true;

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public WarehouseLocation getParentLocation() {
        return parentLocation;
    }

    public void setParentLocation(WarehouseLocation parentLocation) {
        this.parentLocation = parentLocation;
    }

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

    public WarehouseLocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(WarehouseLocationType locationType) {
        this.locationType = locationType;
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