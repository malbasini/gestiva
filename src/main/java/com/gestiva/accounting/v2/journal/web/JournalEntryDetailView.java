package com.gestiva.accounting.v2.journal.web;

import java.util.ArrayList;
import java.util.List;

public class JournalEntryDetailView {

    private Long id;
    private String entryNumber;
    private String formattedEntryDate;
    private String causalCode;
    private String description;
    private String currencyCode;
    private String formattedTotalDebit;
    private String formattedTotalCredit;
    private boolean posted;
    private String notes;
    private String referenceType;
    private Long referenceId;
    private List<JournalEntryDetailLineView> lines = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }    public String getEntryNumber() {
        return entryNumber;
    }

    public void setEntryNumber(String entryNumber) {
        this.entryNumber = entryNumber;
    }    public String getFormattedEntryDate() {
        return formattedEntryDate;
    }

    public void setFormattedEntryDate(String formattedEntryDate) {
        this.formattedEntryDate = formattedEntryDate;
    }    public String getCausalCode() {
        return causalCode;
    }

    public void setCausalCode(String causalCode) {
        this.causalCode = causalCode;
    }    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }    public String getFormattedTotalDebit() {
        return formattedTotalDebit;
    }

    public void setFormattedTotalDebit(String formattedTotalDebit) {
        this.formattedTotalDebit = formattedTotalDebit;
    }    public String getFormattedTotalCredit() {
        return formattedTotalCredit;
    }

    public void setFormattedTotalCredit(String formattedTotalCredit) {
        this.formattedTotalCredit = formattedTotalCredit;
    }    public boolean isPosted() {
        return posted;
    }

    public void setPosted(boolean posted) {
        this.posted = posted;
    }    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }    public List<JournalEntryDetailLineView> getLines() {
        return lines;
    }

    public void setLines(List<JournalEntryDetailLineView> lines) {
        this.lines = lines;
    }
}