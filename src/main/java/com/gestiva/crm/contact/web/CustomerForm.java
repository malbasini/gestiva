package com.gestiva.crm.contact.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CustomerForm {

    @NotBlank(message = "Il nome è obbligatorio")
    @Size(max = 180, message = "Il nome non può superare 180 caratteri")
    private String name;

    @Email(message = "Email non valida")
    @Size(max = 180, message = "L'email non può superare 180 caratteri")
    private String email;

    @Size(max = 50, message = "Il telefono non può superare 50 caratteri")
    private String phone;

    @Size(max = 40, message = "La partita IVA non può superare 40 caratteri")
    private String vatNumber;

    @NotBlank(message = "Il tipo è obbligatorio")
    private String type;

    @NotBlank(message = "Lo stato è obbligatorio")
    private String status;

    @Size(max = 180, message = "L'indirizzo non può superare 180 caratteri")
    private String addressLine1;

    @Size(max = 100, message = "La città non può superare 100 caratteri")
    private String city;

    @Size(max = 20, message = "Il CAP non può superare 20 caratteri")
    private String postalCode;

    @Size(max = 10, message = "Il paese non può superare 10 caratteri")
    private String countryCode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
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