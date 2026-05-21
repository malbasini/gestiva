package com.gestiva.sales.quote.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class QuoteLineForm {

    @NotBlank(message = "La descrizione è obbligatoria")
    @Size(max = 255, message = "La descrizione non può superare 255 caratteri")
    private String description;

    @NotNull(message = "La quantità è obbligatoria")
    private String quantity;

    @NotNull(message = "Il prezzo unitario è obbligatorio")
    private String unitPrice;

    @NotNull(message = "Lo sconto è obbligatorio")
    private String discountPct;

    @NotNull(message = "L'IVA è obbligatoria")
    private String taxPct;

    private Long itemId;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getDiscountPct() {
        return discountPct;
    }

    public void setDiscountPct(String discountPct) {
        this.discountPct = discountPct;
    }

    public String getTaxPct() {
        return taxPct;
    }

    public void setTaxPct(String taxPct) {
        this.taxPct = taxPct;
    }
}