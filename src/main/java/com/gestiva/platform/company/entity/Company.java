package com.gestiva.platform.company.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.*;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "company",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_company_tenant_code",
                        columnNames = {"tenant_id", "code"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_company_tenant_active",
                        columnList = "tenant_id, active"
                )
        }
)
public class Company extends TenantAwareEntity {

    @Column(nullable = false, length = 30)
    private String code;

    @Column(name = "legal_name", nullable = false, length = 180)
    private String legalName;

    @Column(name = "trade_name", length = 180)
    private String tradeName;

    @Column(name = "vat_number", length = 30)
    private String vatNumber;

    @Column(name = "tax_code", length = 30)
    private String taxCode;

    @Column(length = 180)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String website;

    @Column(name = "address_line_1", length = 255)
    private String addressLine1;

    @Column(name = "postal_code", length = 30)
    private String postalCode;

    @Column(length = 120)
    private String city;

    @Column(length = 10)
    private String province;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "locale_code", nullable = false, length = 10)
    private String localeCode;

    @Column(nullable = false)
    private boolean active = true;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
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

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
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

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getLocaleCode() {
        return localeCode;
    }

    public void setLocaleCode(String localeCode) {
        this.localeCode = localeCode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}