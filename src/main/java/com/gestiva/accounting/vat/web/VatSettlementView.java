package com.gestiva.accounting.vat.web;

public class VatSettlementView {

    private Integer year;
    private Integer month;

    private String formattedSalesTaxableAmount;
    private String formattedSalesTaxAmount;

    private String formattedPurchaseTaxableAmount;
    private String formattedPurchaseTaxAmount;

    private String formattedVatBalance;
    private String balanceTypeLabel; // A DEBITO / A CREDITO / ZERO

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public String getFormattedSalesTaxableAmount() {
        return formattedSalesTaxableAmount;
    }

    public void setFormattedSalesTaxableAmount(String formattedSalesTaxableAmount) {
        this.formattedSalesTaxableAmount = formattedSalesTaxableAmount;
    }

    public String getFormattedSalesTaxAmount() {
        return formattedSalesTaxAmount;
    }

    public void setFormattedSalesTaxAmount(String formattedSalesTaxAmount) {
        this.formattedSalesTaxAmount = formattedSalesTaxAmount;
    }

    public String getFormattedPurchaseTaxableAmount() {
        return formattedPurchaseTaxableAmount;
    }

    public void setFormattedPurchaseTaxableAmount(String formattedPurchaseTaxableAmount) {
        this.formattedPurchaseTaxableAmount = formattedPurchaseTaxableAmount;
    }

    public String getFormattedPurchaseTaxAmount() {
        return formattedPurchaseTaxAmount;
    }

    public void setFormattedPurchaseTaxAmount(String formattedPurchaseTaxAmount) {
        this.formattedPurchaseTaxAmount = formattedPurchaseTaxAmount;
    }

    public String getFormattedVatBalance() {
        return formattedVatBalance;
    }

    public void setFormattedVatBalance(String formattedVatBalance) {
        this.formattedVatBalance = formattedVatBalance;
    }

    public String getBalanceTypeLabel() {
        return balanceTypeLabel;
    }

    public void setBalanceTypeLabel(String balanceTypeLabel) {
        this.balanceTypeLabel = balanceTypeLabel;
    }
}