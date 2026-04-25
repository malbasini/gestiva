package com.gestiva.documents.template.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "document_template")
public class DocumentTemplate extends TenantAwareEntity {

    @Column(nullable = false, length = 80)
    private String code; // QUOTE_DEFAULT

    @Column(nullable = false, length = 180)
    private String name;

    @Column(nullable = false, length = 30)
    private String documentType; // QUOTE, ORDER

    @Lob
    @Column(nullable = false)
    private String templateHtml;

    @Column(nullable = false)
    private boolean active;

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

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getTemplateHtml() {
        return templateHtml;
    }

    public void setTemplateHtml(String templateHtml) {
        this.templateHtml = templateHtml;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}