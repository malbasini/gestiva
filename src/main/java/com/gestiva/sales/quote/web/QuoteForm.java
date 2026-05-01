package com.gestiva.sales.quote.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class QuoteForm {

    @NotNull(message = "Il cliente è obbligatorio")
    private Long customerId;

    @NotBlank(message = "Lo stato è obbligatorio")
    private String status;

    @NotBlank(message = "La valuta è obbligatoria")
    @Size(max = 10, message = "La valuta non può superare 10 caratteri")
    private String currencyCode;

    @Size(max = 2000, message = "Le note non possono superare 2000 caratteri")
    private String notes;

    @Valid
    private List<QuoteLineForm> lines = new ArrayList<>();

    @NotNull(message = "La data preventivo è obbligatoria")
    @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
    private LocalDate quoteDate;

    @NotNull(message = "La data di validità è obbligatoria")
    @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
    private LocalDate validUntil;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public LocalDate getQuoteDate() {
        return quoteDate;
    }

    public void setQuoteDate(LocalDate quoteDate) {
        this.quoteDate = quoteDate;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public List<QuoteLineForm> getLines() {
        return lines;
    }

    public void setLines(List<QuoteLineForm> lines) {
        this.lines = lines;
    }
}