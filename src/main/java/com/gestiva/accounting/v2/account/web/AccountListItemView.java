package com.gestiva.accounting.v2.account.web;

public class AccountListItemView {

    private Long id;
    private String code;
    private String name;
    private String accountType;
    private String nature;
    private Integer levelNo;
    private boolean leafAccount;
    private boolean systemAccount;
    private boolean active;
    private String parentCode;
    private String parentName;
    private String indentedName;

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
    }    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }    public Integer getLevelNo() {
        return levelNo;
    }

    public void setLevelNo(Integer levelNo) {
        this.levelNo = levelNo;
    }    public boolean isLeafAccount() {
        return leafAccount;
    }

    public void setLeafAccount(boolean leafAccount) {
        this.leafAccount = leafAccount;
    }    public boolean isSystemAccount() {
        return systemAccount;
    }

    public void setSystemAccount(boolean systemAccount) {
        this.systemAccount = systemAccount;
    }    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }    public String getIndentedName() {
        return indentedName;
    }

    public void setIndentedName(String indentedName) {
        this.indentedName = indentedName;
    }
}