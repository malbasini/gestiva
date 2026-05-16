package com.gestiva.inventory.movement.web;

public class ItemInventoryLedgerLineView {

    private Long movementId;
    private String formattedMovementDate;
    private String movementType;
    private String causalCode;
    private String referenceLabel;
    private String formattedQtyIn;
    private String formattedQtyOut;
    private String formattedRunningBalance;
    private String notes;

    public Long getMovementId() {
        return movementId;
    }

    public void setMovementId(Long movementId) {
        this.movementId = movementId;
    }

    public String getFormattedMovementDate() {
        return formattedMovementDate;
    }

    public void setFormattedMovementDate(String formattedMovementDate) {
        this.formattedMovementDate = formattedMovementDate;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public String getCausalCode() {
        return causalCode;
    }

    public void setCausalCode(String causalCode) {
        this.causalCode = causalCode;
    }

    public String getReferenceLabel() {
        return referenceLabel;
    }

    public void setReferenceLabel(String referenceLabel) {
        this.referenceLabel = referenceLabel;
    }

    public String getFormattedQtyIn() {
        return formattedQtyIn;
    }

    public void setFormattedQtyIn(String formattedQtyIn) {
        this.formattedQtyIn = formattedQtyIn;
    }

    public String getFormattedQtyOut() {
        return formattedQtyOut;
    }

    public void setFormattedQtyOut(String formattedQtyOut) {
        this.formattedQtyOut = formattedQtyOut;
    }

    public String getFormattedRunningBalance() {
        return formattedRunningBalance;
    }

    public void setFormattedRunningBalance(String formattedRunningBalance) {
        this.formattedRunningBalance = formattedRunningBalance;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}