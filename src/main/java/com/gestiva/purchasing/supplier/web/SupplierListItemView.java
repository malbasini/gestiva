package com.gestiva.purchasing.supplier.web;

public class SupplierListItemView {

    private Long id;
    private String code;
    private String name;
    private String vatNumber;
    private String email;
    private String phone;
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
    }    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}