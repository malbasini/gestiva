package com.gestiva.inventory.movement.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class InventoryAdjustmentForm {

    @NotNull(message = "Seleziona un articolo.")
    private Long itemId;

    @NotBlank(message = "Seleziona il tipo rettifica.")
    private String adjustmentType;

    @NotNull(message = "Inserisci la data movimento.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate movementDate;

    @NotBlank(message = "Inserisci la quantità.")
    private String quantity;

    @Size(max = 1000, message = "Le note non possono superare 1000 caratteri.")
    private String notes;

    private String unitCost;

    public String getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(String unitCost) {
        this.unitCost = unitCost;
    }

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

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}