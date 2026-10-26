package com.gestiva.warehouse.item.web;

import java.math.BigDecimal;

public class ItemAutocompleteView {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String unitOfMeasure;
    private Long baseUomId;
    private String itemType;
    private BigDecimal basePrice;
    private BigDecimal purchasePrice;
    private BigDecimal defaultTaxPct;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public Long getBaseUomId() {
        return baseUomId;
    }

    public void setBaseUomId(Long baseUomId) {
        this.baseUomId = baseUomId;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getDefaultTaxPct() {
        return defaultTaxPct;
    }

    public void setDefaultTaxPct(BigDecimal defaultTaxPct) {
        this.defaultTaxPct = defaultTaxPct;
    }
}
