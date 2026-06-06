package com.gestiva.admin.users.web;

import java.util.ArrayList;
import java.util.List;

public class AdminUserPageView {

    private List<AdminUserRowView> users = new ArrayList<>();
    private List<String> availableRoles = new ArrayList<>();

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
}
