package com.gestiva.accounting.vat.web;

public class VatSettlementView {

    private String formattedDateFrom;
    private String formattedDateTo;

    private String formattedSalesTaxableAmount;
    private String formattedSalesTaxAmount;

    private String formattedPurchaseTaxableAmount;
    private String formattedPurchaseTaxAmount;

    private String formattedVatBalance;
    private String balanceTypeLabel;

    public String getFormattedDateFrom() {
        return formattedDateFrom;
    }

    public void setFormattedDateFrom(String formattedDateFrom) {
        this.formattedDateFrom = formattedDateFrom;
    }

    public String getFormattedDateTo() {
        return formattedDateTo;
    }

    public void setFormattedDateTo(String formattedDateTo) {
        this.formattedDateTo = formattedDateTo;
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