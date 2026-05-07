package com.gestiva.purchasing.receipt.web;

public class GoodsReceiptDetailLineView {

    private Integer lineNo;
    private String description;
    private String formattedQuantityReceived;

    public Integer getLineNo() {
        return lineNo;
    }

    public void setLineNo(Integer lineNo) {
        this.lineNo = lineNo;
    }    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }    public String getFormattedQuantityReceived() {
        return formattedQuantityReceived;
    }

    public void setFormattedQuantityReceived(String formattedQuantityReceived) {
        this.formattedQuantityReceived = formattedQuantityReceived;
    }
}