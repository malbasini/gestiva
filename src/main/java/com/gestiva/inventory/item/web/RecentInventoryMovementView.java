package com.gestiva.inventory.item.web;

public class RecentInventoryMovementView {

    private String formattedMovementDate;
    private String movementType;
    private String movementTypeLabel;
    private String causalCode;
    private String causalCodeLabel;
    private String formattedQuantity;
    private String referenceType;
    private String notes;
    private boolean inbound;

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

    public String getMovementTypeLabel() {
        return movementTypeLabel;
    }

    public void setMovementTypeLabel(String movementTypeLabel) {
        this.movementTypeLabel = movementTypeLabel;
    }

    public String getCausalCode() {
        return causalCode;
    }

    public void setCausalCode(String causalCode) {
        this.causalCode = causalCode;
    }

    public String getCausalCodeLabel() {
        return causalCodeLabel;
    }

    public void setCausalCodeLabel(String causalCodeLabel) {
        this.causalCodeLabel = causalCodeLabel;
    }

    public String getFormattedQuantity() {
        return formattedQuantity;
    }

    public void setFormattedQuantity(String formattedQuantity) {
        this.formattedQuantity = formattedQuantity;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isInbound() {
        return inbound;
    }

    public void setInbound(boolean inbound) {
        this.inbound = inbound;
    }
}