package com.gestiva.warehouse.item.web;

import java.math.BigDecimal;

/**
 * Unità di misura disponibile per gli acquisti.
 *
 * Il fattore è informativo per l'interfaccia.
 * Il backend deve sempre verificare la conversione
 * prima di salvare una riga ordine.
 */
public class PurchaseUomOptionView {

    private Long uomId;
    private String code;
    private String name;
    private BigDecimal conversionFactor;
    private int decimalPlaces;

    public Long getUomId() {
        return uomId;
    }

    public void setUomId(Long uomId) {
        this.uomId = uomId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getConversionFactor() {
        return conversionFactor;
    }

    public void setConversionFactor(BigDecimal conversionFactor) {
        this.conversionFactor = conversionFactor;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }
}
