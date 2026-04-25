package com.gestiva.platform.permission.entity;

import com.gestiva.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "permission",
        uniqueConstraints = @UniqueConstraint(columnNames = {"code"}))
public class Permission extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String code; // CRM_LEAD_READ

    @Column(nullable = false, length = 120)
    private String moduleCode;

    @Column(nullable = false, length = 200)
    private String description;


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
