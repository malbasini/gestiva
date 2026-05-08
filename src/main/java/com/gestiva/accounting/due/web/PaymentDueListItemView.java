package com.gestiva.accounting.due.web;

public class PaymentDueListItemView {

    private Long id;
    private String direction;
    private String partyName;
    private String documentNumber;
    private String formattedDocumentDate;
    private String formattedDueDate;
    private String formattedGrossAmount;
    private String formattedPaidAmount;
    private String formattedOpenAmount;
    private String status;
    private boolean overdue;
    private String referenceType;
    private Long referenceId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getFormattedDocumentDate() {
        return formattedDocumentDate;
    }

    public void setFormattedDocumentDate(String formattedDocumentDate) {
        this.formattedDocumentDate = formattedDocumentDate;
    }

    public String getFormattedDueDate() {
        return formattedDueDate;
    }

    public void setFormattedDueDate(String formattedDueDate) {
        this.formattedDueDate = formattedDueDate;
    }

    public String getFormattedGrossAmount() {
        return formattedGrossAmount;
    }

    public void setFormattedGrossAmount(String formattedGrossAmount) {
        this.formattedGrossAmount = formattedGrossAmount;
    }

    public String getFormattedPaidAmount() {
        return formattedPaidAmount;
    }

    public void setFormattedPaidAmount(String formattedPaidAmount) {
        this.formattedPaidAmount = formattedPaidAmount;
    }

    public String getFormattedOpenAmount() {
        return formattedOpenAmount;
    }

    public void setFormattedOpenAmount(String formattedOpenAmount) {
        this.formattedOpenAmount = formattedOpenAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
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
}
