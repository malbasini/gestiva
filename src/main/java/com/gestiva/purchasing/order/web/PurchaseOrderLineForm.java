package com.gestiva.purchasing.order.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class PurchaseOrderLineForm {

    private Long itemId;

    @NotBlank(message = "La descrizione è obbligatoria")
    private String description;

    @NotNull(message = "La quantità è obbligatoria")
    @DecimalMin(value = "0.001", inclusive = true, message = "La quantità deve essere maggiore di zero")
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
