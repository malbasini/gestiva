package com.gestiva.settings.company.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "company_settings",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_company_settings_tenant", columnNames = {"tenant_id"})
        }
)
public class CompanySettings extends TenantAwareEntity {

    @Column(name = "company_name", nullable = false, length = 180)
    private String companyName;

    @Column(name = "trade_name", length = 180)
    private String tradeName;

    @Column(name = "vat_number", length = 30)
    private String vatNumber;

    @Column(name = "tax_code", length = 30)
    private String taxCode;

    @Column(name = "email", length = 180)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "website", length = 180)
    private String website;

    @Column(name = "address_line_1", length = 255)
    private String addressLine1;

    @Column(name = "postal_code", length = 30)
    private String postalCode;

    @Column(name = "city", length = 120)
    private String city;

    @Column(name = "province", length = 10)
    private String province;

    @Column(name = "country_code", length = 10)
    private String countryCode;

    @Column(name = "default_currency_code", nullable = false, length = 3)
    private String defaultCurrencyCode;

    @Column(name = "default_vat_pct", precision = 5, scale = 2)
    private java.math.BigDecimal defaultVatPct;

    @Column(name = "default_customer_due_days")
    private Integer defaultCustomerDueDays;

    @Column(name = "default_supplier_due_days")
    private Integer defaultSupplierDueDays;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
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

    public String getDefaultCurrencyCode() {
        return defaultCurrencyCode;
    }

    public void setDefaultCurrencyCode(String defaultCurrencyCode) {
        this.defaultCurrencyCode = defaultCurrencyCode;
    }

    public java.math.BigDecimal getDefaultVatPct() {
        return defaultVatPct;
    }

    public void setDefaultVatPct(java.math.BigDecimal defaultVatPct) {
        this.defaultVatPct = defaultVatPct;
    }

    public Integer getDefaultCustomerDueDays() {
        return defaultCustomerDueDays;
    }

    public void setDefaultCustomerDueDays(Integer defaultCustomerDueDays) {
        this.defaultCustomerDueDays = defaultCustomerDueDays;
    }

    public Integer getDefaultSupplierDueDays() {
        return defaultSupplierDueDays;
    }

    public void setDefaultSupplierDueDays(Integer defaultSupplierDueDays) {
        this.defaultSupplierDueDays = defaultSupplierDueDays;
    }
}
