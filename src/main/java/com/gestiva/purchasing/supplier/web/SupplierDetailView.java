package com.gestiva.purchasing.supplier.web;

public class SupplierDetailView {

    private Long id;
    private String code;
    private String name;
    private String vatNumber;
    private String taxCode;
    private String email;
    private String phone;
    private String addressLine;
    private String city;
    private String postalCode;
    private String province;
    private String countryCode;
    private String notes;
    private boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}