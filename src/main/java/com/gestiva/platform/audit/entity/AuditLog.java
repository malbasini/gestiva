package com.gestiva.platform.audit.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_tenant_entity", columnList = "tenant_id, entity_name, entity_id"),
        @Index(name = "idx_audit_tenant_created", columnList = "tenant_id, created_at")
})
public class AuditLog extends TenantAwareEntity {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "action_code", nullable = false, length = 30)
    private String actionCode; // CREATE, UPDATE, DELETE

    @Lob
    @Column(name = "old_value_json")
    private String oldValueJson;

    @Lob
    @Column(name = "new_value_json")
    private String newValueJson;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getOldValueJson() {
        return oldValueJson;
    }

    public void setOldValueJson(String oldValueJson) {
        this.oldValueJson = oldValueJson;
    }

    public String getNewValueJson() {
        return newValueJson;
    }

    public void setNewValueJson(String newValueJson) {
        this.newValueJson = newValueJson;
    }
}
