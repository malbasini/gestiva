package com.gestiva.accounting.v2.account.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "account",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_account_tenant_code", columnNames = {"tenant_id", "code"})
        },
        indexes = {
                @Index(name = "idx_account_tenant_type", columnList = "tenant_id,account_type"),
                @Index(name = "idx_account_tenant_active", columnList = "tenant_id,active")
        }
)
public class Account extends TenantAwareEntity {

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 180)
    private String name;

    @Column(name = "account_type", nullable = false, length = 30)
    private String accountType; // ASSET, LIABILITY, EQUITY, REVENUE, COST

    @Column(name = "nature", nullable = false, length = 10)
    private String nature; // DEBIT, CREDIT

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "level_no", nullable = false)
    private Integer levelNo;

    @Column(name = "leaf_account", nullable = false)
    private boolean leafAccount;

    @Column(name = "system_account", nullable = false)
    private boolean systemAccount;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "description", length = 500)
    private String description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }    public Integer getLevelNo() {
        return levelNo;
    }

    public void setLevelNo(Integer levelNo) {
        this.levelNo = levelNo;
    }    public boolean isLeafAccount() {
        return leafAccount;
    }

    public void setLeafAccount(boolean leafAccount) {
        this.leafAccount = leafAccount;
    }    public boolean isSystemAccount() {
        return systemAccount;
    }

    public void setSystemAccount(boolean systemAccount) {
        this.systemAccount = systemAccount;
    }    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
