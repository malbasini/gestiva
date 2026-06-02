package com.gestiva.settings.sequence.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "document_sequence",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_document_sequence_tenant_type", columnNames = {"tenant_id", "document_type"})
        }
)
public class DocumentSequence extends TenantAwareEntity {

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    @Column(name = "prefix", length = 20)
    private String prefix;

    @Column(name = "next_number", nullable = false)
    private Long nextNumber;

    @Column(name = "padding_size", nullable = false)
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
