package com.gestiva.accounting.entry.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "accounting_entry_line",
        indexes = {
                @Index(name = "idx_accounting_entry_line_tenant_entry", columnList = "tenant_id,accounting_entry_id")
        }
)
public class AccountingEntryLine extends TenantAwareEntity {

    @Column(name = "accounting_entry_id", nullable = false)
    private Long accountingEntryId;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    @Column(name = "line_type", nullable = false, length = 20)
    private String lineType; // INCOME, EXPENSE

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    public Long getAccountingEntryId() {
        return accountingEntryId;
    }

    public void setAccountingEntryId(Long accountingEntryId) {
        this.accountingEntryId = accountingEntryId;
    }

    public Integer getLineNo() {
        return lineNo;
    }

    public void setLineNo(Integer lineNo) {
        this.lineNo = lineNo;
    }

    public String getLineType() {
        return lineType;
    }

    public void setLineType(String lineType) {
        this.lineType = lineType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
