package com.gestiva.purchasing.order.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderForm {

    private Long supplierId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @NotNull(message = "La data ordine è obbligatoria")
    private LocalDate orderDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expectedDeliveryDate;

    @NotBlank(message = "Lo stato è obbligatorio")
    private String status;

    @NotBlank(message = "La valuta è obbligatoria")
    private String currencyCode;

    private String notes;
    
    private List<PurchaseOrderLineForm> lines = new ArrayList<>();

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
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

    public List<PurchaseOrderLineForm> getLines() {
        return lines;
    }

    public void setLines(List<PurchaseOrderLineForm> lines) {
        this.lines = lines;
    }
}
