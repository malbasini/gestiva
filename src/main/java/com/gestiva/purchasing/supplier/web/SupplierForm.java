package com.gestiva.purchasing.supplier.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SupplierForm {

    @NotBlank(message = "Il codice è obbligatorio")
    @Size(max = 60, message = "Il codice non può superare 60 caratteri")
    private String code;

    @NotBlank(message = "Il nome è obbligatorio")
    @Size(max = 180, message = "Il nome non può superare 180 caratteri")
    private String name;

    @Size(max = 40, message = "La partita IVA non può superare 40 caratteri")
    private String vatNumber;

    @Size(max = 40, message = "Il codice fiscale non può superare 40 caratteri")
    private String taxCode;

    @Email(message = "Email non valida")
    @Size(max = 180, message = "L'email non può superare 180 caratteri")
    private String email;

    @Size(max = 60, message = "Il telefono non può superare 60 caratteri")
    private String phone;

    @Size(max = 255, message = "L'indirizzo non può superare 255 caratteri")
    private String addressLine;

    @Size(max = 120, message = "La città non può superare 120 caratteri")
    private String city;

    @Size(max = 20, message = "Il CAP non può superare 20 caratteri")
    private String postalCode;

    @Size(max = 20, message = "La provincia non può superare 20 caratteri")
    private String province;

    @Size(max = 2, message = "Il paese deve avere 2 caratteri")
    private String countryCode;

    @Size(max = 1000, message = "Le note non possono superare 1000 caratteri")
    private String notes;

    private boolean active = true;

    public String getCode() {
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