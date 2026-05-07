package com.gestiva.purchasing.receipt.web;

import java.util.ArrayList;
import java.util.List;

public class GoodsReceiptDetailView {

    private Long id;
    private String receiptNumber;
    private String formattedReceiptDate;
    private String supplierName;
    private String purchaseOrderNumber;
    private String notes;
    private List<GoodsReceiptDetailLineView> lines = new ArrayList<>();

    private Long purchaseOrderId;

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Long purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }    public String getFormattedReceiptDate() {
        return formattedReceiptDate;
    }

    public void setFormattedReceiptDate(String formattedReceiptDate) {
        this.formattedReceiptDate = formattedReceiptDate;
    }    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }    public List<GoodsReceiptDetailLineView> getLines() {
        return lines;
    }

    public void setLines(List<GoodsReceiptDetailLineView> lines) {
        this.lines = lines;
    }
}