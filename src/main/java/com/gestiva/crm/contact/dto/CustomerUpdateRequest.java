package com.gestiva.crm.contact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CustomerUpdateRequest {

    @NotBlank
    @Size(max = 180)
    private String name;

    @Size(max = 30)
    private String vatNumber;

    @Size(max = 30)
    private String taxCode;

    @Email
    @Size(max = 180)
    private String email;

    @Size(max = 50)
    private String phone;

    @Size(max = 30)
    private String type;

    @Size(max = 30)
    private String status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVatNumber() { return vatNumber; }
    public void setVatNumber(String vatNumber) { this.vatNumber = vatNumber; }
    public String getTaxCode() { return taxCode; }
    public void setTaxCode(String taxCode) { this.taxCode = taxCode; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}