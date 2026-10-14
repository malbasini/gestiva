package com.gestiva.settings.company.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public class DocumentSequenceForm {

    @NotBlank
    private String documentType;

    @Size(max = 20)
    private String prefix;

    @NotNull(message = "Il numero progressivo è obbligatorio.")
    @Min(value = 1, message = "Il numero progressivo deve essere almeno 1.")
    private Long nextNumber;

    @NotNull(message = "La lunghezza del numeratore è obbligatoria.")
    @Min(value = 1, message = "La lunghezza minima è 1.")
    @Max(value = 10, message = "La lunghezza massima è 10.")
    private Integer paddingSize;

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Long getNextNumber() {
        return nextNumber;
    }

    public void setNextNumber(Long nextNumber) {
        this.nextNumber = nextNumber;
    }

    public Integer getPaddingSize() {
        return paddingSize;
    }

    public void setPaddingSize(Integer paddingSize) {
        this.paddingSize = paddingSize;
    }
}