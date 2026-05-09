package com.gestiva.accounting.entry.web;

import java.util.ArrayList;
import java.util.List;

public class AccountingEntryDetailView {

    private Long id;
    private String entryNumber;
    private String formattedEntryDate;
    private String causalCode;
    private String description;
    private String currencyCode;
    private String formattedTotalAmount;
    private String notes;
    private String referenceType;
    private Long referenceId;
    private List<AccountingEntryLineView> lines = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntryNumber() {
        return entryNumber;
    }

    public void setEntryNumber(String entryNumber) {
        this.entryNumber = entryNumber;
    }

    public String getFormattedEntryDate() {
        return formattedEntryDate;
    }

    public void setFormattedEntryDate(String formattedEntryDate) {
        this.formattedEntryDate = formattedEntryDate;
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

    public String getFormattedTotalAmount() {
        return formattedTotalAmount;
    }

    public void setFormattedTotalAmount(String formattedTotalAmount) {
        this.formattedTotalAmount = formattedTotalAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public List<AccountingEntryLineView> getLines() {
        return lines;
    }

    public void setLines(List<AccountingEntryLineView> lines) {
        this.lines = lines;
    }
}