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

    @Column(name="address_line_1", length = 2000)
    private String addressLine1;

    @Column(name="city", length = 300)
    private String city;

    @Column(name="postal_code", length = 100)
    private String postalCode;

    @Column(name="country_code", length = 20)
    private String countryCode;

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

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
