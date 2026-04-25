package com.gestiva.crm.contact.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer",
        indexes = @Index(name = "idx_customer_tenant_name", columnList = "tenant_id, name"))
public class Customer extends TenantAwareEntity {

    @Column(nullable = false, length = 180)
    private String name;

    @Column(length = 30)
    private String vatNumber;

    @Column(length = 30)
    private String taxCode;

    @Column(length = 180)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 30)
    private String type; // COMPANY, INDIVIDUAL

    @Column(length = 30)
    private String status; // ACTIVE, INACTIVE, PROSPECT


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
