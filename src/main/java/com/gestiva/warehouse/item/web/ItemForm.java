package com.gestiva.warehouse.item.web;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

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
    private String itemType; // PRODUCT, SERVICE

    @NotBlank(message = "L'unità di misura è obbligatoria")
    @Size(max = 20, message = "L'unità di misura non può superare 20 caratteri")
    private String unitOfMeasure;

    private boolean active = true;

    private boolean trackStock = true;

    @DecimalMin(value = "0.00", inclusive = true, message = "Il prezzo base non può essere negativo")
    private BigDecimal basePrice;

    @DecimalMin(value = "0.00", inclusive = true, message = "L'IVA non può essere negativa")
    @DecimalMax(value = "100.00", inclusive = true, message = "L'IVA non può superare 100")
    private BigDecimal defaultTaxPct;

    public String getCode() {
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
    }    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }    public BigDecimal getDefaultTaxPct() {
        return defaultTaxPct;
    }

    public void setDefaultTaxPct(BigDecimal defaultTaxPct) {
        this.defaultTaxPct = defaultTaxPct;
    }
}