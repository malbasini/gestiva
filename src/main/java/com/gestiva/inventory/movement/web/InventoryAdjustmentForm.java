package com.gestiva.inventory.movement.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InventoryAdjustmentForm {

    @NotNull(message = "Seleziona un articolo.")
    private Long itemId;

    @NotNull(message = "Seleziona il tipo rettifica.")
    private String adjustmentType; // IN, OUT

    @NotNull(message = "Inserisci la data movimento.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate movementDate;

    @NotNull(message = "Inserisci la quantità.")
    @DecimalMin(value = "0.001", message = "La quantità deve essere maggiore di zero.")
    private BigDecimal quantity;

    @Size(max = 1000, message = "Le note non possono superare 1000 caratteri.")
    private String notes;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(String adjustmentType) {
        this.adjustmentType = adjustmentType;
    }

    public LocalDate getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(LocalDate movementDate) {
        this.movementDate = movementDate;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
