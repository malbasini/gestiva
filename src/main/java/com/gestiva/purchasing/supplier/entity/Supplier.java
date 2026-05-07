package com.gestiva.purchasing.supplier.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.*;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "supplier",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_supplier_tenant_code", columnNames = {"tenant_id", "code"})
        }
)
public class Supplier extends TenantAwareEntity {

    @Column(name = "code", nullable = false, length = 60)
    private String code;

    @Column(name = "name", nullable = false, length = 180)
    private String name;

    @Column(name="vat_number",length = 40)
    private String vatNumber;

    @Column(name = "tax_code", length = 40)
    private String taxCode;

    @Column(name = "email", length = 180)
    private String email;

    @Column(name = "phone", length = 60)
    private String phone;

    @Column(name = "address_line", length = 255)
    private String addressLine;

    @Column(name = "city", length = 120)
    private String city;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "province", length = 20)
    private String province;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "active", nullable = false)
    private boolean active;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
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

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }
}