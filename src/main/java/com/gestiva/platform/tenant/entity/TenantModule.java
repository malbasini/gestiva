package com.gestiva.platform.tenant.entity;

import com.gestiva.common.model.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "tenant_module",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "module_code"}))
public class TenantModule extends BaseEntity {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "module_code", nullable = false, length = 50)
    private String moduleCode; // CRM, SALES, DOCUMENTS, FINANCE

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "plan_code", length = 50)
    private String planCode;


    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }
}