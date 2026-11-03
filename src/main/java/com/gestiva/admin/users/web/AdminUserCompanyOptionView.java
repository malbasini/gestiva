package com.gestiva.admin.users.web;

public class AdminUserCompanyOptionView {

    private final Long id;
    private final String legalName;

    public AdminUserCompanyOptionView(Long id, String legalName) {
        this.id = id;
        this.legalName = legalName;
    }

    public Long getId() {
        return id;
    }

    public String getLegalName() {
        return legalName;
    }
}
