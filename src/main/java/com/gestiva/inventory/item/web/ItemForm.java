package com.gestiva.inventory.item.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ItemForm {

    @NotBlank(message = "Il codice è obbligatorio")
    @Size(max = 60, message = "Il codice non può superare 60 caratteri")
    private String code;

    @NotBlank(message = "Il nome è obbligatorio")
    @Size(max = 180, message = "Il nome non può superare 180 caratteri")
    private String name;

    @Size(max = 500, message = "La descrizione non può superare 500 caratteri")
    private String description;

    @NotBlank(message = "Il tipo articolo è obbligatorio")
    private String itemType;

    @NotBlank(message = "L'unità di misura è obbligatoria")
    @Size(max = 20, message = "L'unità di misura non può superare 20 caratteri")
    private String unitOfMeasure;

    private boolean active = true;

    private boolean trackStock = true;

    @NotBlank(message = "Il prezzo base è obbligatorio")
    private String basePrice;

    @NotBlank(message = "L'IVA è obbligatoria")
    private String defaultTaxPct;

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

    public String getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(String basePrice) {
        this.basePrice = basePrice;
    }

    public String getDefaultTaxPct() {
        return defaultTaxPct;
    }

    public void setDefaultTaxPct(String defaultTaxPct) {
        this.defaultTaxPct = defaultTaxPct;
    }
}