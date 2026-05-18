package com.gestiva.inventory.stock.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class StockMovementForm {

    @NotNull(message = "La data movimento è obbligatoria")
    private LocalDate movementDate;

    @NotBlank(message = "La direzione è obbligatoria")
    private String direction; // IN, OUT

    @NotBlank(message = "La causale è obbligatoria")
    private String reasonCode; // MANUAL_LOAD, MANUAL_UNLOAD

    @NotNull(message = "La quantità è obbligatoria")
    @DecimalMin(value = "0.001", inclusive = true, message = "La quantità deve essere maggiore di zero")
    private BigDecimal quantity;

    private String notes;

    private BigDecimal unitCost;

    public LocalDate getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(LocalDate movementDate) {
        this.movementDate = movementDate;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
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

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }
}