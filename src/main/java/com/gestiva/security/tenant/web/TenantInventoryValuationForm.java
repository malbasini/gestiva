package com.gestiva.security.tenant.web;

import jakarta.validation.constraints.NotBlank;

public class TenantInventoryValuationForm {

    @NotBlank(message = "Seleziona un metodo di valorizzazione.")
    private String inventoryValuationMethod;

    public String getInventoryValuationMethod() {
        return inventoryValuationMethod;
    }

    public void setInventoryValuationMethod(String inventoryValuationMethod) {
        this.inventoryValuationMethod = inventoryValuationMethod;
    }
}