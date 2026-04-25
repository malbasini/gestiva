package com.gestiva.sales.sequence.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "document_sequence",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "sequence_code", "year_value"}))
public class DocumentSequence extends TenantAwareEntity {

    @Column(name = "sequence_code", nullable = false, length = 40)
    private String sequenceCode; // QUOTE, ORDER

    @Column(name = "year_value", nullable = false)
    private Integer yearValue;

    @Column(name = "next_number", nullable = false)
    private Integer nextNumber;

    @Column(length = 30)
    private String prefix;

    public String getSequenceCode() {
        return sequenceCode;
    }

    public void setSequenceCode(String sequenceCode) {
        this.sequenceCode = sequenceCode;
    }

    public Integer getYearValue() {
        return yearValue;
    }

    public void setYearValue(Integer yearValue) {
        this.yearValue = yearValue;
    }

    public Integer getNextNumber() {
        return nextNumber;
    }

    public void setNextNumber(Integer nextNumber) {
        this.nextNumber = nextNumber;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}