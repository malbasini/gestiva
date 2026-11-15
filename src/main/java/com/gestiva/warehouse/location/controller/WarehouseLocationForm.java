package com.gestiva.warehouse.location.controller;

import com.gestiva.warehouse.location.entity.WarehouseLocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class WarehouseLocationForm {

    @NotNull
    private Long warehouseId;

    private Long parentLocationId;

    @NotBlank
    @Size(max = 50)
    private String code;

    @NotBlank
    @Size(max = 180)
    private String name;

    @NotNull
    private WarehouseLocationType locationType =
            WarehouseLocationType.STORAGE;

    @Size(max = 100)
    private String barcode;

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Long getParentLocationId() {
        return parentLocationId;
    }

    public void setParentLocationId(Long parentLocationId) {
        this.parentLocationId = parentLocationId;
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

    public void setLocationType(
            WarehouseLocationType locationType) {
        this.locationType = locationType;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}
