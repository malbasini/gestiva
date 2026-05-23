package com.gestiva.accounting.v2.account.web;

public class AccountOptionView {

    private Long id;
    private String code;
    private String name;
    private String label;

    public AccountOptionView(Long id, String code, String name, String label) {
       this.id = id;
       this.code = code;
       this.name = name;
       this.label = label;
    }

    public AccountOptionView() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
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
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
