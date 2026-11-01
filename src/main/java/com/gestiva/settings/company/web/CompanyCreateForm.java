package com.gestiva.settings.company.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CompanyCreateForm {

    @NotBlank(message = "Il codice società è obbligatorio.")
    @Size(max = 30, message = "Il codice non può superare 30 caratteri.")
    private String code;

    @NotBlank(message = "La ragione sociale è obbligatoria.")
    @Size(max = 180, message = "La ragione sociale non può superare 180 caratteri.")
    private String legalName;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }
}
