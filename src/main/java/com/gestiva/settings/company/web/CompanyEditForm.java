package com.gestiva.settings.company.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CompanyEditForm {

    @NotBlank(message = "La ragione sociale è obbligatoria.")
    @Size(max = 180, message = "La ragione sociale non può superare 180 caratteri.")
    private String legalName;

    private boolean active;

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
