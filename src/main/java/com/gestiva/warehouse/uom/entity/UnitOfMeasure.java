package com.gestiva.warehouse.uom.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.*;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "unit_of_measure",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_uom_tenant_code",
                        columnNames = {"tenant_id", "code"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_uom_tenant_active",
                        columnList = "tenant_id, active"
                ),
                @Index(
                        name = "idx_uom_tenant_dimension",
                        columnList = "tenant_id, dimension"
                )
        }
)
public class UnitOfMeasure extends TenantAwareEntity {

    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "dimension", nullable = false, length = 30)
    private UnitOfMeasureDimension dimension;

    @Column(name = "decimal_places", nullable = false)
    private int decimalPlaces = 0;

    @Column(name = "active", nullable = false)
    private boolean active = true;

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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public UnitOfMeasureDimension getDimension() {
        return dimension;
    }

    public void setDimension(UnitOfMeasureDimension dimension) {
        this.dimension = dimension;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}