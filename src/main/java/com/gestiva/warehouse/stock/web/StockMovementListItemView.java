package com.gestiva.warehouse.stock.web;

public class StockMovementListItemView {

    private Long id;
    private String formattedMovementDate;
    private String direction;
    private String reasonCode;
    private String formattedQuantity;
    private String notes;
    private String referenceType;
    private Long referenceId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFormattedMovementDate() {
        return formattedMovementDate;
    }

    public void setFormattedMovementDate(String formattedMovementDate) {
        this.formattedMovementDate = formattedMovementDate;
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

    public String getFormattedQuantity() {
        return formattedQuantity;
    }

    public void setFormattedQuantity(String formattedQuantity) {
        this.formattedQuantity = formattedQuantity;
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
}
