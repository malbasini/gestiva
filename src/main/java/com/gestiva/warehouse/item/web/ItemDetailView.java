package com.gestiva.warehouse.item.web;

public class ItemDetailView {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String itemType;
    private String unitOfMeasure;
    private boolean active;
    private boolean trackStock;
    private String formattedBasePrice;
    private String formattedDefaultTaxPct;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }    public boolean isTrackStock() {
        return trackStock;
    }

    public void setTrackStock(boolean trackStock) {
        this.trackStock = trackStock;
    }    public String getFormattedBasePrice() {
        return formattedBasePrice;
    }

    public void setFormattedBasePrice(String formattedBasePrice) {
        this.formattedBasePrice = formattedBasePrice;
    }    public String getFormattedDefaultTaxPct() {
        return formattedDefaultTaxPct;
    }

    public void setFormattedDefaultTaxPct(String formattedDefaultTaxPct) {
        this.formattedDefaultTaxPct = formattedDefaultTaxPct;
    }
}