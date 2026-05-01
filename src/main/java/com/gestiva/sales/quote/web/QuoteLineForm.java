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
    @DecimalMin(value = "0.001", message = "La quantità deve essere maggiore di zero")
    private BigDecimal quantity;

    @NotNull(message = "Il prezzo unitario è obbligatorio")
    @DecimalMin(value = "0.00", inclusive = true, message = "Il prezzo unitario non può essere negativo")
    private BigDecimal unitPrice;

    @NotNull(message = "Lo sconto è obbligatorio")
    @DecimalMin(value = "0.00", inclusive = true, message = "Lo sconto non può essere negativo")
    private BigDecimal discountPct;

    @NotNull(message = "L'IVA è obbligatoria")
    @DecimalMin(value = "0.00", inclusive = true, message = "L'IVA non può essere negativa")
    private BigDecimal taxPct;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getDiscountPct() {
        return discountPct;
    }

    public void setDiscountPct(BigDecimal discountPct) {
        this.discountPct = discountPct;
    }

    public BigDecimal getTaxPct() {
        return taxPct;
    }

    public void setTaxPct(BigDecimal taxPct) {
        this.taxPct = taxPct;
    }
}