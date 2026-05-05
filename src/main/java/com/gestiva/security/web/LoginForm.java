package com.gestiva.security.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginForm {

    @NotBlank(message = "Il tenant è obbligatorio")
    @Size(max = 120, message = "Il tenant non può superare 120 caratteri")
    private String tenantSlug;

    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "L'email non è valida")
    @Size(max = 180, message = "L'email non può superare 180 caratteri")
    private String email;

    @NotBlank(message = "La password è obbligatoria")
    private String password;

    public String getTenantSlug() {
        return tenantSlug;
    }

    public void setTenantSlug(String tenantSlug) {
        this.tenantSlug = tenantSlug;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toLoginIdentifier() {
        return (tenantSlug == null ? "" : tenantSlug.trim().toLowerCase()) +
                "|" +
                (email == null ? "" : email.trim().toLowerCase());
    }
}