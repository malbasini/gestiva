package com.gestiva.purchasing.invoice.web;

import java.util.ArrayList;
import java.util.List;

public class SupplierInvoiceDetailView {

    private Long id;
    private String invoiceNumber;
    private String formattedInvoiceDate;
    private String supplierName;
    private String status;
    private String currencyCode;
    private String notes;
    private Long goodsReceiptId;
    private String goodsReceiptNumber;
    private Long purchaseOrderId;
    private String purchaseOrderNumber;
    private String formattedSubtotalAmount;
    private String formattedTaxAmount;
    private String formattedTotalAmount;
    private List<SupplierInvoiceDetailLineView> lines = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }    public String getFormattedInvoiceDate() {
        return formattedInvoiceDate;
    }

    public void setFormattedInvoiceDate(String formattedInvoiceDate) {
        this.formattedInvoiceDate = formattedInvoiceDate;
    }    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }    public Long getGoodsReceiptId() {
        return goodsReceiptId;
    }

    public void setGoodsReceiptId(Long goodsReceiptId) {
        this.goodsReceiptId = goodsReceiptId;
    }    public String getGoodsReceiptNumber() {
        return goodsReceiptNumber;
    }

    public void setGoodsReceiptNumber(String goodsReceiptNumber) {
        this.goodsReceiptNumber = goodsReceiptNumber;
    }    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Long purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }    public String getFormattedSubtotalAmount() {
        return formattedSubtotalAmount;
    }

    public void setFormattedSubtotalAmount(String formattedSubtotalAmount) {
        this.formattedSubtotalAmount = formattedSubtotalAmount;
    }    public String getFormattedTaxAmount() {
        return formattedTaxAmount;
    }

    public void setFormattedTaxAmount(String formattedTaxAmount) {
        this.formattedTaxAmount = formattedTaxAmount;
    }    public String getFormattedTotalAmount() {
        return formattedTotalAmount;
    }

    public void setFormattedTotalAmount(String formattedTotalAmount) {
        this.formattedTotalAmount = formattedTotalAmount;
    }    public List<SupplierInvoiceDetailLineView> getLines() {
        return lines;
    }

    public void setLines(List<SupplierInvoiceDetailLineView> lines) {
        this.lines = lines;
    }
}