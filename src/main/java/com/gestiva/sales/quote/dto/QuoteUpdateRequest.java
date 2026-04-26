package com.gestiva.sales.quote.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public class QuoteUpdateRequest {

    @NotNull
    private Long customerId;

    @NotNull
    private LocalDate quoteDate;

    @NotNull
    private LocalDate validUntil;

    @Size(max = 30)
    private String status;

    @Size(max = 3)
    private String currencyCode;

    @Size(max = 4000)
    private String notes;

    @Valid
    @NotNull
    private List<QuoteLineRequest> lines;

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

    public List<QuoteLineRequest> getLines() {
        return lines;
    }

    public void setLines(List<QuoteLineRequest> lines) {
        this.lines = lines;
    }
}