package com.gestiva.settings.company.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DocumentSequenceForm {

    @NotBlank
    private String documentType;

    @Size(max = 20)
    private String prefix;

    @NotNull
    private Long nextNumber;

    @NotNull
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