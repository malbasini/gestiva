package com.gestiva.settings.company.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CompanySettingsForm {

    @NotBlank(message = "La ragione sociale è obbligatoria.")
    @Size(max = 180)
    private String companyName;

    @Size(max = 180)
    private String tradeName;

    @Size(max = 30)
    private String vatNumber;

    @Size(max = 30)
    private String taxCode;

    @Size(max = 180)
    private String email;

    @Size(max = 50)
    private String phone;

    @Size(max = 180)
    private String website;

    @Size(max = 255)
    private String addressLine1;

    @Size(max = 30)
    private String postalCode;

    @Size(max = 120)
    private String city;

    @Size(max = 10)
    private String province;

    @Size(max = 10)
    private String countryCode;

    @NotBlank(message = "La valuta predefinita è obbligatoria.")
    @Size(max = 3)
    private String defaultCurrencyCode;

    @DecimalMin(value = "0.00", message = "L'aliquota IVA non può essere negativa.")
    private BigDecimal defaultVatPct;

    private Integer defaultCustomerDueDays;
    private Integer defaultSupplierDueDays;

    private List<DocumentSequenceForm> sequences = new ArrayList<>();

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

    public BigDecimal getDefaultVatPct() {
        return defaultVatPct;
    }

    public void setDefaultVatPct(BigDecimal defaultVatPct) {
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

    public List<DocumentSequenceForm> getSequences() {
        return sequences;
    }

    public void setSequences(List<DocumentSequenceForm> sequences) {
        this.sequences = sequences;
    }
}
