package com.gestiva.warehouse.location.entity;

import com.gestiva.common.model.TenantAwareEntity;
import com.gestiva.platform.company.entity.Company;
import jakarta.persistence.*;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "warehouse",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_warehouse_company_code",
                        columnNames = {
                                "tenant_id",
                                "company_id",
                                "code"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_warehouse_company_active",
                        columnList = "tenant_id, company_id, active"
                )
        }
)
public class Warehouse extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "company_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_warehouse_company"
            )
    )
    private Company company;

    @Column(
            name = "code",
            nullable = false,
            length = 30
    )
    private String code;

    @Column(
            name = "name",
            nullable = false,
            length = 180
    )
    private String name;

    @Column(
            name = "description",
            length = 500
    )
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "warehouse_type",
            nullable = false,
            length = 30
    )
    private WarehouseType warehouseType = WarehouseType.PHYSICAL;

    @Column(
            name = "allow_negative_stock",
            nullable = false
    )
    private boolean allowNegativeStock = false;

    @Column(
            name = "active",
            nullable = false
    )
    private boolean active = true;

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public WarehouseType getWarehouseType() {
        return warehouseType;
    }

    public void setWarehouseType(WarehouseType warehouseType) {
        this.warehouseType = warehouseType;
    }

    public boolean isAllowNegativeStock() {
        return allowNegativeStock;
    }

    public void setAllowNegativeStock(boolean allowNegativeStock) {
        this.allowNegativeStock = allowNegativeStock;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}