package com.gestiva.accounting.entry.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AccountingEntryManualForm {

    @NotNull(message = "La data registrazione è obbligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate entryDate;

    @NotBlank(message = "Il tipo movimento è obbligatorio")
    private String movementType; // MANUAL_INCOME, MANUAL_EXPENSE

    @NotBlank(message = "La descrizione è obbligatoria")
    private String description;

    @NotNull(message = "L'importo è obbligatorio")
    @DecimalMin(value = "0.01", inclusive = true, message = "L'importo deve essere maggiore di zero")
    private BigDecimal amount;

    @NotBlank(message = "La valuta è obbligatoria")
    private String currencyCode;

    private String notes;

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}