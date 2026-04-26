package com.gestiva.crm.lead.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "crm_lead")
public class Lead extends TenantAwareEntity {

    @Column(nullable = false, length = 180)
    private String companyName;

    @Column(length = 120)
    private String contactName;

    @Column(length = 180)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(nullable = false, length = 40)
    private String source; // LINKEDIN, WEBSITE, MANUAL

    @Column(nullable = false, length = 40)
    private String status; // NEW, QUALIFIED, LOST, WON

    @Column(length = 80)
    private String assignedTo;

    @Column(precision = 15, scale = 2)
    private BigDecimal estimatedValue;

    private LocalDate expectedCloseDate;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }

    public void setEstimatedValue(BigDecimal estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    public LocalDate getExpectedCloseDate() {
        return expectedCloseDate;
    }

    public void setExpectedCloseDate(LocalDate expectedCloseDate) {
        this.expectedCloseDate = expectedCloseDate;
    }
}