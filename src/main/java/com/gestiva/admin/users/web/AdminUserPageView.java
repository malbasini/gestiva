package com.gestiva.admin.users.web;

import java.util.ArrayList;
import java.util.List;

public class AdminUserPageView {

    private List<AdminUserRowView> users = new ArrayList<>();
    private List<String> availableRoles = new ArrayList<>();
    private List<AdminUserCompanyOptionView> availableCompanies =
            new ArrayList<>();

    public List<AdminUserRowView> getUsers() {
        return users;
    }

    public void setUsers(List<AdminUserRowView> users) {
        this.users = users;
    }

    public List<String> getAvailableRoles() {
        return availableRoles;
    }

    public void setAvailableRoles(List<String> availableRoles) {
        this.availableRoles = availableRoles;
    }

    public List<AdminUserCompanyOptionView> getAvailableCompanies() {
        return availableCompanies;
    }

    public void setAvailableCompanies(
            List<AdminUserCompanyOptionView> availableCompanies) {
        this.availableCompanies = availableCompanies == null
                ? new ArrayList<>()
                : new ArrayList<>(availableCompanies);
    }
}
