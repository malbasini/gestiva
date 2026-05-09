package com.gestiva.accounting.v2.journal.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JournalEntryForm {

    @NotNull(message = "La data scrittura è obbligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate entryDate;

    @NotBlank(message = "La causale è obbligatoria")
    private String causalCode;

    @NotBlank(message = "La descrizione è obbligatoria")
    private String description;

    @NotBlank(message = "La valuta è obbligatoria")
    private String currencyCode;

    private String notes;

    private List<JournalEntryLineForm> lines = new ArrayList<>();

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public String getCausalCode() {
        return causalCode;
    }

    public void setCausalCode(String causalCode) {
        this.causalCode = causalCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<JournalEntryLineForm> getLines() {
        return lines;
    }

    public void setLines(List<JournalEntryLineForm> lines) {
        this.lines = lines;
    }
}
