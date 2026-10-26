package com.gestiva.warehouse.item.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ItemUomForm {

    @NotNull(message = "Seleziona un'unità di misura alternativa.")
    private Long uomId;

    @NotNull(message = "Il fattore di conversione è obbligatorio.")
    @DecimalMin(
            value = "0.000000",
            inclusive = false,
            message = "Il fattore di conversione deve essere positivo."
    )
    @Digits(
            integer = 13,
            fraction = 6,
            message = "Il fattore deve avere al massimo 13 cifre intere e 6 decimali."
    )
    private BigDecimal conversionFactor;

    private boolean purchaseUom = false;

    private boolean salesUom = false;

    @Size(
            max = 100,
            message = "Il barcode non può superare 100 caratteri."
    )
    private String barcode;

    public Long getUomId() {
        return uomId;
    }

    public void setUomId(Long uomId) {
        this.uomId = uomId;
    }

    public BigDecimal getConversionFactor() {
        return conversionFactor;
    }

    public void setConversionFactor(BigDecimal conversionFactor) {
        this.conversionFactor = conversionFactor;
    }

    public boolean isPurchaseUom() {
        return purchaseUom;
    }

    public void setPurchaseUom(boolean purchaseUom) {
        this.purchaseUom = purchaseUom;
    }

    public boolean isSalesUom() {
        return salesUom;
    }

    public void setSalesUom(boolean salesUom) {
        this.salesUom = salesUom;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}
